package yelm.io.yelm.pickup_address.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.runtime.image.ImageProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.databinding.ActivityAddressPickupChooseBinding;
import yelm.io.yelm.old_version.maps.ShopClass;
import yelm.io.yelm.retrofit.API;
import yelm.io.yelm.retrofit.DynamicURL;
import yelm.io.yelm.retrofit.RetrofitClient;
import yelm.io.yelm.support_stuff.Logging;

public class AddressPickupChooseActivity extends AppCompatActivity implements BottomSheetShop.BottomSheetShopListener {

    ActivityAddressPickupChooseBinding binding;

    private MapObjectCollection mapObjects;

    private MapObjectTapListener markMapObjectTapListener = (mapObject, point) -> {
        if (mapObject instanceof PlacemarkMapObject) {
            PlacemarkMapObject mark = (PlacemarkMapObject) mapObject;
            Object userDataObject = mark.getUserData();
            if (userDataObject instanceof ShopClass) {
                ShopClass userData = (ShopClass) userDataObject;
                BottomSheetShop bottomSheetShop = new BottomSheetShop(userData);
                bottomSheetShop.show(getSupportFragmentManager(), "shopBottomSheet");
            }
        }
        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(getString(R.string.yandex_maps_API_key));
        MapKitFactory.initialize(this);
        super.onCreate(savedInstanceState);
        binding = ActivityAddressPickupChooseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapObjects = binding.mapView.getMap().getMapObjects().addCollection();

        initShops();

    }

    private void initShops() {
        Log.d(Logging.debug, "Map: initShops");
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getShops(DynamicURL.getURL(API.URL_API_GET_SHOPS)).
                enqueue(new Callback<ArrayList<ShopClass>>() {
                    @Override
                    public void onResponse(@NotNull Call<ArrayList<ShopClass>> call, @NotNull Response<ArrayList<ShopClass>> response) {
                        if (response.isSuccessful()) {
                            Log.d("AlexDebug", "response.body().size(): " + response.body().size());
                            for (int i = 0; i < response.body().size(); i++) {
                                PlacemarkMapObject mark = mapObjects
                                        .addPlacemark(new Point(Double.parseDouble(response.body().get(i).getLatitude()),
                                                Double.parseDouble(response.body().get(i).getLongitude())));
                                mark.setOpacity(0.8f);
                                mark.setIcon(ImageProvider.fromResource(AddressPickupChooseActivity.this, R.drawable.search_result));
                                mark.setDraggable(true);
                                mark.setUserData(response.body().get(i));
                                mark.addTapListener(markMapObjectTapListener);
                            }
                        } else {
                            Log.e("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ArrayList<ShopClass>> call, @NotNull Throwable t) {
                    }
                });
    }

    @Override
    public void onClicked(String address, String id) {
        Intent intent = new Intent();
        intent.putExtra("pickupShopAddress", address);
        intent.putExtra("pickupShopID", id);
        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    protected void onStop() {
        binding.mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        binding.mapView.onStart();
    }


}