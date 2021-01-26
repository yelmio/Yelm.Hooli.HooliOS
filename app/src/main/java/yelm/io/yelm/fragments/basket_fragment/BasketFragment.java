package yelm.io.yelm.fragments.basket_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.old_version.cashback.CashBackViewModel;
import yelm.io.yelm.databinding.FragmentBasketBinding;
import yelm.io.yelm.fragments.catalog_fragment.adapter.ProductsAdapterHorizontal;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.R;
import yelm.io.yelm.fragments.basket_fragment.adapter.BasketAdapter;
import yelm.io.yelm.database_old.basket.Cart;
import yelm.io.yelm.database_old.catalog.products.Product;
import yelm.io.yelm.fragments.catalog_fragment.model.ProductsClass;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.order.OrderActivity;
import yelm.io.yelm.retrofit.API;
import yelm.io.yelm.retrofit.DynamicURL;
import yelm.io.yelm.retrofit.RetrofitClient;
import yelm.io.yelm.old_version.user.history_orders.OrdersHistoryActivity;

public class BasketFragment extends Fragment {

    ArrayList<Cart> productsList = new ArrayList<>();
    private BasketAdapter basketAdapter;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    BigDecimal minOrderPriceBigDecimal = new BigDecimal(LoaderActivity.settings.getString(LoaderActivity.MIN_ORDER_PRICE, ""));

    private ProductsAdapterHorizontal productsAdapter;

    //for animation duration
    Handler h;

    private CashBackViewModel cashBackModel;

    private FragmentBasketBinding binding;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initPopularProducts();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBasketBinding.inflate(inflater, container, false);

        if (Common.cartRepository.countCardItems() != 0) {
            binding.emptyTextView.setVisibility(View.GONE);
        }

        binding.recyclerPopularProducts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerBasket.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.checkout.setOnClickListener(view -> {
            if (Common.cartRepository.countCardItems() == 0) {
                Toast.makeText(getContext(),getText(R.string.cart_is_epty), Toast.LENGTH_SHORT).show();
            }else {
                Intent intent = new Intent(getContext(), OrderActivity.class);
                getContext().startActivity(intent);
            }

        });
        binding.deleteAll.setOnClickListener(view -> Common.cartRepository.emptyCart());

        binding.archive.setOnClickListener((View.OnClickListener) view -> {
            Intent intent = new Intent(getContext(), OrdersHistoryActivity.class);
            getContext().startActivity(intent);
        });

        return binding.getRoot();
    }


    private void initPopularProducts() {
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
                                    name =  json.get("name").toString();
                                    type = json.get("type").toString();
                                    quantity =  json.get("quantity").toString();
                                    category =  json.get("category").toString();
                                    parameters = json.get("parameters").toString().toString();
                                    price = json.get("price").toString();
                                    text_about =  json.get("text_about").toString();
                                    JSONArray jsonArray = json.getJSONArray("images");
                                    url = jsonArray.get(0).toString();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Product product = new Product(itemID, type, name, price, parameters, category, quantity, text_about, url,date);
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
                            binding.recyclerPopularProducts.setAdapter(productsAdapter);
                        } else {
                            Log.e("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<ProductsClass>> call, Throwable t) {
                    }
                });
    }


    private void displayCartItems() {
        Log.d("AlexDebug", "Basket fragment: displayCartItems");
        compositeDisposable.
                add(Common.cartRepository.
                        getCartItems().
                        observeOn(AndroidSchedulers.from(Objects.requireNonNull(Looper.myLooper()))).
                        subscribeOn(Schedulers.io()).
                        subscribe(new Consumer<List<Cart>>() {
                            @Override
                            public void accept(List<Cart> carts) throws Exception {
                                if (Common.cartRepository.countCardItems() != 0) {
                                    binding.emptyTextView.setVisibility(View.GONE);
                                } else {
                                    binding.emptyTextView.setVisibility(View.VISIBLE);
                                }
                                displayCartItem(carts);
                            }
                        }));
    }


    private void displayCartItem(List<Cart> carts) {
        basketAdapter = new BasketAdapter(getContext(), carts);
        BigDecimal finalCost = new BigDecimal("0");

        for (Cart cart : carts) {
            BigDecimal bigCount = new BigDecimal(String.valueOf(cart.count));
            BigDecimal bigPriceCount = new BigDecimal(String.valueOf(cart.price));
            finalCost = finalCost.add(bigCount.multiply(bigPriceCount));
        }

        Log.d("AlexDebug", "BasketFragment: finalCost " + finalCost);
        Log.d("AlexDebug", "BasketFragment: countCardItems " + Common.cartRepository.countCardItems());
        binding.checkout.setText(new StringBuilder().append(getResources().getString(R.string.checkout)).append(": ").append(finalCost).append(getResources().getString(R.string.ruble_sign)).toString());
        binding.recyclerBasket.setAdapter(basketAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        displayCartItems();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
