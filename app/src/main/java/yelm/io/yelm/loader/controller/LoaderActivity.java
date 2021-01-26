package yelm.io.yelm.loader.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.loader.model.ApplicationSettings;
import yelm.io.yelm.support_stuff.AlexTAG;
import yelm.io.yelm.database_new.basket_new.BasketCartDataSource;
import yelm.io.yelm.database_new.basket_new.BasketCartRepository;
import yelm.io.yelm.database_new.Common;
import yelm.io.yelm.database_new.Database;
import yelm.io.yelm.database_new.user_addresses.UserAddressesDataSource;
import yelm.io.yelm.database_new.user_addresses.UserAddressesRepository;
import yelm.io.yelm.database_old.articles.ArticleDataSource;
import yelm.io.yelm.database_old.articles.ArticlesRepository;
import yelm.io.yelm.database_old.basket.CartDataSource;
import yelm.io.yelm.database_old.basket.CartRepository;
import yelm.io.yelm.database_old.catalog.products.ProductDataSource;
import yelm.io.yelm.database_old.catalog.products.ProductRepository;
import yelm.io.yelm.database_old.news.NewsDataSource;
import yelm.io.yelm.database_old.news.NewsRepository;
import yelm.io.yelm.loader.model.UserLoginResponse;
import yelm.io.yelm.main.controller.MainActivity;
import yelm.io.yelm.payment.Constants;
import yelm.io.yelm.retrofit.new_api.RestAPI;
import yelm.io.yelm.retrofit.new_api.RetrofitClientNew;

public class LoaderActivity extends AppCompatActivity {

    public static final String USER_NAME = "USER_NAME";
    public static final String ALLOW_PAYMENTS = "ALLOW_PAYMENTS";
    public static final String CATALOG_STYLE = "CATALOG_STYLE";
    public static final String USER_TYPE = "USER_TYPE";
    public static final String MIN_DELIVERY_PRICE = "MIN_DELIVERY_PRICE";
    public static final String MIN_ORDER_PRICE = "MIN_ORDER_PRICE";
    public static final String PRICE_IN = "PRICE_IN";
    public static final String CURRENCY = "CNT";
    public static final String COUNTRY_CODE = "COUNTRY_CODE";

    public static SharedPreferences settings;
    private static final String APP_PREFERENCES = "settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        Log.d("AlexDebug", "Locale.getDefault().getDisplayLanguage(): " + Locale.getDefault().getDisplayLanguage());
        Log.d("AlexDebug", "Locale.getDefault().getLanguage(): " + Locale.getDefault().getLanguage());
        Log.d("AlexDebug", "Locale.locale: " + getResources().getConfiguration().locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
        initRoom();
        init();
    }

