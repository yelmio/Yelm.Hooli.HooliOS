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
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.badge.BadgeDrawable;
import com.google.zxing.Result;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.basket.controller.BasketActivityOnlyDelivery;
import yelm.io.yelm.main.news.NewNews;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.search.SearchActivity;
import yelm.io.yelm.database_new.basket_new.BasketCart;
import yelm.io.yelm.database_new.user_addresses.UserAddress;
import yelm.io.yelm.databinding.ActivityMainBinding;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.main.model.CatalogsWithProductsClass;
import yelm.io.yelm.main.model.Modifier;
import yelm.io.yelm.retrofit.new_api.RestAPI;
import yelm.io.yelm.retrofit.new_api.RetrofitClientNew;
import yelm.io.yelm.constants.Constants;
import yelm.io.yelm.user_address.controller.AddressesBottomSheet;
import yelm.io.yelm.chat.controller.ChatActivity;
import yelm.io.yelm.main.adapter.ProductsNewMenuAdapter;
import yelm.io.yelm.R;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.main.news.NewsAdapter;
import yelm.io.yelm.support_stuff.ItemOffsetDecorationRight;
import yelm.io.yelm.retrofit.DynamicURL;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler, AddressesBottomSheet.AddressesBottomSheetListener {

    public BadgeDrawable badge;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private String userID = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");
    ActivityMainBinding binding;
    AddressesBottomSheet addressesBottomSheet = new AddressesBottomSheet();

    private ProductsNewMenuAdapter productsAdapter;
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

        initNews();

        getLocationPermission();
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
            allowUpdateUI = false;
        }
    }

    private void getLocationPermission() {
        if (hasLocationPermission()) {
            Log.d(AlexTAG.debug, "Method getLocationPermission() - Location permission granted");
            //getUserCurrentAddress();
            getUserCurrentLocation();
        } else {
            Log.d(AlexTAG.debug, "Method getLocationPermission() - Location permission not granted");
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
                    Log.d(AlexTAG.debug, "Method onRequestPermissionsResult() - Request Permissions Result: Success!");
                    //getUserCurrentAddress();
                    getUserCurrentLocation();
                } else if (shouldShowRequestPermissionRationale(permissions[0])) {
                    showDialogExplanationAboutRequestLocationPermission(getText(R.string.mainActivityRequestPermission).toString());
                } else {
                    Log.d(AlexTAG.debug, "Method onRequestPermissionsResult() - Request Permissions Result: Failed!");
                    performIfNoLocationPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    private void performIfNoLocationPermission() {
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
            getCategoriesWithProducts("0", "0");
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
        Log.d(AlexTAG.debug, "Method getUserCurrentLocation()");
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
                Log.d(AlexTAG.debug, "Method getUserCurrentLocation() - userStreet: " + userStreet);

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
                Log.d(AlexTAG.debug, "Method getUserCurrentLocation() - location null - unexpected error");
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
        Log.d("AlexDebug", "getResources().getConfiguration().locale.getLanguage(): " + getResources().getConfiguration().locale.getLanguage());
        Log.d("AlexDebug", "getResources().getConfiguration().locale.getCountry() " + getResources().getConfiguration().locale.getCountry());

        RetrofitClientNew.
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
                                Constants.ShopID = catalogsWithProductsList.get(0).getShopID();
                                Log.d(AlexTAG.debug, "Method getCategoriesWithProducts() - Constants.ShopID: " + Constants.ShopID);
                                redrawProducts();
                            } else {
                                Log.e(AlexTAG.error, "Method getCategoriesWithProducts() - by some reason response is null!");
                            }
                        } else {
                            Log.e(AlexTAG.error, "Method getCategoriesWithProducts() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ArrayList<CatalogsWithProductsClass>> call, @NotNull Throwable t) {
                        Log.e(AlexTAG.error, "Method getCategoriesWithProducts() - failure: " + t.toString());
                    }
                });
    }

    @Override
    public void selectedAddress(UserAddress userAddress) {
        Log.d(AlexTAG.debug, "Method selectedAddress() - address: " + userAddress.address);
        binding.userCurrentAddress.setText(userAddress.address);
        //getCategoriesWithProducts(userAddress.latitude,userAddress.longitude);
    }


    private void initNews() {
        RetrofitClientNew.
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
//                                if (newsList.size() == 1) {
//                                    binding.recyclerCards.setVisibility(View.GONE);
//                                    if (newsList.get(0).getName().trim().isEmpty()) {
//                                        Picasso.get()
//                                                .load(newsList.get(0).getImage().get(0))
//                                                .noPlaceholder()
//                                                .into(binding.mainTopImage);
//                                    } else {
//                                        Picasso.get()
//                                                .load(newsList.get(0).getImage().get(0))
//                                                .noPlaceholder()
//                                                .transform(new GradientTransformation(MainActivity.this))
//                                                .into(binding.mainTopImage);
//                                        binding.name.setText(newsList.get(0).getName());
//                                    }
//                                    binding.cardTopImage.setVisibility(View.VISIBLE);
//                                    binding.cardTopImage.setOnClickListener((v) -> {
//                                        //setLinks(stockList.get(0).getAttachments());
//                                    });
//                                } else {
                                //binding.cardTopImage.setVisibility(View.GONE);
                                newsAdapter = new NewsAdapter(MainActivity.this, newsList);
                                binding.recyclerCards.setAdapter(newsAdapter);
                                //}
                            } else {
                                Log.e(AlexTAG.error, "Method initNews() - by some reason response is null!");
                            }
                        } else {
                            Log.e(AlexTAG.error, "Method initNews() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ArrayList<NewNews>> call, @NotNull Throwable t) {
                        Log.e(AlexTAG.error, "Method initNews() - failure: " + t.toString());
                    }
                });

    }

    private boolean checkPlatform() {
        return (DynamicURL.getPlatformValue().equals("5f771d465f4191.76733056")
                || DynamicURL.getPlatformValue().equals("5f8561895c51f7.73864076")
                || DynamicURL.getPlatformValue().equals("5f5dfa9a7023c2.94067733"));
    }

    @Override
    public void onStop() {
        compositeDisposableBasket.clear();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateCost();
        if (allowUpdateUI) {
            redrawProducts();
            for (UserAddress address : Common.userAddressesRepository.getUserAddressesList()) {
                if (address.isChecked) {
                    binding.userCurrentAddress.setText(address.address);
                    //getCategoriesWithProducts(address.latitude, address.longitude);
                    return;
                }
            }
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
                        Log.d(AlexTAG.debug, "Method updateCost() - carts.size(): " + carts.size() + "\n" +
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
        Log.d(AlexTAG.debug, "Method redrawProducts()");
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
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
                (int) getResources().getDimension(R.dimen.dimen_60dp)));
        binding.storeFragments.addView(footer);
    }

    public void setScanCode(String code) {
        DynamicURL.setPLATFORM(code);
        Toast.makeText(this, "QRCode получен", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.d("AlexDebug", "rawResult.getText(): " + rawResult.getText());
        DynamicURL.setPLATFORM(rawResult.getText());
    }
}