package yelm.io.raccoon.main.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

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
import yelm.io.raccoon.basket.controller.BasketActivity;
import yelm.io.raccoon.item.ItemFromNotificationActivity;
import yelm.io.raccoon.main.categories.CategoriesAdapter;
import yelm.io.raccoon.main.categories.CategoriesPOJO;
import yelm.io.raccoon.main.news.NewNews;
import yelm.io.raccoon.main.news.NewsFromNotificationActivity;
import yelm.io.raccoon.rest.query.RestMethods;
import yelm.io.raccoon.support_stuff.Logging;
import yelm.io.raccoon.search.SearchActivity;
import yelm.io.raccoon.database_new.basket_new.BasketCart;
import yelm.io.raccoon.database_new.user_addresses.UserAddress;
import yelm.io.raccoon.databinding.ActivityMainBinding;
import yelm.io.raccoon.loader.controller.LoaderActivity;
import yelm.io.raccoon.main.model.CategoriesWithProductsClass;
import yelm.io.raccoon.main.model.Modifier;
import yelm.io.raccoon.rest.rest_api.RestAPI;
import yelm.io.raccoon.rest.client.RetrofitClient;
import yelm.io.raccoon.constants.Constants;
import yelm.io.raccoon.support_stuff.StaticRepository;
import yelm.io.raccoon.user_address.controller.AddressesBottomSheet;
import yelm.io.raccoon.chat.controller.ChatActivity;
import yelm.io.raccoon.R;
import yelm.io.raccoon.database_new.Common;
import yelm.io.raccoon.main.news.NewsAdapter;
import yelm.io.raccoon.support_stuff.ItemOffsetDecorationRight;

public class MainActivity extends AppCompatActivity implements AddressesBottomSheet.AddressesBottomSheetListener {


    ActivityMainBinding binding;
    AddressesBottomSheet addressesBottomSheet = new AddressesBottomSheet();

    private ArrayList<CategoriesWithProductsClass> catalogsWithProductsList = new ArrayList<>();
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
        getCategoriesWithProducts("0", "0");
        initNews();
        getAppToken();
        getCategories();
        getLocationPermission();
        Bundle args = getIntent().getExtras();
        if (args != null) {
            Log.d(Logging.debug, "MainActivity - Notification data: " + args.getString("id"));
            Log.d(Logging.debug, "MainActivity - Notification data: " + args.getString("name"));
            if (Objects.equals(args.getString("name"), "news")) {
                Intent intent = new Intent(MainActivity.this, NewsFromNotificationActivity.class);
                intent.putExtra("id", args.getString("id"));
                startActivity(intent);
            } else if (Objects.equals(args.getString("name"), "item")) {
                Intent intent = new Intent(MainActivity.this, ItemFromNotificationActivity.class);
                intent.putExtra("id", args.getString("id"));
                startActivity(intent);
            }
        }

