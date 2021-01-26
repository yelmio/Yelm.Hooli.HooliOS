package yelm.io.yelm.fragments.main_fragment;

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

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.database_old.articles.Articles;
import yelm.io.yelm.fragments.catalog_fragment.ProductActivity;
import yelm.io.yelm.fragments.catalog_fragment.adapter.ProductsAdapterHorizontal;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_old.basket.Cart;
import yelm.io.yelm.database_old.catalog.products.Product;
import yelm.io.yelm.database_old.news.News;
import yelm.io.yelm.R;
import yelm.io.yelm.fragments.catalog_fragment.model.ProductsClass;
import yelm.io.yelm.fragments.main_fragment.adapter.ArticlesAdapter;
import yelm.io.yelm.support_stuff.ItemOffsetDecorationRight;
import yelm.io.yelm.fragments.main_fragment.adapter.NewsAdapter;
import yelm.io.yelm.fragments.main_fragment.model.ArticleClass;
import yelm.io.yelm.fragments.main_fragment.model.NewsClassOLD;
import yelm.io.yelm.retrofit.API;
import yelm.io.yelm.retrofit.DynamicURL;
import yelm.io.yelm.retrofit.RetrofitClient;
import yelm.io.yelm.support_stuff.GradientTransformation;

public class MainFragment extends Fragment {

    RecyclerView recyclerArticles;
    ArrayList<ArticleClass> articlesList;
    private ArticlesAdapter articlesAdapter;

    RecyclerView recyclerNews;
    ArrayList<NewsClassOLD> newsList;
    private NewsAdapter newsAdapter;

    RecyclerView recyclerPopularProducts;
    private ProductsAdapterHorizontal productsAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    ImageView mainTopImage;
    CardView cardTopImage;
    TextView name;

    public MainFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initNews();
        initArticles();
        initPopularProducts();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mainTopImage = view.findViewById(R.id.mainTopImage);
        cardTopImage = view.findViewById(R.id.cardTopImage);
        name = view.findViewById(R.id.name);
        recyclerNews = view.findViewById(R.id.recycler_news);
        recyclerNews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerNews.setHasFixedSize(false);
        recyclerNews.addItemDecoration(new ItemOffsetDecorationRight((int) getResources().getDimension(R.dimen.dimens_16dp)));

        recyclerPopularProducts = view.findViewById(R.id.recyclerPopularProducts);
        recyclerPopularProducts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerPopularProducts.setHasFixedSize(false);
        recyclerPopularProducts.addItemDecoration(new ItemOffsetDecorationRight((int) getResources().getDimension(R.dimen.dimens_16dp)));

