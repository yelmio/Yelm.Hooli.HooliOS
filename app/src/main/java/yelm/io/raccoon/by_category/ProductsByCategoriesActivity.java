package yelm.io.raccoon.by_category;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.raccoon.basket.controller.BasketActivity;
import yelm.io.raccoon.database_new.user_addresses.UserAddress;
import yelm.io.raccoon.support_stuff.Logging;
import yelm.io.raccoon.database_new.basket_new.BasketCart;
import yelm.io.raccoon.database_new.Common;
import yelm.io.raccoon.databinding.ActivityProductsByCategoryBinding;
import yelm.io.raccoon.loader.controller.LoaderActivity;
import yelm.io.raccoon.constants.Constants;
import yelm.io.raccoon.rest.rest_api.RestAPI;
import yelm.io.raccoon.rest.client.RetrofitClient;

public class ProductsByCategoriesActivity extends AppCompatActivity {

    ActivityProductsByCategoryBinding binding;

    private final CompositeDisposable compositeDisposableBasket = new CompositeDisposable();
    private ArrayList<ProductsByCategoryClass> productsByCategoryList = new ArrayList<>();
    private boolean allowUpdateUI = false;

    private ArrayList<ProductsByCategoryFragment> listFragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductsByCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle args = getIntent().getExtras();
        if (args != null) {
            Log.d(Logging.debug, "args.getString(catalogID): " + args.getString("catalogID"));
            Log.d(Logging.debug, "args.getString(catalogName): " + args.getString("catalogName"));

            getProductByCategory(args.getString("catalogID"));
            binding.title.setText(args.getString("catalogName"));
        }
        binding.back.setOnClickListener(v -> finish());
        binding.basket.setOnClickListener(v -> startActivity(new Intent(this, BasketActivity.class)));

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                for (ProductsByCategoryFragment fragment : listFragments) {
                    fragment.sortAdapter(s);
                }
                return false;
            }
        });
    }

    private void getProductByCategory(String categoryID) {
        String latitude = "0";
        String longitude = "0";
        if (Common.userAddressesRepository.getUserAddressesList() != null && Common.userAddressesRepository.getUserAddressesList().size() != 0) {
            for (UserAddress userAddress : Common.userAddressesRepository.getUserAddressesList()) {
                if (userAddress.isChecked) {
                    latitude = userAddress.latitude;
                    longitude = userAddress.longitude;
                    Log.d(Logging.debug, "latitude: " + latitude);
                    Log.d(Logging.debug, "longitude: " + longitude);
                    break;
                }
            }
        }
        RetrofitClient.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                getProductsByCategory(
                        "3",
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry(),
                        RestAPI.PLATFORM_NUMBER,
                        Constants.ShopID,
                        categoryID,
                        latitude,
                        longitude).
                enqueue(new Callback<ArrayList<ProductsByCategoryClass>>() {
                    @Override
                    public void onResponse(@NotNull Call<ArrayList<ProductsByCategoryClass>> call, @NotNull final Response<ArrayList<ProductsByCategoryClass>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                productsByCategoryList = response.body();
                                rewriteView();
                            } else {
                                Log.e(Logging.error, "Method getProductByCategory() - by some reason response is null!");
                            }
                        } else {
                            Log.e(Logging.error, "Method getProductByCategory() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ArrayList<ProductsByCategoryClass>> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method getProductByCategory() - failure: " + t.toString());
                    }
                });
    }

    @Override
    public void onStop() {
        compositeDisposableBasket.clear();
        binding.search.setQuery("", false);
        binding.search.clearFocus();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (allowUpdateUI) {
            rewriteView();
        }
        allowUpdateUI = true;
        updateCost();
    }

    private void updateCost() {
        compositeDisposableBasket.add(Common.basketCartRepository.getBasketCarts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(carts -> {
                    if (carts.size() == 0) {
                        binding.basketLayout.setVisibility(View.GONE);
                        binding.basket.setText(String.format("0 %s", LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
                    } else {
                        binding.basketLayout.setVisibility(View.VISIBLE);
                        BigDecimal basketPrice = new BigDecimal("0");
                        for (BasketCart cart : carts) {
                            basketPrice = basketPrice.add(new BigDecimal(cart.finalPrice).multiply(new BigDecimal(cart.count)));
                        }
                        binding.basket.setText(String.format("%s %s", basketPrice.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
                        Log.d(Logging.debug, "Method updateCost() - carts.size(): " + carts.size() + "\n" +
                                "basketPrice.toString(): " + basketPrice.toString());
                    }
                }));
    }

    private void rewriteView() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
        binding.storeFragments.removeAllViews();
        boolean isHorizontalLayout = true;
        if (productsByCategoryList.size() == 1) {
            isHorizontalLayout = false;
        }
        for (int i = 0; i < productsByCategoryList.size(); i++) {
            FrameLayout frameLayout = new FrameLayout(ProductsByCategoriesActivity.this);
            frameLayout.setId(View.generateViewId());
            ProductsByCategoryFragment categoryFragment = new ProductsByCategoryFragment(productsByCategoryList.get(i), isHorizontalLayout);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(frameLayout.getId(), categoryFragment, "fragment" + i).commitAllowingStateLoss();
            binding.storeFragments.addView(frameLayout);
            listFragments.add(categoryFragment);
        }
    }
}