package yelm.io.yelm.basket.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import yelm.io.yelm.constants.Constants;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.ActivityBasketOnlyDeliveryBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.Modifier;
import yelm.io.yelm.order.OrderActivityNew;
import yelm.io.yelm.pickup_address.controller.AddressPickupChooseActivity;
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
        setDeliveryAddress();

        for (UserAddress userAddress : Common.userAddressesRepository.getUserAddressesList()) {
            if (userAddress.isChecked) {
                checkBasket(userAddress.latitude, userAddress.longitude);
                break;
            }
        }

        compositeDisposableBasket.
                add(Common.basketCartRepository.
                        getBasketCarts().
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribeOn(Schedulers.io()).
                        subscribe(this::updateBasket));
    }

    private void binding() {
        basketAdapter = new BasketAdapter(this, Common.basketCartRepository.getBasketCartsList());
        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerCart.setAdapter(basketAdapter);

        binding.back.setOnClickListener(v -> finish());

        binding.cleanBasket.setOnClickListener(v -> {
            binding.deliveryCost.setText("");
            deliveryCost = new BigDecimal("0");
            Common.basketCartRepository.emptyBasketCart();
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

    public void setDeliveryAddress() {
        for (UserAddress current : Common.userAddressesRepository.getUserAddressesList()) {
            if (current.isChecked) {
                currentAddress = current;
                binding.addressDelivery.setText(currentAddress.address);
            }
        }
    }

    private StringBuilder getItemsForCheckBasket(List<BasketCart> basketCarts) {
        StringBuilder items = new StringBuilder();
        items.append("[");
        String comma = "";
        for (BasketCart basketCart : basketCarts) {
            items.append(comma);
            items.append(basketCart.itemID);
            comma = ",";
        }
        items.append("]");
        return items;
    }

    synchronized private void checkBasket(String Lat, String Lon) {
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

//            for (int i = 0; i < basketCarts.size(); i++) {
//                JSONObject jsonObjectItem = new JSONObject();
//                jsonObjectItem
//                        .put("id", basketCarts.get(i).itemID)
//                        .put("count", basketCarts.get(i).count);
//                jsonObjectItems.put(jsonObjectItem);
//            }
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

                                //updateBasketCartsForExist(response.body().getDeletedID());
                            } else {
                                Log.e(AlexTAG.error, "Method checkBasket() - by some resason response is null!");
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

    private void updateBasketCartsForExist(List<String> deletedIDList) {
        boolean updated = false;
        for (String deletedID : deletedIDList) {
            for (BasketCart basketCart : Common.basketCartRepository.getBasketCartsList()) {
                if (basketCart.itemID.equals(deletedID)) {
                    basketCart.isExist = false;
                    Common.basketCartRepository.updateBasketCart(basketCart);
                    updated = true;
                } else {
                    if (!basketCart.isExist) {
                        basketCart.isExist = true;
                        Common.basketCartRepository.updateBasketCart(basketCart);
                        updated = true;
                    }
                }
            }
        }
        if (!updated) {
            updateBasket(Common.basketCartRepository.getBasketCartsList());
        }
    }

    private void updateBasket(List<BasketCart> carts) {

        binding.emptyTextView.setVisibility(carts.size() == 0 ? View.VISIBLE : View.GONE);
        binding.layoutDeliveryCost.setVisibility(carts.size() == 0 ? View.GONE : View.VISIBLE);

        basketAdapter = new BasketAdapter(this, carts);
        finalCost = new BigDecimal("0");
        boolean allCartsExist = true;
        for (BasketCart cart : carts) {
            if (!cart.isExist) {
                allCartsExist = false;
            }
            BigDecimal costCurrentCart = new BigDecimal(cart.finalPrice);
            for (Modifier modifier : cart.modifier) {
                costCurrentCart = costCurrentCart.add(new BigDecimal(modifier.getValue()));
            }
            costCurrentCart = costCurrentCart.multiply(new BigDecimal(cart.count));
            finalCost = finalCost.add(costCurrentCart);
        }

        binding.finalPrice.setText(String.format("%s %s", finalCost, LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        binding.recyclerCart.setAdapter(basketAdapter);
        Log.d(AlexTAG.debug, "Method updateBasket() - carts.size(): " + carts.size() + "\n" +
                "finalCost: " + finalCost.toString());
        if (carts.size() == 0) {
            allCartsExist = false;
        }
        binding.ordering.setEnabled(allCartsExist);
    }

    @Override
    public void selectedAddress(UserAddress userAddress) {
        Log.d(AlexTAG.debug, "Method selectedAddress() - address: " + userAddress.address);
        binding.addressDelivery.setText(String.format("%s", userAddress.address));
        checkBasket(userAddress.latitude, userAddress.longitude);
    }
}