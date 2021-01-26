package yelm.io.yelm.fragments.catalog_fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_old.basket.Cart;
import yelm.io.yelm.database_old.catalog.products.Product;
import yelm.io.yelm.database_old.catalog.stocks.Stock;
import yelm.io.yelm.R;
import yelm.io.yelm.fragments.catalog_fragment.adapter.CategoryProductAdapter;
import yelm.io.yelm.fragments.catalog_fragment.adapter.ProductsAdapter;
import yelm.io.yelm.fragments.catalog_fragment.adapter.SquareCategoryProductAdapter;
import yelm.io.yelm.main.adapter.NewsAdapter;
import yelm.io.yelm.fragments.catalog_fragment.model.CatalogClass;
import yelm.io.yelm.fragments.catalog_fragment.model.ProductsClass;
import yelm.io.yelm.fragments.catalog_fragment.model.StockClass;
import yelm.io.yelm.fragments.main_fragment.ArticleActivity;
import yelm.io.yelm.support_stuff.ItemOffsetDecorationRight;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.retrofit.API;
import yelm.io.yelm.retrofit.DynamicURL;
import yelm.io.yelm.retrofit.RetrofitClient;
import yelm.io.yelm.support_stuff.GradientTransformation;

public class CatalogFragment extends Fragment {

    RecyclerView recyclerStock;
    private NewsAdapter mNewsAdapter;

    RecyclerView recyclerProducts;
    private ProductsAdapter productsAdapter;

    private CategoryProductAdapter categoryProductsAdapter;
    ArrayList<CatalogClass> allCategory = new ArrayList<>();

    private SquareCategoryProductAdapter squareCategoryProductAdapter;