        recyclerArticles = view.findViewById(R.id.recycler_articles);
        recyclerArticles.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void initNews() {
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getNews(DynamicURL.getURL(API.URL_API_CARD_TYPE_MAIN)).
                enqueue(new Callback<ArrayList<NewsClassOLD>>() {
                    @Override
                    public void onResponse(Call<ArrayList<NewsClassOLD>> call, final Response<ArrayList<NewsClassOLD>> response) {
                        if (response.isSuccessful()) {
                            List<News> newsList = new ArrayList<>();
                            for (int i = 0; i < response.body().size(); i++) {
                                News news = new News(
                                        response.body().get(i).getName(),
                                        response.body().get(i).getImage(),
                                        response.body().get(i).getAttachments());
                                newsList.add(news);
                            }

                            if (newsList.size() == 1) {
                                if (newsList.get(0).getName().trim().isEmpty()) {
                                    Picasso.get()
                                            .load(newsList.get(0).getImage())
                                            .noPlaceholder()
                                            .into(mainTopImage);
                                } else {
                                    Picasso.get()
                                            .load(newsList.get(0).getImage())
                                            .noPlaceholder()
                                            .transform(new GradientTransformation(getContext()))
                                            .into(mainTopImage);
                                    name.setText(newsList.get(0).getName());
                                }
                                cardTopImage.setVisibility(View.VISIBLE);
                                cardTopImage.setOnClickListener((v) -> {
                                    setLinks(newsList.get(0).getAttachments());
                                });
                                recyclerNews.setVisibility(View.GONE);
                            } else {
                                cardTopImage.setVisibility(View.GONE);
                                newsAdapter = new NewsAdapter(getContext(), newsList);
                                recyclerNews.setAdapter(newsAdapter);
                            }
                        } else {
                            Log.d("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<NewsClassOLD>> call, Throwable t) {
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
            link = json.get("link").toString();
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
                id = jsonItem.get("id").toString();
                name = jsonItem.get("name").toString();
                JSONObject item = (JSONObject) jsonItem.get("item");
                quantity = item.get("quantity").toString();
                parameters = item.get("parameters").toString();
                price = item.get("price").toString();
                text_about = item.get("text_about").toString();
                type = item.get("type").toString();
                JSONArray urls = item.getJSONArray("images");
                url = urls.toString();
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
            getContext().startActivity(intent);
        }

        if (jsonArrayNews.length() != 0) {
            String image = "";
            String title = "";
            String text_about = "";
            try {
                JSONObject jsonNews = (JSONObject) jsonArrayNews.getJSONObject(0);
                JSONArray jsonArray = jsonNews.getJSONArray("images");
                image = jsonArray.get(0).toString();
                title = jsonNews.get("title").toString();
                JSONObject text = (JSONObject) jsonNews.get("item");
                text_about = text.get("text_about").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(getContext(), ArticleActivity.class);
            intent.putExtra("image", image);
            intent.putExtra("title", title);
            intent.putExtra("text_about", text_about);
            getContext().startActivity(intent);
        }

        if (!link.isEmpty()&&  URLUtil.isValidUrl(link)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            getContext().startActivity(intent);
        }
    }


    private void initPopularProducts() {
        Log.d("AlexDebug", "LoaderActivity: initProducts");
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getPopularProducts(DynamicURL.getURL(API.URL_API_POPULAR_PRODUCTS)).
                enqueue(new Callback<ArrayList<ProductsClass>>() {
                    @Override
                    public void onResponse(Call<ArrayList<ProductsClass>> call, final Response<ArrayList<ProductsClass>> response) {
                        if (response.isSuccessful()) {
                            List<Product> products = new ArrayList<>();
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
                                    name = json.get("name").toString();
                                    type = json.get("type").toString();
                                    quantity = json.get("quantity").toString();
                                    category = json.get("category").toString();
                                    parameters = json.get("parameters").toString();
                                    price = json.get("price").toString();
                                    text_about = json.get("text_about").toString();
                                    JSONArray jsonArray = json.getJSONArray("images");
                                    url = jsonArray.toString();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Product product = new Product(itemID, type, name, price, parameters, category, quantity, text_about, url, date);
                                products.add(product);
                            }
                            productsAdapter = new ProductsAdapterHorizontal(getContext(), products);
                            productsAdapter.setListener(new ProductsAdapterHorizontal.Listener() {
                                @Override
                                public void onClick(Product product) {
                                    Cart cartItem = new Cart();
                                    cartItem.item = product.getItemID();
                                    cartItem.type = product.getType();
                                    cartItem.count = "1";
                                    cartItem.name = product.getName();
                                    cartItem.isPromo = false;
                                    cartItem.price = product.getPrice();
                                    cartItem.quantity = product.quantity;
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
                            recyclerPopularProducts.setAdapter(productsAdapter);
                        } else {
                            Log.e("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<ProductsClass>> call, Throwable t) {
                    }
                });
    }

    private void initArticles() {
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getArticles(DynamicURL.getURL(API.URL_API_NEWS)).
                enqueue(new Callback<ArrayList<ArticleClass>>() {
                    @Override
                    public void onResponse(Call<ArrayList<ArticleClass>> call, Response<ArrayList<ArticleClass>> response) {
                        if (response.isSuccessful()) {
                            List<Articles> articlesList = new ArrayList<>();
                            for (int i = 0; i < response.body().size(); i++) {

                                ArticleClass currentArticle = response.body().get(i);
                                JSONObject json = null;
                                String url = "";
                                String title = "";
                                String subtitle = "";
                                String theme = "";
                                String text_about = "";
                                String viewscreen = currentArticle.getViewScreen();
                                try {
                                    json = new JSONObject(currentArticle.getItem());
                                    JSONArray jsonArray = json.getJSONArray("images");
                                    url = jsonArray.get(0).toString();
                                    title = json.get("title").toString();
                                    theme = json.get("theme").toString();
                                    subtitle = json.get("subtitle").toString();
                                    text_about = json.get("text_about").toString();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Articles article = new Articles(theme, title, subtitle, url, text_about, viewscreen);
                                articlesList.add(article);
                            }
                            articlesAdapter = new ArticlesAdapter(getContext(), articlesList);
                            recyclerArticles.setAdapter(articlesAdapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<ArticleClass>> call, Throwable t) {
                    }
                });
    }

}