        checkIfGPSEnabled();
    }

    private void checkIfGPSEnabled() {
        if (!StaticRepository.isLocationEnabled(this)) {
            Snackbar snackbar = Snackbar.make(
                    findViewById(R.id.layout),
                    R.string.mainActivityNoGPS,
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Включить", view -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setActionTextColor(getResources().getColor(R.color.mainThemeColor));
            snackbar.show();
        }
    }

    private void binding() {
        binding.chat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        binding.recyclerCards.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerCards.setHasFixedSize(false);
        binding.recyclerCards.addItemDecoration(new ItemOffsetDecorationRight((int) getResources().getDimension(R.dimen.dimens_16dp)));
        binding.addressLayout.setOnClickListener(v -> callAddressesBottomSheet());
        binding.userCurrentAddress.setOnClickListener(v -> callAddressesBottomSheet());
        binding.search.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        binding.basket.setOnClickListener(v -> startActivity(new Intent(this, BasketActivity.class)));

        binding.recyclerCategories.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));
        binding.recyclerCategories.setHasFixedSize(false);

        binding.categoryExpand.setOnClickListener(v -> {
            if (binding.recyclerCategories.getVisibility() == View.VISIBLE) {
                binding.recyclerCategories.setVisibility(View.GONE);
                binding.categoryExpand.setRotation(0);
            } else {
                binding.recyclerCategories.setVisibility(View.VISIBLE);
                binding.categoryExpand.setRotation(90);
            }
        });
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
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (hasLocationPermission()) {
                Log.d(Logging.debug, "Method onRequestPermissionsResult() - Request Permissions Result: Success!");
                getUserCurrentLocation();
            } else if (shouldShowRequestPermissionRationale(permissions[0])) {
                showDialogExplanationAboutRequestLocationPermission(getText(R.string.mainActivityRequestPermission).toString());
            } else {
                Log.d(Logging.debug, "Method onRequestPermissionsResult() - Request Permissions Result: Failed!");
                performIfNoLocationPermission();
            }
        } else {
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
        LocationRequest locationRequest = LocationRequest.create();
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
        public void onLocationResult(@NotNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            binding.progress.setVisibility(View.GONE);
            double latitude = locationResult.getLastLocation().getLatitude();
            double longitude = locationResult.getLastLocation().getLongitude();
            Log.d(Logging.debug, "location updated:" + "\nlatitude: " + latitude + "\nlongitude: " + longitude);
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
                Log.d(Logging.debug, "userChosenAddress: " + userCurrentAddress.getAddressLine(0));
                Log.d(Logging.debug, "getFeatureName: " + userCurrentAddress.getFeatureName());
                Log.d(Logging.debug, "getThoroughfare: " + userCurrentAddress.getThoroughfare());
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
                getCategoriesWithChosenProducts(
                        "3",
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry(),
                        RestAPI.PLATFORM_NUMBER,
                        Lat,
                        Lon).
                enqueue(new Callback<ArrayList<CategoriesWithProductsClass>>() {
                    @Override
                    public void onResponse(@NotNull Call<ArrayList<CategoriesWithProductsClass>> call, @NotNull final Response<ArrayList<CategoriesWithProductsClass>> response) {
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
                    public void onFailure(@NotNull Call<ArrayList<CategoriesWithProductsClass>> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method getCategoriesWithProducts() - failure: " + t.toString());
                    }
                });
    }

    private void getCategories() {
        RetrofitClient.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                getCategories(
                        RestAPI.PLATFORM_NUMBER,
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry()
                ).
                enqueue(new Callback<ArrayList<CategoriesPOJO>>() {
                    @Override
                    public void onResponse(@NotNull Call<ArrayList<CategoriesPOJO>> call, @NotNull final Response<ArrayList<CategoriesPOJO>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                CategoriesAdapter categoriesAdapter = new CategoriesAdapter(MainActivity.this, response.body());

                                binding.recyclerCategories.setAdapter(categoriesAdapter);
                            } else {
                                Log.e(Logging.error, "Method getCategories() - by some reason response is null!");
                            }
                        } else {
                            Log.e(Logging.error, "Method getCategories() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ArrayList<CategoriesPOJO>> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method getCategories() - failure: " + t.toString());
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
                    String token = task.getResult();
                    //Log.d(Logging.debug, "FCM: token:" + token);
                    RestMethods.sendRegistrationToServer(token);
                });
    }

    @Override
    public void selectedAddress(UserAddress selectedUserAddress) {
        binding.progress.setVisibility(View.GONE);
        Log.d(Logging.debug, "Method selectedAddress() - address: " + selectedUserAddress.address);
        binding.userCurrentAddress.setText(selectedUserAddress.address);
        getCategoriesWithProducts(selectedUserAddress.latitude, selectedUserAddress.longitude);
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
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
        Log.d(Logging.debug, "allowUpdateUI: " + allowUpdateUI);
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
                        binding.basket.setVisibility(View.GONE);
                        binding.basket.setText(String.format("0 %s", LoaderActivity.settings.getString(LoaderActivity.PRICE_IN, "")));
                        binding.footer.setVisibility(View.GONE);
                    } else {
                        binding.basket.setVisibility(View.VISIBLE);
                        binding.footer.setVisibility(View.VISIBLE);
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

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        //No call for super(). Bug on API Level > 11.
        super.onSaveInstanceState(outState);
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
    }
}