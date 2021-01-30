package yelm.io.yelm.search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;


import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.basket.controller.BasketActivity;
import yelm.io.yelm.basket.controller.BasketActivityOnlyDelivery;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.R;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.databinding.ActivitySearchBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.Item;
import yelm.io.yelm.main.model.Modifier;
import yelm.io.yelm.retrofit.new_api.RestAPI;
import yelm.io.yelm.retrofit.new_api.RetrofitClientNew;
import yelm.io.yelm.support_stuff.ItemOffsetDecorationBottom;
import yelm.io.yelm.constants.Constants;

public class SearchActivity extends AppCompatActivity {
    ActivitySearchBinding binding;
    SearchProductAdapter searchProductAdapter;
    private List<Item> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerProducts.setHasFixedSize(false);
        binding.recyclerProducts.addItemDecoration(new ItemOffsetDecorationBottom((int) getResources().getDimension(R.dimen.dimen_60dp)));

        getAllProducts();

        binding.back.setOnClickListener(v -> finish());

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchProductAdapter.getFilter().filter(s.toString());
                binding.recyclerProducts.scrollToPosition(0);
                return false;
            }
        });

        binding.basket.setOnClickListener(v -> startActivity(new Intent(this, BasketActivityOnlyDelivery.class)));

    }

    private void getAllProducts() {
        RetrofitClientNew.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                getAllItems(
                        "3",
                        String.valueOf(getResources().getConfiguration().locale.getLanguage()),
                        String.valueOf(getResources().getConfiguration().locale.getCountry()),
                        RestAPI.PLATFORM_NUMBER,
                        Constants.ShopID).
                enqueue(new Callback<ArrayList<Item>>() {
                    @Override
                    public void onResponse(@NotNull Call<ArrayList<Item>> call, @NotNull final Response<ArrayList<Item>> response) {
                        if (response.isSuccessful()) {
                            searchProductAdapter = new SearchProductAdapter(SearchActivity.this, response.body());
                            binding.recyclerProducts.setAdapter(searchProductAdapter);
                        } else {
                            Log.e(AlexTAG.error, "Method getAllProducts() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ArrayList<Item>> call, @NotNull Throwable t) {
                        Log.e(AlexTAG.error, "Method getAllProducts() - failure: " + t.toString());
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateCost();
    }


    private void updateCost() {
        List<BasketCart> carts = Common.basketCartRepository.getBasketCartsList();
        if (carts.size() == 0) {
            binding.basket.setText(String.format("0 %s", LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            binding.basket.setVisibility(View.GONE);
        } else {
            binding.basket.setVisibility(View.VISIBLE);
            BigDecimal basketPrice = new BigDecimal("0");
            for (BasketCart cart : carts) {
                BigDecimal costCurrent = new BigDecimal(cart.finalPrice);
                for (Modifier modifier : cart.modifier) {
                    costCurrent = costCurrent.add(new BigDecimal(modifier.getValue()));
                }
                costCurrent = costCurrent.multiply(new BigDecimal(cart.count));
                basketPrice = basketPrice.add(costCurrent);
            }

            binding.basket.setText(String.format("%s %s", basketPrice.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            Log.d(AlexTAG.debug, "Method updateCost() - carts.size(): " + carts.size() + "\n" +
                    "basketPrice.toString(): " + basketPrice.toString());
        }
    }
}