package yelm.io.yelm.main.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.math.BigDecimal;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import yelm.io.yelm.basket.controller.BasketActivityOnlyDelivery;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.databinding.ActivityItemsFromNewsBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.adapter.ProductsNewMenuSquareImageAdapter;
import yelm.io.yelm.main.model.Item;
import yelm.io.yelm.support_stuff.AlexTAG;

public class ItemsFromNewsActivity extends AppCompatActivity {
    ActivityItemsFromNewsBinding binding;
    private final CompositeDisposable compositeDisposableBasket = new CompositeDisposable();
    ProductsNewMenuSquareImageAdapter productsSquareAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemsFromNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.title.setText(getIntent().getStringExtra("title"));

        ArrayList<Item> items = getIntent().getParcelableArrayListExtra("items");
        binding.recycler.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        productsSquareAdapter = new ProductsNewMenuSquareImageAdapter(this, items);
        binding.recycler.setAdapter(productsSquareAdapter);
        binding.back.setOnClickListener(v -> finish());
        binding.basket.setOnClickListener(v -> startActivity(new Intent(this, BasketActivityOnlyDelivery.class)));

    }

    @Override
    public void onStop() {
        compositeDisposableBasket.clear();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                        Log.d(AlexTAG.debug, "Method updateCost() - carts.size(): " + carts.size() + "\n" +
                                "basketPrice.toString(): " + basketPrice.toString());
                    }
                }));
    }

}