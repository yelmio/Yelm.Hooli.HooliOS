package yelm.io.raccoon.basket.controller;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.raccoon.R;
import yelm.io.raccoon.basket.adapter.BasketAdapter;
import yelm.io.raccoon.basket.model.BasketCheckPOJO;
import yelm.io.raccoon.basket.model.DeletedId;
import yelm.io.raccoon.constants.Constants;
import yelm.io.raccoon.database_new.Common;
import yelm.io.raccoon.database_new.basket_new.BasketCart;
import yelm.io.raccoon.database_new.user_addresses.UserAddress;
import yelm.io.raccoon.databinding.ActivityBasketOnlyDeliveryBinding;
import yelm.io.raccoon.loader.controller.LoaderActivity;
import yelm.io.raccoon.main.model.Modifier;
import yelm.io.raccoon.order.OrderActivity;
import yelm.io.raccoon.rest.rest_api.RestAPI;
import yelm.io.raccoon.rest.client.RetrofitClient;
import yelm.io.raccoon.support_stuff.Logging;

public class BasketActivity extends AppCompatActivity {

    ActivityBasketOnlyDeliveryBinding binding;
    BasketAdapter basketAdapter;
    private final CompositeDisposable compositeDisposableBasket = new CompositeDisposable();
    UserAddress currentAddress;
    private BigDecimal deliveryCostStart = new BigDecimal("0");
    private BigDecimal deliveryCostFinal = new BigDecimal("0");
    private BigDecimal finalCost = new BigDecimal("0");
    private String deliveryTime = "0";
    private static final int PAYMENT_SUCCESS = 777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBasketOnlyDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding();
        currentAddress = getDeliveryAddress();
        //check if there is user address in db and delivery available to it
        //if there is not we just show basket and disable ordering button
        if (currentAddress != null && !Constants.ShopID.equals("0")) {
            checkBasket(currentAddress.latitude, currentAddress.longitude);
        } else {
            Log.d(Logging.debug, "Method onCreate() - currentAddress is null or ShopID equals 0");
            binding.layoutFinalCost.setVisibility(View.GONE);
            binding.layoutDeliveryNotAvailable.setVisibility(View.VISIBLE);
            binding.layoutDelivery.setVisibility(View.GONE);
            binding.time.setText(String.format("%s %s", "0", getText(R.string.delivery_time)));
            setCompositeDisposableBasket();
            basketAdapter = new BasketAdapter(this, Common.basketCartRepository.getBasketCartsList());
            binding.recyclerCart.setAdapter(basketAdapter);
        }
    }

    private void checkBasket(String lat, String lon) {
        //stop upgrade basket UI
        //compositeDisposableBasket.clear();

        JSONArray jsonObjectItems = new JSONArray();
        List<BasketCart> basketCarts = Common.basketCartRepository.getBasketCartsList();
        for (BasketCart basketCart : basketCarts) {
            try {
                JSONObject jsonObjectItem = new JSONObject();
                jsonObjectItem
                        .put("id", basketCart.itemID)
                        .put("count", basketCart.count);
                jsonObjectItems.put(jsonObjectItem);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(Logging.debug, "Method checkBasket() - jsonObjectItems: " + jsonObjectItems.toString());

        RetrofitClient.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                checkBasket(
                        RestAPI.PLATFORM_NUMBER,
                        Constants.ShopID,
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry(),
                        lat,
                        lon,
                        jsonObjectItems.toString()
                ).
                enqueue(new Callback<BasketCheckPOJO>() {
                    @Override
                    public void onResponse(@NotNull Call<BasketCheckPOJO> call, @NotNull final Response<BasketCheckPOJO> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                Log.d(Logging.debug, "Method checkBasket() - BasketCheckPOJO: " + response.body().toString());
                                deliveryTime = response.body().getDelivery().getTime();
                                binding.time.setText(String.format("%s %s", deliveryTime, getText(R.string.delivery_time)));
                                deliveryCostStart = new BigDecimal(response.body().getDelivery().getPrice());
                                new Thread(() -> updateBasketCartsQuantity(response.body().getDeletedId())).start();
                            } else {
                                Log.e(Logging.error, "Method checkBasket() - by some reason response is null!");
                            }
                        } else {
                            Log.e(Logging.error, "Method checkBasket() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<BasketCheckPOJO> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method checkBasket() - failure: " + t.toString());
                    }
                });
    }

    private void updateBasketCartsQuantity(List<DeletedId> deletedIDList) {
        for (DeletedId deletedId : deletedIDList) {
            Log.d(Logging.debug, "deletedId: " + deletedId.toString());
            BasketCart basketCart = Common.basketCartRepository.getBasketCartById(deletedId.getId());
            if (basketCart != null) {
                basketCart.quantity = deletedId.getAvailableCount();
                Common.basketCartRepository.updateBasketCart(basketCart);
            }
        }
        setCompositeDisposableBasket();
    }

    private void setCompositeDisposableBasket() {
        compositeDisposableBasket.
                add(Common.basketCartRepository.
                        getBasketCarts().
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribeOn(Schedulers.io()).
                        subscribe(this::updateBasket));
    }

    private void updateBasket(List<BasketCart> carts) {

        binding.emptyBasket.setVisibility(carts.size() == 0 ? View.VISIBLE : View.GONE);
        binding.layoutDeliveryInfo.setVisibility(carts.size() == 0 ? View.GONE : View.VISIBLE);
        binding.layoutFinalCost.setVisibility(carts.size() == 0 ? View.GONE : View.VISIBLE);

        basketAdapter = new BasketAdapter(this, carts);
        binding.recyclerCart.setAdapter(basketAdapter);

        finalCost = new BigDecimal("0");
        boolean allowOrdering = true;
        for (BasketCart cart : carts) {
            BigDecimal costCurrentCart = new BigDecimal(cart.finalPrice);
            for (Modifier modifier : cart.modifier) {
                costCurrentCart = costCurrentCart.add(new BigDecimal(modifier.getValue()));
            }
            costCurrentCart = costCurrentCart.multiply(new BigDecimal(cart.count));
            finalCost = finalCost.add(costCurrentCart);
            //check if quantity less than its count
            if (new BigDecimal(cart.count).compareTo(new BigDecimal(cart.quantity)) > 0) {
                allowOrdering = false;
            }
        }
        Log.d(Logging.debug, "Method updateBasket() - finalCost: " + finalCost.toString());

        if (finalCost.compareTo(new BigDecimal(LoaderActivity.settings.getString(LoaderActivity.MIN_ORDER_PRICE, "0"))) < 0) {
            binding.layoutMinOrderPrice.setVisibility(View.VISIBLE);
            allowOrdering = false;
            binding.orderMinPrice.setText(String.format("%s %s %s",
                    getString(R.string.basketActivityOrderMinPrice),
                    LoaderActivity.settings.getString(LoaderActivity.MIN_ORDER_PRICE, "0"),
                    LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "0")));
        } else {
            binding.layoutMinOrderPrice.setVisibility(View.GONE);
        }

        if (carts.size() != 0 && allowOrdering && currentAddress != null && !Constants.ShopID.equals("0")) {
            binding.ordering.setEnabled(true);
        } else {
            binding.ordering.setEnabled(false);
        }

        //show a note about possible free shipping
        //if finalCost of basket more than minimum value for free delivery
        if (finalCost.compareTo(new BigDecimal(LoaderActivity.settings.getString(LoaderActivity.MIN_PRICE_FOR_FREE_DELIVERY, "0"))) < 0) {
            deliveryCostFinal = deliveryCostStart;
            BigDecimal freeDelivery = new BigDecimal(LoaderActivity.settings.getString(LoaderActivity.MIN_PRICE_FOR_FREE_DELIVERY, "0"));
            freeDelivery = freeDelivery.subtract(finalCost);
            binding.freeDelivery.setText(String.format("%s %s %s %s",
                    getString(R.string.basketActivityFreeDelivery1),
                    freeDelivery,
                    LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, ""),
                    getString(R.string.basketActivityFreeDelivery2)));
            binding.freeDelivery.setVisibility(View.VISIBLE);
        } else {
            deliveryCostFinal = new BigDecimal("0");
            binding.freeDelivery.setVisibility(View.GONE);
        }

        Log.d(Logging.debug, "Method updateBasket() - deliveryCostFinal: " + deliveryCostFinal.toString());

        binding.deliveryCost.setText(String.format("%s %s",
                deliveryCostFinal,
                LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        binding.finalPrice.setText(String.format("%s %s",
                finalCost.add(deliveryCostFinal),
                LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
    }

    private void binding() {
        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.back.setOnClickListener(v -> finish());
        binding.cleanBasket.setOnClickListener(v -> Common.basketCartRepository.emptyBasketCart());
        binding.ordering.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderActivity.class);
            intent.putExtra("finalPrice", finalCost.toString());
            intent.putExtra("deliveryCost", deliveryCostFinal.toString());
            intent.putExtra("deliveryTime", deliveryTime);
            intent.putExtra(UserAddress.class.getSimpleName(), currentAddress);
            startActivityForResult(intent, PAYMENT_SUCCESS);
        });
    }

    public UserAddress getDeliveryAddress() {
        if (Common.userAddressesRepository.getUserAddressesList() == null) {
            return null;
        }
        for (UserAddress current : Common.userAddressesRepository.getUserAddressesList()) {
            if (current.isChecked) {
                return current;
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case PAYMENT_SUCCESS:
                Common.basketCartRepository.emptyBasketCart();
                Log.d("AlexDebug", "PAYMENT_SUCCESS " + data.getStringExtra("success"));
                if (Objects.equals(data.getStringExtra("success"), "card")) {
                    binding.paymentResultText.setText(getText(R.string.order_is_accepted_by_card));
                } else {
                    binding.paymentResultText.setText(getText(R.string.order_is_accepted_by_google_pay));
                }
                binding.paymentResult.setVisibility(View.VISIBLE);
                binding.lotti.playAnimation();
                break;
        }
        new Handler(Looper.getMainLooper()) {{
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.paymentResult.setVisibility(View.GONE);
                }
            }, 3000);
        }};
    }

    @Override
    protected void onDestroy() {
        compositeDisposableBasket.clear();
        super.onDestroy();
    }
}