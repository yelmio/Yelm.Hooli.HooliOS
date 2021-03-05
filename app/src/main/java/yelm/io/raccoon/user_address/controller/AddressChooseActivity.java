package yelm.io.raccoon.user_address.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateSource;
import com.yandex.mapkit.map.Map;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import yelm.io.raccoon.support_stuff.Logging;
import yelm.io.raccoon.R;
import yelm.io.raccoon.database_new.Common;
import yelm.io.raccoon.database_new.user_addresses.UserAddress;
import yelm.io.raccoon.databinding.ActivityAddressChoiseBinding;

public class AddressChooseActivity extends AppCompatActivity {

    ActivityAddressChoiseBinding binding;
    SpringAnimation springAnimationUp, springAnimationDown;
    CameraPosition cameraPositionCurrent;
    private Address userSelectedAddress;

    Snackbar snackbar;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 3333;
    private static final float DEFAULT_ZOOM = 15f;
    double currentLatitude = 0.0;
    double currentLongitude = 0.0;
    boolean moveCameraFirst = false;

    Runnable task = this::getAddress;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(getString(R.string.yandex_maps_API_key));
        MapKitFactory.initialize(this);
        super.onCreate(savedInstanceState);
        binding = ActivityAddressChoiseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.mapView.getMap().addCameraListener(cameraListener);
        setImageSpringAnimation();

        binding.getLocation.setOnClickListener(view -> {
            if (cameraPositionCurrent != null) {
                String userStreet;
                userStreet =
                        (userSelectedAddress.getThoroughfare() == null ? "" : userSelectedAddress.getThoroughfare())
                                + (userSelectedAddress.getThoroughfare() != null && userSelectedAddress.getFeatureName() != null ? ", " : "")
                                + (userSelectedAddress.getFeatureName() == null ? "" : userSelectedAddress.getFeatureName());

                for (UserAddress userAddress : Common.userAddressesRepository.getUserAddressesList()) {
                    if (userAddress.isChecked) {
                        userAddress.isChecked = false;
                        Common.userAddressesRepository.updateUserAddresses(userAddress);
                        break;
                    }
                }

                if (Common.userAddressesRepository.getUserAddressByName(userStreet) == null) {
                    UserAddress userAddress = new UserAddress(
                            String.valueOf(cameraPositionCurrent.getTarget().getLatitude()),
                            String.valueOf(cameraPositionCurrent.getTarget().getLongitude()),
                            userStreet, true);
                    Common.userAddressesRepository.insertToUserAddresses(userAddress);
                    Log.d(Logging.debug, "userAddress: " + userAddress.toString());
                } else {
                    UserAddress userAddress = Common.userAddressesRepository.getUserAddressByName(userStreet);
                    userAddress.isChecked = true;
                    Common.userAddressesRepository.updateUserAddresses(userAddress);
                }
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(AddressChooseActivity.this, getText(R.string.waitForGeolocationetermination), Toast.LENGTH_SHORT).show();
            }
        });

