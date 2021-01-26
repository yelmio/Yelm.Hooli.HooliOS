package yelm.io.yelm.fragments.catalog_fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import org.angmarch.views.NiceSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_old.basket.Cart;
import yelm.io.yelm.database_old.catalog.products.Product;
import yelm.io.yelm.fragments.catalog_fragment.adapter.ProductsAdapter;
import yelm.io.yelm.fragments.catalog_fragment.model.ProductsClass;
import yelm.io.yelm.retrofit.API;
import yelm.io.yelm.retrofit.DynamicURL;
import yelm.io.yelm.retrofit.RetrofitClient;

public class CategoryActivity extends AppCompatActivity {

    RecyclerView recyclerProducts;
    private ProductsAdapter productsAdapter;

    TextView title;
    String catalog, id, count;
    private List<Product> products = new ArrayList<>();

    EditText editTextSearch;
    NiceSpinner niceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);
        recyclerProducts = findViewById(R.id.recyclerProducts);
        title = findViewById(R.id.title);
        editTextSearch = findViewById(R.id.editTextSearch);
        niceSpinner = findViewById(R.id.nice_spinner);

        recyclerProducts.setLayoutManager(new StaggeredGridLayoutManager(2, 1));

        niceSpinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            Log.d("AlexDebug", "item: " + item);
            Log.d("AlexDebug", "position: " + position);
            productsAdapter.sortProducts(position);
            recyclerProducts.scrollToPosition(0);
        });


        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                productsAdapter.getFilter().filter(s.toString());
            }
        });


        Intent intent = getIntent();
        catalog = intent.getStringExtra("category");
        id = intent.getStringExtra("id");
        count = intent.getStringExtra("count");
        title.setText(catalog);
        getProductsByID();


    }


    private void getProductsByID() {
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getProductsByID(DynamicURL.getURL(API.URL_API_GET_PRODUCTS_BY_ID) + id + "&sort=normal").
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
                                String date = response.body().get(i).getCreateDate();

                                String itemID = response.body().get(i).getID();
                                try {
                                    json = new JSONObject(response.body().get(i).getItem());
                                    name = json.get("name").toString();
                                    type =  json.get("type").toString();
                                    quantity =  json.get("quantity").toString();
                                    category =  json.get("category").toString();
                                    parameters = json.get("parameters").toString();
                                    price = json.get("price").toString();
                                    text_about =  json.get("text_about").toString();
                                    JSONArray jsonArray = json.getJSONArray("images");
                                    url = jsonArray.toString();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Product product = new Product(itemID, type, name, price, parameters, category, quantity, text_about, url, date);
                                products.add(product);
                            }

                            productsAdapter = new ProductsAdapter(CategoryActivity.this, products);
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
}