    private List<Product> products = new ArrayList<>();
    ImageView mainTopImage;
    CardView cardTopImage;
    TextView name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initStock();
        getAllCatalog();
    }

    private void displayCategory(ArrayList<CatalogClass> allCategory) {
        if (LoaderActivity.settings.getString(LoaderActivity.CATALOG_STYLE, "").equals("1")) {
            recyclerProducts.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
            squareCategoryProductAdapter = new SquareCategoryProductAdapter(getContext(), allCategory);
            squareCategoryProductAdapter.setListener(new SquareCategoryProductAdapter.Listener() {
                @Override
                public void onClick(Integer index) {
                    Intent intent = new Intent(getContext(), CategoryActivity.class);
                    intent.putExtra("category", allCategory.get(index).getItem().getName());
                    intent.putExtra("id", allCategory.get(index).getItem().getID());
                    intent.putExtra("count", allCategory.get(index).getItemCount());
                    getContext().startActivity(intent);
                }
            });
            recyclerProducts.setAdapter(squareCategoryProductAdapter);
        } else {
            recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            categoryProductsAdapter = new CategoryProductAdapter(getContext(), allCategory);
            categoryProductsAdapter.setListener(new CategoryProductAdapter.Listener() {
                @Override
                public void onClick(Integer index) {
                    Intent intent = new Intent(getContext(), CategoryActivity.class);
                    intent.putExtra("category", allCategory.get(index).getItem().getName());
                    intent.putExtra("id", allCategory.get(index).getItem().getID());
                    intent.putExtra("count", allCategory.get(index).getItemCount());
                    getContext().startActivity(intent);
                }
            });
            recyclerProducts.setAdapter(categoryProductsAdapter);
        }

//
//        recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
//        categoryProductsAdapter = new CategoryProductAdapter(getContext(), allCategory);
//        categoryProductsAdapter.setListener(new CategoryProductAdapter.Listener() {
//            @Override
//            public void onClick(Integer index) {
//                Intent intent = new Intent(getContext(), CategoryActivity.class);
//                intent.putExtra("category", allCategory.get(index).getItem().getName());
//                intent.putExtra("id", allCategory.get(index).getItem().getID());
//                intent.putExtra("count", allCategory.get(index).getItemCount());
//                getContext().startActivity(intent);
//            }
//        });
//        recyclerProducts.setAdapter(categoryProductsAdapter);
    }


    private void initStock() {
        Log.d("AlexDebug", "LoaderActivity: initStock");
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getStock(DynamicURL.getURL(API.URL_API_CARD_TYPE_CATALOG)).
                enqueue(new Callback<ArrayList<StockClass>>() {
                    @Override
                    public void onResponse(Call<ArrayList<StockClass>> call, final Response<ArrayList<StockClass>> response) {
                        if (response.isSuccessful()) {
                            List<Stock> stockList = new ArrayList<>();
                            for (int i = 0; i < response.body().size(); i++) {
                                Stock stock = new Stock(
                                        response.body().get(i).getName(),
                                        response.body().get(i).getImage(),
                                        response.body().get(i).getAttachments());
                                stockList.add(stock);
                            }
                            if (stockList.size() == 1) {
                                if(stockList.get(0).getName().trim().isEmpty()){
                                    Picasso.get()
                                            .load(stockList.get(0).getImage())
                                            .noPlaceholder()
                                            .into(mainTopImage);
                                }else {
                                    Picasso.get()
                                            .load(stockList.get(0).getImage())
                                            .noPlaceholder()
                                            .transform(new GradientTransformation(getContext()))
                                            .into(mainTopImage);
                                    name.setText(stockList.get(0).getName());
                                }
                                cardTopImage.setVisibility(View.VISIBLE);
                                cardTopImage.setOnClickListener((v) -> {
                                    setLinks(stockList.get(0).getAttachments());
                                });
                                recyclerStock.setVisibility(View.GONE);
                            } else {
                                cardTopImage.setVisibility(View.GONE);
                                //mNewsAdapter = new NewsAdapter(getContext(), stockList);
                                recyclerStock.setAdapter(mNewsAdapter);
                            }
                        } else {
                            Log.d("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<StockClass>> call, Throwable t) {
                    }
                });

    }
    private void setLinks(String attachments) {
        JSONObject json = null;
        JSONArray jsonArrayItems = null;
        JSONArray jsonArrayNews = null;
        String link = "";
        try {
            json = new JSONObject(attachments);
            link =  json.get("link").toString();
            jsonArrayItems = json.getJSONArray("items");
            jsonArrayNews = json.getJSONArray("news");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonArrayItems.length() != 0) {
            String name = "";
            String price = "0";
            String url = "";
            String text_about = "";
            String id = "";
            String type = "";
            String quantity = "";
            String parameters = "";
            try {
                JSONObject jsonItem = (JSONObject) jsonArrayItems.getJSONObject(0);
                id =  jsonItem.get("id").toString();
                name =  jsonItem.get("name").toString();
                JSONObject item = (JSONObject) jsonItem.get("item");
                quantity =  item.get("quantity").toString();
                parameters = item.get("parameters").toString();
                price = item.get("price").toString();
                text_about =  item.get("text_about").toString();
                type =  item.get("type").toString();
                JSONArray urls = item.getJSONArray("images");
                url =  urls.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(getContext(), ProductActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("price", price);
            intent.putExtra("image", url);
            intent.putExtra("id", id);
            intent.putExtra("parameters", parameters);
            intent.putExtra("type", type);
            intent.putExtra("quantity", quantity);
            intent.putExtra("text_about", text_about);
            //Objects.requireNonNull(getContext()).startActivity(intent);
        }

        if (jsonArrayNews.length() != 0) {
            String image = "";
            String title = "";
            String text_about = "";
            try {
                JSONObject jsonNews = (JSONObject) jsonArrayNews.getJSONObject(0);
                JSONArray jsonArray = jsonNews.getJSONArray("images");
                image = jsonArray.get(0).toString();
                title =jsonNews.get("title").toString();
                JSONObject text = (JSONObject) jsonNews.get("item");
                text_about =  text.get("text_about").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(getContext(), ArticleActivity.class);
            intent.putExtra("image", image);
            intent.putExtra("title", title);
            intent.putExtra("text_about", text_about);
            //Objects.requireNonNull(getContext()).startActivity(intent);
        }

        if (!link.isEmpty()&&  URLUtil.isValidUrl(link)) {
            Log.d("AlexDebug", "link: " + link);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            //Objects.requireNonNull(getContext()).startActivity(intent);
        }

    }


    private void getAllCatalog() {
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getCatalog(DynamicURL.getURL(API.URL_API_ALL_CATALOG)).
                enqueue(new Callback<ArrayList<CatalogClass>>() {
                    @Override
                    public void onResponse(Call<ArrayList<CatalogClass>> call, Response<ArrayList<CatalogClass>> response) {
                        if (response.isSuccessful()) {
                            Integer itemCount = 0;
                            for (int i = 0; i < response.body().size(); i++) {
                                itemCount += Integer.parseInt(response.body().get(i).getItemCount());
                                allCategory.add(response.body().get(i));
                            }
                            if (itemCount > 10) {
                                Log.d("AlexDebug", "itemCount>10");
                                displayCategory(allCategory);
                            } else {
                                Log.d("AlexDebug", "itemCount<10");
                                getProducts();
                            }
                        } else {
                            Log.e("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }
                    @Override
                    public void onFailure(Call<ArrayList<CatalogClass>> call, Throwable t) {
                    }
                });
    }







    private void getProducts() {
        recyclerProducts.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        Log.d("AlexDebug", "Catalog: initProducts");
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getProducts(DynamicURL.getURL(API.URL_API_ANY_PRODUCTS)).
                enqueue(new Callback<ArrayList<ProductsClass>>() {
                    @Override
                    public void onResponse(Call<ArrayList<ProductsClass>> call, Response<ArrayList<ProductsClass>> response) {
                        if (response.isSuccessful()) {
                            for (int i = 0; i < response.body().size(); i++) {
                                JSONObject json = null;
                                String name = "";
                                String price = "0";
                                String url = "";
                                String category = "";
                                String text_about = "";
                                String type = "";
                                String parameters = "";
                                String quantity = "";
                                String itemID = response.body().get(i).getID();
                                String date = response.body().get(i).getCreateDate();

                                try {
                                    json = new JSONObject(response.body().get(i).getItem());
                                    name =  json.get("name").toString();
                                    type =  json.get("type").toString();
                                    quantity = json.get("quantity").toString();
                                    category =  json.get("category").toString();
                                    parameters = json.get("parameters").toString();
                                    price = json.get("price").toString();
                                    text_about =  json.get("text_about").toString();
                                    JSONArray jsonArray = json.getJSONArray("images");
                                    url =  jsonArray.toString();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Product product = new Product(itemID, type, name, price, parameters, category, quantity, text_about, url,date);
                                products.add(product);
                            }

                            productsAdapter = new ProductsAdapter(getContext(), products);
                            productsAdapter.setListener(new ProductsAdapter.Listener() {
                                @Override
                                public void onClick(Product product) {
                                    Cart cartItem = new Cart();
                                    cartItem.item = product.getItemID();
                                    cartItem.type = product.getType();
                                    cartItem.count = "1";
                                    cartItem.name = product.getName();
                                    cartItem.price = product.getPrice();
                                    cartItem.quantity = product.quantity;
                                    cartItem.isPromo = false;

                                    String imageFirst = "";

                                    try {
                                        JSONArray jsonImageUrlArray = new JSONArray(product.getImage());
                                        imageFirst = jsonImageUrlArray.get(0).toString();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    cartItem.imageUrl = imageFirst;

                                    Cart cartByID = Common.cartRepository.getCartItemById(cartItem.item);
                                    if (cartByID == null) {
                                        Common.cartRepository.insertToCart(cartItem);
                                    } else {
                                        BigDecimal temp = new BigDecimal(cartByID.count);
                                        cartByID.count = temp.add(new BigDecimal("1")).toString();
                                        Common.cartRepository.updateCart(cartByID);
                                    }
                                }
                            });
                            recyclerProducts.setAdapter(productsAdapter);

                        } else {
                            Log.e("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<ProductsClass>> call, Throwable t) {

                    }
                });


    }

    private void initViews(View view) {
        recyclerStock = view.findViewById(R.id.recycler_cards);
        recyclerProducts = view.findViewById(R.id.recycler_products);
        mainTopImage = view.findViewById(R.id.mainTopImage);
        cardTopImage = view.findViewById(R.id.cardTopImage);
        name = view.findViewById(R.id.name);

        recyclerStock.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerStock.setHasFixedSize(false);
        recyclerStock.addItemDecoration(new ItemOffsetDecorationRight((int) getResources().getDimension(R.dimen.dimens_16dp)));

    }


}
