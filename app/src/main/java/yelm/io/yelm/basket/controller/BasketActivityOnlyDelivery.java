package yelm.io.yelm.basket.controller;

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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.basket.adapter.BasketAdapter;
import yelm.io.yelm.basket.model.BasketCheckPOJO;
import yelm.io.yelm.basket.model.DeletedId;
import yelm.io.yelm.constants.Constants;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.ActivityBasketOnlyDeliveryBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.Modifier;
import yelm.io.yelm.order.OrderActivityNew;
import yelm.io.yelm.retrofit.new_api.RestAPI;
import yelm.io.yelm.retrofit.new_api.RetrofitClientNew;
import yelm.io.yelm.support_stuff.Logging;
import yelm.io.yelm.user_address.controller.AddressesBottomSheet;

public class BasketActivityOnlyDelivery extends AppCompatActivity implements AddressesBottomSheet.AddressesBottomSheetListener {

    ActivityBasketOnlyDeliveryBinding binding;
    BasketAdapter basketAdapter;
    private final CompositeDisposable compositeDisposableBasket = new CompositeDisposable();
    UserAddress currentAddress;
    AddressesBottomSheet addressesBottomSheet = new AddressesBottomSheet();
    private BigDecimal deliveryCost = new BigDecimal("0");
    private BigDecimal finalCost = new BigDecimal("0");
    private String deliveryTime = "0";
    private static final int PAYMENT_SUCCESS = 777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBasketOnlyDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding();
        currentAddress = setDeliveryAddress();
        //check if there is user address in db - if there is not we just show basket and disable ordering button
        if (currentAddress != null && !Constants.ShopID.equals("0")) {
            checkBasket(currentAddress.latitude, currentAddress.longitude);
        } else {
            Log.d(Logging.debug, "Method onCreate() - currentAddress is null or ShopID equals 0");
            //binding.addressDelivery.setText(getText(R.string.basketActivitySelectAddress));
            binding.time.setText(String.format("%s %s", deliveryTime, getText(R.string.delivery_time)));
            binding.layoutDeliveryNotAvailable.setVisibility(View.VISIBLE);
            setCompositeDisposableBasket();
            basketAdapter = new BasketAdapter(this, Common.basketCartRepository.getBasketCartsList());
            binding.recyclerCart.setAdapter(basketAdapter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    synchronized private void checkBasket(String lat, String lon) {
        //stop upgrade basket UI
        compositeDisposableBasket.clear();

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

        RetrofitClientNew.
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
                                deliveryCost = new BigDecimal(response.body().getDelivery().getPrice());
                                binding.deliveryCost.setText(String.format("%s %s", deliveryCost, LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
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

        binding.emptyTextView.setVisibility(carts.size() == 0 ? View.VISIBLE : View.GONE);
        binding.layoutDeliveryCost.setVisibility(carts.size() == 0 ? View.GONE : View.VISIBLE);

        basketAdapter = new BasketAdapter(this, carts);
        binding.recyclerCart.setAdapter(basketAdapter);

        finalCost = new BigDecimal("0").add(deliveryCost);

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
        binding.finalPrice.setText(String.format("%s %s", finalCost, LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        Log.d(Logging.debug, "Method updateBasket() - finalCost: " + finalCost.toString());
        if (carts.size() != 0 && allowOrdering && currentAddress != null && !Constants.ShopID.equals("0")) {
            binding.ordering.setEnabled(true);
        } else {
            binding.ordering.setEnabled(false);
        }
    }

    private void binding() {
        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.back.setOnClickListener(v -> finish());
        binding.cleanBasket.setOnClickListener(v -> {
            deliveryCost = new BigDecimal("0");
            Common.basketCartRepository.emptyBasketCart();
            binding.time.setText(String.format("0 %s", getText(R.string.delivery_time)));
        });
        binding.addressDelivery.setOnClickListener(v -> callAddressesBottomSheet());
        binding.ordering.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderActivityNew.class);
            intent.putExtra("finalPrice", finalCost.toString());
            intent.putExtra("deliveryCost", deliveryCost.toString());
            intent.putExtra("deliveryTime", deliveryTime);
            intent.putExtra(UserAddress.class.getSimpleName(), currentAddress);
            startActivityForResult(intent, PAYMENT_SUCCESS);
        });
    }

    private void callAddressesBottomSheet() {
        //check if addressesBottomSheetForBasket is added otherwise we get exception:
        //java.lang.IllegalStateException: Fragment already added
        if (!addressesBottomSheet.isAdded()) {
            addressesBottomSheet.show(getSupportFragmentManager(), "addressesBottomSheet");
        }
    }

    public UserAddress setDeliveryAddress() {
        for (UserAddress current : Common.userAddressesRepository.getUserAddressesList()) {
            if (current.isChecked) {
                binding.addressDelivery.setText(current.address);
                return current;
            }
        }
        return null;
    }

    @Override
    public void selectedAddress(UserAddress userAddress) {
        currentAddress = userAddress;
        Log.d(Logging.debug, "Method selectedAddress() - address: " + currentAddress.address);
        binding.addressDelivery.setText(String.format("%s", currentAddress.address));
        checkBasket(currentAddress.latitude, currentAddress.longitude);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case PAYMENT_SUCCESS:
                deliveryCost = new BigDecimal("0");
                Common.basketCartRepository.emptyBasketCart();
                binding.time.setText(String.format("0 %s", getText(R.string.delivery_time)));
                Log.d("AlexDebug", "PAYMENT_SUCCESS " + data.getStringExtra("success"));
                binding.paymentResult.setVisibility(View.VISIBLE);
                if (data.getStringExtra("success").equals("card")) {
                    binding.paymentResultText.setText(getText(R.string.order_is_accepted_by_card));
                } else {
                    binding.paymentResultText.setText(getText(R.string.order_is_accepted_by_google_pay));
                }
                binding.lotti.playAnimation();
                break;
        }
        new Handler(Looper.getMainLooper()) {{
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.paymentResult.setVisibility(View.GONE);
                }
            }, 2000);
        }};
    }

    @Override
    protected void onDestroy() {
        compositeDisposableBasket.clear();
        super.onDestroy();
    }
}