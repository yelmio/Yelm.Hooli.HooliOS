package yelm.io.yelm.old_version.maps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateSource;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.pickup_address.controller.BottomSheetShop;
import yelm.io.yelm.retrofit.API;
import yelm.io.yelm.retrofit.DynamicURL;
import yelm.io.yelm.retrofit.RetrofitClient;

public class MapShopActivity extends AppCompatActivity implements BottomSheetShop.BottomSheetShopListener {

    private MapView mapView;
    private MapObjectCollection mapObjects;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 3333;
    private static final float DEFAULT_ZOOM = 10f;
    CameraPosition cameraPositionCurrent;

    private final CameraListener cameraListener = new CameraListener() {
        @Override
        public void onCameraPositionChanged(@NonNull Map map,
                                            @NonNull final CameraPosition cameraPosition,
                                            @NonNull CameraUpdateSource cameraUpdateSource,
                                            boolean finished) {
            if (finished) {
                cameraPositionCurrent = cameraPosition;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(getString(R.string.yandex_maps_API_key));
        MapKitFactory.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_shop);
        mapView = findViewById(R.id.mapView);
        getLocationPermission();

        mapObjects = mapView.getMap().getMapObjects().addCollection();
        initShops();
    }

    private void getLocationPermission() {
        Log.d("AlexDebug", "Getting location permission");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("AlexDebug", "Call Request Permissions Result");
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d("AlexDebug", "Request Permissions Result: Failed!");
                            return;
                        }
                    }
                    Log.d("AlexDebug", "Request Permissions Result: Success!");
                    getLocationPermission();
                }
        }
    }


    private void getCurrentLocation() {
        Log.d("AlexDebug", "Getting Current Location");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Log.d("AlexDebug", "Getting fusedLocationProviderClient");
        mapView.getMap().addCameraListener(cameraListener);
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                final Location location = task.getResult();
                if (task.isSuccessful()) {
                    if (location != null) {
                        Log.d("AlexDebug", "Move camera to start");
                        mapView.getMap().move(
                                new CameraPosition(new Point(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM, 0.0f, 0.0f), new Animation(Animation.Type.SMOOTH, 2), null);
                    } else {
                        Log.d("AlexDebug", "location = null");
                    }
                } else {
                    Log.d("AlexDebug", "Current location is null. Using defaults.");
                    Log.e("AlexDebug", "Exception: %s", task.getException());
                    //map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                }
            }
        });
    }

    private void initShops() {
        Log.d("AlexDebug", "Map: initShops");
        RetrofitClient.
                getClient(API.URL_API_MAIN).
                create(API.class).
                getShops(DynamicURL.getURL(API.URL_API_GET_SHOPS)).
                enqueue(new Callback<ArrayList<ShopClass>>() {
                    @Override
                    public void onResponse(Call<ArrayList<ShopClass>> call, Response<ArrayList<ShopClass>> response) {
                        if (response.isSuccessful()) {
                            Log.d("AlexDebug", "response.body().size(): " + response.body().size());
                            for (int i = 0; i < response.body().size(); i++) {
                                PlacemarkMapObject mark = mapObjects
                                        .addPlacemark(new Point(Double.valueOf(response.body().get(i).getLatitude()),
                                                Double.valueOf(response.body().get(i).getLongitude())));
                                mark.setOpacity(0.8f);
                                mark.setIcon(ImageProvider.fromResource(MapShopActivity.this, R.drawable.search_result));
                                mark.setDraggable(true);
                                mark.setUserData(response.body().get(i));
                                mark.addTapListener(markMapObjectTapListener);
                            }
                        } else {
                            Log.e("AlexDebug", "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<ShopClass>> call, Throwable t) {
                    }
                });
    }

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
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    public void onClicked(String address, String id) {
        Log.d("AlexDebug", "pickupAddress: " + address);
        Intent intent = new Intent();
        intent.putExtra("pickupAddress", address);
        setResult(RESULT_OK, intent);
        finish();

    }
}