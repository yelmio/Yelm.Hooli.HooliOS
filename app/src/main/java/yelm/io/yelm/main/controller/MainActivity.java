package yelm.io.yelm.main.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.basket.controller.BasketActivityOnlyDelivery;
import yelm.io.yelm.main.news.NewNews;
import yelm.io.yelm.main.news.NewsFromNotificationActivity;
import yelm.io.yelm.constants.Logging;
import yelm.io.yelm.search.SearchActivity;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.ActivityMainBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.CatalogsWithProductsClass;
import yelm.io.yelm.main.model.Modifier;
import yelm.io.yelm.retrofit.RestAPI;
import yelm.io.yelm.retrofit.RetrofitClient;
import yelm.io.yelm.constants.Constants;
import yelm.io.yelm.user_address.controller.AddressesBottomSheet;
import yelm.io.yelm.chat.controller.ChatActivity;
import yelm.io.yelm.R;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.main.news.NewsAdapter;
import yelm.io.yelm.support_stuff.ItemOffsetDecorationRight;

public class MainActivity extends AppCompatActivity implements AddressesBottomSheet.AddressesBottomSheetListener {

    public BadgeDrawable badge;

    ActivityMainBinding binding;
    AddressesBottomSheet addressesBottomSheet = new AddressesBottomSheet();

    private ArrayList<CatalogsWithProductsClass> catalogsWithProductsList = new ArrayList<>();
    private NewsAdapter newsAdapter;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 7777;

    private final CompositeDisposable compositeDisposableBasket = new CompositeDisposable();