        binding.currentButton.setOnClickListener(view -> {
            if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                binding.mapView.getMap().move(
                        new CameraPosition(new Point(currentLatitude, currentLongitude), DEFAULT_ZOOM, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, 3),
                        null);
            }
        });
    }


    private void getLocationPermission() {
        Log.d(Logging.debug, " Method getLocationPermission() - Getting location permission");
        if (hasLocationPermission()) {
            Log.d(Logging.debug, "Method getLocationPermission() - Location permission granted");
            getCurrentLocation();
            if (snackbar != null) {
                snackbar.dismiss();
            }
        } else {
            Log.d(Logging.debug, "Method getLocationPermission() - Location permission not granted");
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean hasLocationPermission() {
        int result = ContextCompat
                .checkSelfPermission(this.getApplicationContext(), LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000); //interval in which we want to get location
        locationRequest.setFastestInterval(4000); //if location is available sooner you can get it early
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(Logging.debug, "Call Request Permissions Result");
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (hasLocationPermission()) {
                    if (snackbar != null) {
                        snackbar.dismiss();
                    }
                    Log.d(Logging.debug, "Method onRequestPermissionsResult() - Request Permissions Result: Success!");
                    getCurrentLocation();
                } else if (shouldShowRequestPermissionRationale(permissions[0])) {
                    showDialogExplanationAboutRequestLocationPermission(getText(R.string.addressChooseActivityRequestPermission).toString());
                } else {
                    Log.d(Logging.debug, "Method onRequestPermissionsResult() - Request Permissions Result: Failed!");
                    showSnackBar();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }

    }

    private void showSnackBar() {
        snackbar = Snackbar.make(
                findViewById(R.id.frameRoot),
                R.string.addressChooseActivityRequestPermission,
                Snackbar.LENGTH_INDEFINITE);
        View view = snackbar.getView();
        final TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.dimens_16dp));
        tv.setMaxLines(5);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.TOP;
        view.setLayoutParams(params);
        snackbar.show();
    }

    private void showDialogExplanationAboutRequestLocationPermission(String message) {
        new AlertDialog.Builder(AddressChooseActivity.this)
                .setMessage(message)
                .setTitle(getText(R.string.mainActivityAttention))
                .setOnCancelListener(dialogInterface -> ActivityCompat.requestPermissions(AddressChooseActivity.this, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE))
                .setPositiveButton(getText(R.string.mainActivityYes), (dialogInterface, i) -> ActivityCompat.requestPermissions(AddressChooseActivity.this, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE))
                .create()
                .show();
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return super.shouldShowRequestPermissionRationale(permission);
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                //get current location
                currentLatitude = locationResult.getLastLocation().getLatitude();
                currentLongitude = locationResult.getLastLocation().getLongitude();
                Log.d(Logging.debug, "location updated:"
                        + "\nlatitude: " + currentLatitude
                        + "\nlongitude: " + currentLongitude);
                //move camera to current location once
                if (!moveCameraFirst) {
                    binding.mapView.getMap().move(
                            new CameraPosition(new Point(currentLatitude, currentLongitude), DEFAULT_ZOOM, 0.0f, 0.0f), new Animation(Animation.Type.SMOOTH, 3), null);
                }
                moveCameraFirst = true;
            }
        }
    };

    private void getAddress() {
        try {
            Geocoder geocoder = new Geocoder(AddressChooseActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(cameraPositionCurrent.getTarget().getLatitude(),
                    cameraPositionCurrent.getTarget().getLongitude(), 1);
            if (addresses.size() > 0) {
                userSelectedAddress = addresses.get(0);
                Log.d(Logging.debug, "userAddress: " + userSelectedAddress.getThoroughfare() + userSelectedAddress.getFeatureName());
                runOnUiThread(() -> {
                    String userStreet =
                            (userSelectedAddress.getThoroughfare() == null ? "" : userSelectedAddress.getThoroughfare())
                                    + (userSelectedAddress.getThoroughfare() != null && userSelectedAddress.getFeatureName() != null ? ", " : "")
                                    + (userSelectedAddress.getFeatureName() == null ? "" : userSelectedAddress.getFeatureName());
                    binding.userAddress.setText(userStreet);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setImageSpringAnimation() {
        springAnimationUp = new SpringAnimation(binding.animatedLayout, DynamicAnimation.TRANSLATION_Y);
        SpringForce springForceUp = new SpringForce();
        springForceUp.setStiffness(SpringForce.STIFFNESS_VERY_LOW);
        springForceUp.setFinalPosition(-250f);
        springForceUp.setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        springAnimationUp.setSpring(springForceUp);

        springAnimationDown = new SpringAnimation(binding.animatedLayout, DynamicAnimation.TRANSLATION_Y);
        SpringForce springForceDown = new SpringForce();
        springForceDown.setStiffness(SpringForce.STIFFNESS_VERY_LOW);
        springForceDown.setFinalPosition(0f);
        springForceDown.setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        springAnimationDown.setSpring(springForceDown);
    }

    @Override
    protected void onStop() {
        binding.mapView.onStop();
        MapKitFactory.getInstance().onStop();
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        binding.mapView.onStart();
        getLocationPermission();
    }
}