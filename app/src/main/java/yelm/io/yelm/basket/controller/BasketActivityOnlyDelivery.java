package yelm.io.yelm.basket.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
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
import yelm.io.yelm.basket.model.BasketCheckResponse;
import yelm.io.yelm.basket.model.DeletedId;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.ActivityBasketOnlyDeliveryBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.Modifier;
import yelm.io.yelm.order.OrderActivityNew;
import yelm.io.yelm.retrofit.new_api.RestAPI;
import yelm.io.yelm.retrofit.new_api.RetrofitClientNew;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.user_address.controller.AddressesBottomSheet;

public class BasketActivityOnlyDelivery extends AppCompatActivity implements AddressesBottomSheet.AddressesBottomSheetListener {

    ActivityBasketOnlyDeliveryBinding binding;
    BasketAdapter basketAdapter;
    private final CompositeDisposable compositeDisposableBasket = new CompositeDisposable();
    UserAddress currentAddress;
    AddressesBottomSheet addressesBottomSheet = new AddressesBottomSheet();
    private BigDecimal deliveryCost = new BigDecimal("0");
    private BigDecimal finalCost = new BigDecimal("0");
    private String deliveryTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBasketOnlyDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentAddress = setDeliveryAddress();
        //check if there is user address in db - if there is not we just show basket and disable ordering button
        if (currentAddress != null) {
            checkBasket(currentAddress.latitude, currentAddress.longitude);
        } else {
            Log.d(AlexTAG.debug, "Method onStart() - currentAddress is null");
            binding.addressDelivery.setText(getText(R.string.basketActivitySelectAddress));
            setCompositeDisposableBasket();
            basketAdapter = new BasketAdapter(this, Common.basketCartRepository.getBasketCartsList());
            binding.recyclerCart.setAdapter(basketAdapter);
        }
    }

    synchronized private void checkBasket(String Lat, String Lon) {
        compositeDisposableBasket.clear();

        JSONArray jsonObjectItems = new JSONArray();
        List<BasketCart> basketCarts = Common.basketCartRepository.getBasketCartsList();
        try {
            JSONObject jsonObjectItem1 = new JSONObject();
            jsonObjectItem1
                    .put("id", "6137")
                    .put("count", "10");
            jsonObjectItems.put(jsonObjectItem1);

            JSONObject jsonObjectItem2 = new JSONObject();
            jsonObjectItem2
                    .put("id", "6138")
                    .put("count", "100");
            jsonObjectItems.put(jsonObjectItem2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RetrofitClientNew.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                checkBasket(
                        RestAPI.PLATFORM_NUMBER,
                        "11",
                        //Constants.ShopID,
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry(),
                        "55.78487156477965",
                        //Lat,
                        //Lon
                        "37.56303011869477",
                        jsonObjectItems.toString()
                ).
                enqueue(new Callback<BasketCheckResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<BasketCheckResponse> call, @NotNull final Response<BasketCheckResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                Log.d(AlexTAG.debug, "Method checkBasket() - PriceDelivery(): " + response.body().getDelivery().getPrice());
                                Log.d(AlexTAG.debug, "Method checkBasket() - TimeDelivery(): " + response.body().getDelivery().getTime());
                                Log.d(AlexTAG.debug, "Method checkBasket() - response.getDeletedID(): " + response.body().getDeletedId().toString());
                                binding.deliveryCost.setText(String.format("%s %s", response.body().getDelivery().getPrice(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
                                binding.time.setText(String.format("%s %s", response.body().getDelivery().getTime(), getText(R.string.delivery_time)));
                                deliveryTime = response.body().getDelivery().getTime();
                                deliveryCost = BigDecimal.valueOf(response.body().getDelivery().getPrice());

                                new Thread(() -> updateBasketCartsQuantity(response.body().getDeletedId())).start();

                            } else {
                                Log.e(AlexTAG.error, "Method checkBasket() - by some reason response is null!");
                            }
                        } else {
                            Log.e(AlexTAG.error, "Method checkBasket() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<BasketCheckResponse> call, @NotNull Throwable t) {
                        Log.e(AlexTAG.error, "Method checkBasket() - failure: " + t.toString());
                    }
                });
    }

    private void updateBasketCartsQuantity(List<DeletedId> deletedIDList) {

        for (BasketCart basketCart : Common.basketCartRepository.getBasketCartsList()) {
            basketCart.quantity = "9999";
            Common.basketCartRepository.updateBasketCart(basketCart);
        }

        for (DeletedId deletedId : deletedIDList) {
            BasketCart basketCart = Common.basketCartRepository.getBasketCartById(deletedId.getId());
            basketCart.quantity = deletedId.getAvailableCount();
            Common.basketCartRepository.updateBasketCart(basketCart);
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
        Log.d(AlexTAG.debug, "Method updateBasket() - carts.size(): " + carts.size() + "\n" +
                "finalCost: " + finalCost.toString());

        if (carts.size() != 0 && currentAddress != null && allowOrdering) {
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
            intent.putExtra("finalCost", finalCost.toString());
            intent.putExtra("deliveryCost", deliveryCost.toString());
            intent.putExtra("deliveryTime", deliveryTime);
            startActivity(intent);
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
        Log.d(AlexTAG.debug, "Method selectedAddress() - address: " + currentAddress.address);
        binding.addressDelivery.setText(String.format("%s", currentAddress.address));
        checkBasket(currentAddress.latitude, currentAddress.longitude);
    }

    @Override
    protected void onDestroy() {
        compositeDisposableBasket.clear();
        super.onDestroy();
    }
}