    private boolean allowUpdateUI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding();
        //getCategoriesWithProducts("0", "0");
        initNews();
        //getLocationPermission();
        getAppToken();


//        Bundle args = getIntent().getExtras();
//        if (args != null) {
//            Log.d(Logging.debug, "MainActivity - Notification data: " + args.getString("data"));
//            String data = args.getString("data");
//            if (data != null && !data.isEmpty()) {
//                try {
//                    JSONObject jsonObj = new JSONObject(data);
//                    Log.d(Logging.debug, "jsonObj: " + jsonObj.getString("id"));
//                    Log.d(Logging.debug, "jsonObj: " + jsonObj.getString("name"));
//                    if (jsonObj.getString("name").equals("news")) {
//                        Intent intent = new Intent(MainActivity.this, NewsFromNotificationActivity.class);
//                        intent.putExtra("id", jsonObj.getString("id"));
//                        startActivity(intent);
//                    } else if (jsonObj.getString("name").equals("items")) {
//                        //Intent intent = new Intent(MainActivity.this, NewsFromNotificationActivity.class);
//                        //intent.putExtra("id", jsonObj.getString("id"));
//                        //startActivity(intent);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }


    }

    private void binding() {
        binding.chat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        binding.recyclerCards.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerCards.setHasFixedSize(false);
        binding.recyclerCards.addItemDecoration(new ItemOffsetDecorationRight((int) getResources().getDimension(R.dimen.dimens_16dp)));
        binding.addressLayout.setOnClickListener(v -> callAddressesBottomSheet());
        binding.userCurrentAddress.setOnClickListener(v -> callAddressesBottomSheet());
        binding.search.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.basket.setOnClickListener(v -> startActivity(new Intent(this, BasketActivityOnlyDelivery.class)));
    }

    private void callAddressesBottomSheet() {
        //check if AddressesBottomSheet is added otherwise we get exception:
        //java.lang.IllegalStateException: Fragment already added
        if (!addressesBottomSheet.isAdded()) {
            addressesBottomSheet.show(getSupportFragmentManager(), "addressBottomSheet");
        }
    }

    private void getLocationPermission() {
        if (hasLocationPermission()) {
            Log.d(Logging.debug, "Method getLocationPermission() - Location permission granted");
            getUserCurrentLocation();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (hasLocationPermission()) {
                    Log.d(Logging.debug, "Method onRequestPermissionsResult() - Request Permissions Result: Success!");
                    getUserCurrentLocation();
                } else if (shouldShowRequestPermissionRationale(permissions[0])) {
                    showDialogExplanationAboutRequestLocationPermission(getText(R.string.mainActivityRequestPermission).toString());
                } else {
                    Log.d(Logging.debug, "Method onRequestPermissionsResult() - Request Permissions Result: Failed!");
                    performIfNoLocationPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    private void performIfNoLocationPermission() {
        Log.d(Logging.debug, "Method performIfNoLocationPermission()");
        binding.progress.setVisibility(View.GONE);
        if (Common.userAddressesRepository.getUserAddressesList() != null && Common.userAddressesRepository.getUserAddressesList().size() != 0) {
            for (UserAddress userAddress : Common.userAddressesRepository.getUserAddressesList()) {
                if (userAddress.isChecked) {
                    binding.userCurrentAddress.setText(userAddress.address);
                    getCategoriesWithProducts(userAddress.latitude, userAddress.longitude);
                    break;
                }
            }
        } else {
            binding.userCurrentAddress.setText(getText(R.string.choose_address));
        }
    }

    private void showDialogExplanationAboutRequestLocationPermission(String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setTitle(getText(R.string.mainActivityAttention))
                .setOnCancelListener(dialogInterface -> ActivityCompat.requestPermissions(MainActivity.this, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE))
                .setPositiveButton(getText(R.string.mainActivityOk), (dialogInterface, i) -> ActivityCompat.requestPermissions(MainActivity.this, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE))
                .create()
                .show();
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return super.shouldShowRequestPermissionRationale(permission);
    }

    private void getUserCurrentLocation() {
        Log.d(Logging.debug, "Method getUserCurrentLocation()");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
        locationRequest.setInterval(0);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            performIfNoLocationPermission();
        } else {
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            binding.progress.setVisibility(View.GONE);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                Log.d("AlexDebug", "location updated:" + "\nlatitude: " + latitude + "\nlongitude: " + longitude);
                String userStreet = getUserStreet(locationResult.getLastLocation());
                Log.d(Logging.debug, "Method getUserCurrentLocation() - userStreet: " + userStreet);

                if (userStreet.trim().isEmpty()) {
                    performIfNoLocationPermission();
                    return;
                }

                for (UserAddress userAddress : Common.userAddressesRepository.getUserAddressesList()) {
                    if (userAddress.isChecked) {
                        userAddress.isChecked = false;
                        Common.userAddressesRepository.updateUserAddresses(userAddress);
                        break;
                    }
                }
                UserAddress currentUserAddress = Common.userAddressesRepository.getUserAddressByName(userStreet);
                if (currentUserAddress == null) {
                    UserAddress userAddress = new UserAddress(
                            String.valueOf(latitude),
                            String.valueOf(longitude),
                            userStreet, true);
                    Common.userAddressesRepository.insertToUserAddresses(userAddress);
                    binding.userCurrentAddress.setText(userAddress.address);
                } else {
                    currentUserAddress.isChecked = true;
                    Common.userAddressesRepository.updateUserAddresses(currentUserAddress);
                    binding.userCurrentAddress.setText(currentUserAddress.address);
                }
                getCategoriesWithProducts(String.valueOf(latitude),
                        String.valueOf(longitude));
            } else {
                Log.d(Logging.debug, "Method getUserCurrentLocation() - location null - unexpected error");
                performIfNoLocationPermission();
            }
        }
    };

    private String getUserStreet(Location location) {
        String userStreet = "";
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.size() > 0) {
                Address userCurrentAddress = addresses.get(0);
                Log.d("AlexDebug", "userChosenAddress: " + userCurrentAddress.getAddressLine(0));
                Log.d("AlexDebug", "getFeatureName: " + userCurrentAddress.getFeatureName());
                Log.d("AlexDebug", "getThoroughfare: " + userCurrentAddress.getThoroughfare());
                userStreet =
                        (userCurrentAddress.getThoroughfare() == null ? "" : userCurrentAddress.getThoroughfare())
                                + (userCurrentAddress.getThoroughfare() != null && userCurrentAddress.getFeatureName() != null ? ", " : "")
                                + (userCurrentAddress.getFeatureName() == null ? "" : userCurrentAddress.getFeatureName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userStreet;
    }

    private void getCategoriesWithProducts(String Lat, String Lon) {
        RetrofitClient.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                getCategoriesWithProducts(
                        "3",
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry(),
                        RestAPI.PLATFORM_NUMBER,
                        Lat,
                        Lon).
                enqueue(new Callback<ArrayList<CatalogsWithProductsClass>>() {
                    @Override
                    public void onResponse(@NotNull Call<ArrayList<CatalogsWithProductsClass>> call, @NotNull final Response<ArrayList<CatalogsWithProductsClass>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                catalogsWithProductsList = response.body();
                                if (catalogsWithProductsList.size() != 0) {
                                    Constants.ShopID = catalogsWithProductsList.get(0).getShopID();
                                }
                                Log.d(Logging.debug, "Method getCategoriesWithProducts() - Constants.ShopID: " + Constants.ShopID);
                                redrawProducts();
                            } else {
                                Log.e(Logging.error, "Method getCategoriesWithProducts() - by some reason response is null!");
                            }
                        } else {
                            Log.e(Logging.error, "Method getCategoriesWithProducts() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ArrayList<CatalogsWithProductsClass>> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method getCategoriesWithProducts() - failure: " + t.toString());
                    }
                });
    }

    private void getAppToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(Logging.error, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();

                    Log.d(Logging.debug, "FCM: token:" + token);
                });
    }

    @Override
    public void selectedAddress(UserAddress selectedUserAddress) {
        Log.d(Logging.debug, "Method selectedAddress() - address: " + selectedUserAddress.address);
        binding.userCurrentAddress.setText(selectedUserAddress.address);
        getCategoriesWithProducts(selectedUserAddress.latitude, selectedUserAddress.longitude);
    }

    private void initNews() {
        RetrofitClient.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                getNews("3",
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry(),
                        RestAPI.PLATFORM_NUMBER).
                enqueue(new Callback<ArrayList<NewNews>>() {
                    @Override
                    public void onResponse(@NotNull Call<ArrayList<NewNews>> call, @NotNull final Response<ArrayList<NewNews>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                List<NewNews> newsList = response.body();
                                newsAdapter = new NewsAdapter(MainActivity.this, newsList);
                                binding.recyclerCards.setAdapter(newsAdapter);
                            } else {
                                Log.e(Logging.error, "Method initNews() - by some reason response is null!");
                            }
                        } else {
                            Log.e(Logging.error, "Method initNews() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ArrayList<NewNews>> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method initNews() - failure: " + t.toString());
                    }
                });
    }

    @Override
    public void onStop() {
        compositeDisposableBasket.clear();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("AlexDebug", "allowUpdateUI: " + allowUpdateUI);
        updateCost();
        if (allowUpdateUI) {
            redrawProducts();
        }
        allowUpdateUI = true;
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
                            BigDecimal costCurrentCart = new BigDecimal(cart.finalPrice);
                            for (Modifier modifier : cart.modifier) {
                                costCurrentCart = costCurrentCart.add(new BigDecimal(modifier.getValue()));
                            }
                            costCurrentCart = costCurrentCart.multiply(new BigDecimal(cart.count));
                            basketPrice = basketPrice.add(costCurrentCart);
                        }

                        binding.basket.setText(String.format("%s %s", basketPrice.toString(), LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
                        Log.d(Logging.debug, "Method updateCost() - carts.size(): " + carts.size() + "\n" +
                                "basketPrice.toString(): " + basketPrice.toString());
                    }
                }));
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
        //super.onSaveInstanceState(outState);
    }

    synchronized private void redrawProducts() {
        Log.d(Logging.debug, "Method redrawProducts()");
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (!Objects.equals(fragment.getTag(), "addressBottomSheet")) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
            }
        }
        binding.storeFragments.removeAllViews();
        for (int i = 0; i < catalogsWithProductsList.size(); i++) {
            FrameLayout frameLayout = new FrameLayout(MainActivity.this);
            frameLayout.setId(View.generateViewId());
            CategoryFragment categoryFragment = new CategoryFragment(catalogsWithProductsList.get(i));
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(frameLayout.getId(), categoryFragment, "fragment: " + i).commitAllowingStateLoss();
            binding.storeFragments.addView(frameLayout);
        }
        View footer = new View(MainActivity.this);
        footer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.dimen_70dp)));
        binding.storeFragments.addView(footer);
    }
}