    private String getDeviceInfo() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("percentage", Integer.toString(percentage));
            jsonData.put("device", Build.DEVICE);
            jsonData.put("model", Build.MODEL);
            jsonData.put("product", Build.PRODUCT);
            jsonData.put("display", Build.DISPLAY);
            jsonData.put("brand", Build.BRAND);
            jsonData.put("user", Build.USER);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonData.toString();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    //if user does not exist then we pull request to create it
    private void checkUser() {
        if (settings.contains(USER_NAME)) {
            Log.d(AlexTAG.debug, "Method checkUser() - user exist: " + settings.getString(USER_NAME, ""));
            startActivity(new Intent(LoaderActivity.this, MainActivity.class));
            finish();
        } else {
//            User user = new User(0, "", "", "", "", "", "", "", "", "");
            //Common.userRepository.insertToUser(user);
            //Log.d("AlexDebug", "user:" + user.toString());
            RetrofitClientNew.
                    getClient(RestAPI.URL_API_MAIN)
                    .create(RestAPI.class)
                    .createUser(RestAPI.PLATFORM_NUMBER, getDeviceInfo())
                    .enqueue(new Callback<UserLoginResponse>() {
                        @Override
                        public void onResponse(@NotNull Call<UserLoginResponse> call, @NotNull Response<UserLoginResponse> response) {
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString(USER_NAME, response.body().getLogin()).apply();
                                    Log.d(AlexTAG.debug, "Method checkUser() - created user:" +
                                            " " + settings.getString(USER_NAME, ""));
                                    startActivity(new Intent(LoaderActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Log.e(AlexTAG.error, "Method checkUser() - by some reason response is null!");
                                }
                            } else {
                                Log.e(AlexTAG.error, "Method checkUser() - response is not successful. " +
                                        "Code: " + response.code() + "Message: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<UserLoginResponse> call, @NotNull Throwable t) {
                            Log.e(AlexTAG.error, "Method checkUser() - failure: " + t.toString());
                        }
                    });
        }
    }

    //we get main settings of app
    private void getApplicationSettings() {
        RetrofitClientNew.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                getAppSettings(RestAPI.PLATFORM_NUMBER,
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry(),
                        "0",
                        "0").
                enqueue(new Callback<ApplicationSettings>() {
                    @Override
                    public void onResponse(@NotNull Call<ApplicationSettings> call, @NotNull final Response<ApplicationSettings> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                Log.d(AlexTAG.debug, "Method getApplicationSettings() - MERCHANT_PUBLIC_ID: " +
                                        " " + response.body().getSettings().getPublicId());
                                Constants.MERCHANT_PUBLIC_ID = response.body().getSettings().getPublicId();
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString(MIN_DELIVERY_PRICE, response.body().getSettings().getMinDeliveryPrice());
                                editor.putString(MIN_ORDER_PRICE, response.body().getSettings().getMinOrderPrice());
                                editor.putString(CURRENCY, response.body().getCurrency());
                                editor.putString(PRICE_IN, response.body().getSymbol());
                                editor.putString(COUNTRY_CODE, response.body().getSettings().getRegionCode());
                                editor.apply();

                                //check user if we got the main settings
                                checkUser();
                            } else {
                                Log.e(AlexTAG.error, "Method getApplicationSettings(): by some reason response is null!");
                            }
                        } else {
                            Log.e(AlexTAG.error, "Method getApplicationSettings() response is not successful." +
                                    " Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ApplicationSettings> call, @NotNull Throwable t) {
                        Log.e(AlexTAG.error, "Method getApplicationSettings() failure: " + t.toString());
                    }
                });
    }

    private void initRoom() {
        Common.sDatabase = Database.getInstance(this);
        Common.cartRepository = CartRepository.getInstance(CartDataSource.getInstance(Common.sDatabase.cartDAO()));
        Common.basketCartRepository = BasketCartRepository.getInstance(BasketCartDataSource.getInstance(Common.sDatabase.basketCartDao()));
        Common.productRepository = ProductRepository.getInstance(ProductDataSource.getInstance(Common.sDatabase.productDao()));
        //Common.stockRepository = StockRepository.getInstance(StockDataSource.getInstance(Common.sDatabase.stockDao()));
        Common.newsRepository = NewsRepository.getInstance(NewsDataSource.getInstance(Common.sDatabase.newsDao()));
        Common.articlesRepository = ArticlesRepository.getInstance(ArticleDataSource.getInstance(Common.sDatabase.articlesDao()));
        //Common.userRepository = UserRepository.getInstance(UserDataSource.getInstance(Common.sDatabase.userDAO()));
        Common.userAddressesRepository = UserAddressesRepository.getInstance(UserAddressesDataSource.getInstance(Common.sDatabase.addressesDao()));

    }

    private void init() {
        if (isNetworkConnected()) {
            Log.d(AlexTAG.debug, "Method init() - NetworkConnected successfully");

            //clean all data before adding if there is network connection
            //Common.productRepository.emptyProduct();
            //Common.articlesRepository.emptyArticles();
            //Common.stockRepository.emptyStock();
            //Common.newsRepository.emptyNews();
            getApplicationSettings();
        } else {
            Log.d(AlexTAG.debug, "Method init() - NetworkConnected not successful");
            Snackbar snackbar = Snackbar.make(
                    findViewById(R.id.layout),
                    R.string.loaderActivityNoNetworkConnection,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.loaderActivityUpdateNetworkConnection, view -> init());
            snackbar.show();

        }
    }




}