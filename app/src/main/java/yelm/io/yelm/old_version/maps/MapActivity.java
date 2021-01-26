package yelm.io.yelm.old_version.maps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import yelm.io.yelm.R;


public class MapActivity extends AppCompatActivity {

    SpringAnimation springAnimationUp, springAnimationDown;
    TextView addressTextView;
    ImageButton currentButton;
    ConstraintLayout animatedLayout;
    CameraPosition cameraPositionCurrent;
    CardView getLocation;
    private MapObjectCollection mapObjects;
    private UserLocationLayer userLocationLayer;

    private static Address userCurrentAddress;
    private static Address userSelectedAddress;

    private MapView mapView;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 3333;
    private static final float DEFAULT_ZOOM = 15f;

    private final CameraListener cameraListener = new CameraListener() {
        @Override
        public void onCameraPositionChanged(@NonNull Map map,
                                            @NonNull final CameraPosition cameraPosition,
                                            @NonNull CameraUpdateSource cameraUpdateSource,
                                            boolean finished) {
            if (finished) {
                cameraPositionCurrent = cameraPosition;
                new Thread(task).start();
                springAnimationUp.cancel();
                springAnimationDown.start();

            } else {
                springAnimationDown.cancel();
                springAnimationUp.start();
            }
        }
    };

    Runnable task = new Runnable() {
        @Override
        public void run() {
            getAddress();
        }
    };

    private void getAddress(){
        try {
            Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(cameraPositionCurrent.getTarget().getLatitude(),
                    cameraPositionCurrent.getTarget().getLongitude(), 1);
            if (addresses.size() > 0) {
                userSelectedAddress = addresses.get(0);
                Log.d("AlexDebug", "userChosenAddress: " + userSelectedAddress.getAddressLine(0));
                runOnUiThread(() -> addressTextView.setText(userSelectedAddress.getAddressLine(0)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(getString(R.string.yandex_maps_API_key));
        MapKitFactory.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initViews();
        getLocationPermission();
    }


    private void initViews() {
        mapView = findViewById(R.id.mapView);
        mapView.getMap().setRotateGesturesEnabled(false);
        currentButton = findViewById(R.id.currentButton);
        addressTextView = findViewById(R.id.userAddress);
        animatedLayout = findViewById(R.id.animatedLayout);
        getLocation = findViewById(R.id.getLocation);
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("addressLine", userSelectedAddress == null ? "" : userSelectedAddress.getAddressLine(0));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        setImageSpringAnimation();
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
                                new CameraPosition(new Point(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM, 0.0f, 0.0f), new Animation(Animation.Type.SMOOTH, 3), null);

                        mapObjects = mapView.getMap().getMapObjects().addCollection();
                        currentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mapView.getMap().move(
                                        new CameraPosition(new Point(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM, 0.0f, 0.0f),
                                        new Animation(Animation.Type.SMOOTH, 2),
                                        null);
                            }
                        });
                        try {
                            Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                                    location.getLongitude(), 1);
                            if (addresses.size() > 0) {
                                userCurrentAddress = addresses.get(0);
                                userSelectedAddress = userCurrentAddress;
                                Log.d("AlexDebug", "Address: " + userCurrentAddress.getAddressLine(0));
                                addressTextView.setText(userCurrentAddress.getAddressLine(0));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Log.d("AlexDebug", "location = null");
                    }
                } else {
                    Log.d("AlexDebug", "Current location is null. Using defaults.");
                    Log.e("AlexDebug", "Exception: %s", task.getException());
                }
            }
        });
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

    private void setImageSpringAnimation() {
        springAnimationUp = new SpringAnimation(animatedLayout, DynamicAnimation.TRANSLATION_Y);
        SpringForce springForceUp = new SpringForce();
        springForceUp.setStiffness(SpringForce.STIFFNESS_VERY_LOW);
        springForceUp.setFinalPosition(-250f);
        springForceUp.setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        springAnimationUp.setSpring(springForceUp);

        springAnimationDown = new SpringAnimation(animatedLayout, DynamicAnimation.TRANSLATION_Y);
        SpringForce springForceDown = new SpringForce();
        springForceDown.setStiffness(SpringForce.STIFFNESS_VERY_LOW);
        springForceDown.setFinalPosition(0f);
        springForceDown.setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        springAnimationDown.setSpring(springForceDown);
    }

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

}