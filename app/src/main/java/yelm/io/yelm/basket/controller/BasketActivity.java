package yelm.io.yelm.basket.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.pickup_address.controller.AddressPickupChooseActivity;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.R;
import yelm.io.yelm.basket.model.BasketCheckResponse;
import yelm.io.yelm.basket.adapter.BasketAdapter;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.ActivityBasketBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.Modifier;
import yelm.io.yelm.order.OrderActivityNew;
import yelm.io.yelm.retrofit.new_api.RestAPI;
import yelm.io.yelm.retrofit.new_api.RetrofitClientNew;
import yelm.io.yelm.constants.Constants;
import yelm.io.yelm.user_address.controller.AddressesBottomSheet;

public class BasketActivity extends AppCompatActivity implements AddressesBottomSheet.AddressesBottomSheetListener {

    ActivityBasketBinding binding;
    BasketAdapter basketAdapter;
    private final CompositeDisposable compositeDisposableBasket = new CompositeDisposable();
    UserAddress currentAddress;
    AddressesBottomSheet addressesBottomSheet = new AddressesBottomSheet();
    private BigDecimal deliveryCost = new BigDecimal("0");
    private BigDecimal finalCost = new BigDecimal("0");
    private String methodDelivery = "delivery";
    private String deliveryTime = "";
    private String pickupShopAddress = "";
    private String pickupShopID = "";

    private static final int PICKUP_ADDRESS_REQUEST_CODE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBasketBinding.inflate(getLayoutInflater());
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

    private void binding() {
        basketAdapter = new BasketAdapter(this, Common.basketCartRepository.getBasketCartsList());
        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerCart.setAdapter(basketAdapter);

        Picasso.get()
                .load(R.drawable.drone)
                .noPlaceholder()
                .centerCrop()
                .resize(400, 400)
                .into(binding.imageDelivery);

        Picasso.get()
                .load(R.drawable.pickup)
                .noPlaceholder()
                .centerCrop()
                .resize(400, 400)
                .into(binding.imagePickup);

        binding.back.setOnClickListener(v -> finish());
        binding.cleanBasket.setOnClickListener(v -> {
            binding.deliveryCost.setText("");
            deliveryCost = new BigDecimal("0");
            Common.basketCartRepository.emptyBasketCart();
            binding.ordering.setEnabled(false);
        });

        binding.layoutDelivery.setOnClickListener(v -> {
            binding.layoutDelivery.setBackgroundResource(R.drawable.card_stroke_selected);
            binding.layoutPickup.setBackgroundResource(R.drawable.card_stroke_unselected);
            binding.layoutPickupCost.setVisibility(View.GONE);
            binding.layoutDeliveryCost.setVisibility(View.VISIBLE);
            if (!methodDelivery.equals("delivery")) {
                methodDelivery = "delivery";
                updateBasket(Common.basketCartRepository.getBasketCartsList());
            }
        });

        binding.layoutPickup.setOnClickListener(v -> {
            binding.layoutPickup.setBackgroundResource(R.drawable.card_stroke_selected);
            binding.layoutDelivery.setBackgroundResource(R.drawable.card_stroke_unselected);
            binding.layoutPickupCost.setVisibility(View.VISIBLE);
            binding.layoutDeliveryCost.setVisibility(View.GONE);
            if (!methodDelivery.equals("pickup")) {
                if (pickupShopAddress.isEmpty()) {
                    binding.ordering.setEnabled(false);
                }
                methodDelivery = "pickup";
                finalCost = finalCost.subtract(deliveryCost);
                binding.finalPrice.setText(String.format("%s %s", finalCost, LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
            }
        });

        binding.addressDelivery.setOnClickListener(v -> callAddressesBottomSheet());

        binding.ordering.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderActivityNew.class);
            intent.putExtra("finalCost", finalCost.toString());
            intent.putExtra("methodDelivery", methodDelivery);
            intent.putExtra("deliveryCost", deliveryCost.toString());
            intent.putExtra("deliveryTime", deliveryTime);
            startActivity(intent);
        });

        binding.addressDelivery.setOnClickListener(v -> startActivityForResult(new Intent(BasketActivity.this, AddressPickupChooseActivity.class), PICKUP_ADDRESS_REQUEST_CODE));


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case PICKUP_ADDRESS_REQUEST_CODE:
                pickupShopAddress = data.getStringExtra("pickupShopAddress");
                pickupShopID = data.getStringExtra("pickupShopID");
                break;

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

    private void checkBasket(String Lat, String Lon) {
        StringBuilder items = getItemsForCheckBasket(Common.basketCartRepository.getBasketCartsList());
        Log.d(AlexTAG.debug, "Method checkBasket() - items: " + items);
        RetrofitClientNew.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                checkBasket(
                        items.toString(),
                        RestAPI.PLATFORM_NUMBER,
                        Constants.ShopID,
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry(),
                        Lat,
                        Lon
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        compositeDisposableBasket.clear();
        super.onDestroy();
    }


    private void updateBasket(List<BasketCart> carts) {
        basketAdapter = new BasketAdapter(this, carts);
        finalCost = new BigDecimal("0");
        boolean allCartsExist = true;
        for (BasketCart cart : carts) {
            if (!cart.isExist) {
                allCartsExist = false;
                binding.ordering.setEnabled(false);
            }
            BigDecimal costCurrentCart = new BigDecimal(cart.finalPrice);
            for (Modifier modifier : cart.modifier) {
                costCurrentCart = costCurrentCart.add(new BigDecimal(modifier.getValue()));
            }
            costCurrentCart = costCurrentCart.multiply(new BigDecimal(cart.count));
            finalCost = finalCost.add(costCurrentCart);
        }

        if (methodDelivery.equals("delivery")) {
            finalCost = finalCost.add(deliveryCost);
        }
        binding.finalPrice.setText(String.format("%s %s", finalCost, LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
        binding.recyclerCart.setAdapter(basketAdapter);
        Log.d(AlexTAG.debug, "Method updateBasket() - carts.size(): " + carts.size() + "\n" +
                "finalCost: " + finalCost.toString());
        binding.ordering.setEnabled(allCartsExist);

    }

    @Override
    public void selectedAddress(UserAddress userAddress) {
        Log.d(AlexTAG.debug, "Method selectedAddress() - address: " + userAddress.address);
        binding.addressDelivery.setText(String.format("%s", userAddress.address));
        checkBasket(userAddress.latitude, userAddress.longitude);
    }
}