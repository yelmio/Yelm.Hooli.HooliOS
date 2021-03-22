package yelm.io.raccoon.loader.controller;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.raccoon.R;
import yelm.io.raccoon.loader.model.ApplicationSettings;
import yelm.io.raccoon.loader.model.ChatSettingsClass;
import yelm.io.raccoon.rest.query.RestMethods;
import yelm.io.raccoon.support_stuff.Logging;
import yelm.io.raccoon.database_new.basket_new.BasketCartDataSource;
import yelm.io.raccoon.database_new.basket_new.BasketCartRepository;
import yelm.io.raccoon.database_new.Common;
import yelm.io.raccoon.database_new.Database;
import yelm.io.raccoon.database_new.user_addresses.UserAddressesDataSource;
import yelm.io.raccoon.database_new.user_addresses.UserAddressesRepository;
import yelm.io.raccoon.loader.model.UserLoginResponse;
import yelm.io.raccoon.main.controller.MainActivity;
import yelm.io.raccoon.payment.Constants;
import yelm.io.raccoon.rest.rest_api.RestAPI;
import yelm.io.raccoon.rest.client.RetrofitClient;

public class LoaderActivity extends AppCompatActivity {

    public static final String USER_NAME = "USER_NAME";
    public static final String MIN_PRICE_FOR_FREE_DELIVERY = "MIN_PRICE_FOR_FREE_DELIVERY";
    public static final String COLOR = "COLOR";
    public static final String MIN_ORDER_PRICE = "MIN_ORDER_PRICE";
    public static final String PRICE_IN = "PRICE_IN";
    public static final String CURRENCY = "CNT";
    public static final String COUNTRY_CODE = "COUNTRY_CODE";
    public static final String API_TOKEN = "API_TOKEN";
    public static final String ROOM_CHAT_ID = "ROOM_ID";
    public static final String SHOP_CHAT_ID = "SHOP_ID";
    public static final String CLIENT_CHAT_ID = "CLIENT_ID";

    public static final String DISCOUNT_TYPE = "DISCOUNT_TYPE";
    public static final String DISCOUNT_AMOUNT = "DISCOUNT_AMOUNT";
    public static final String DISCOUNT_NAME = "DISCOUNT_NAME";

    public static SharedPreferences settings;
    private static final String APP_PREFERENCES = "settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

//        Log.d(Logging.debug, "Locale.getDefault().getDisplayLanguage(): " + Locale.getDefault().getDisplayLanguage());
//        Log.d(Logging.debug, "Locale.getDefault().getLanguage(): " + Locale.getDefault().getLanguage());
//        Log.d(Logging.debug, "Locale.locale: " + getResources().getConfiguration().locale);
        //Log.d("AlexDebug", "getResources().getConfiguration().locale.getLanguage(): " + getResources().getConfiguration().locale.getLanguage());
        // Log.d("AlexDebug", "getResources().getConfiguration().locale.getCountry() " + getResources().getConfiguration().locale.getCountry());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH));
        }
        RestMethods.sendStatistic("open_app");
        initRoom();
        init();
    }

    private JSONObject getDeviceInfo() {
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
        return jsonData;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    //if user does not exist then we pull request to create it
    private void checkUser() {
        if (settings.contains(USER_NAME)) {
            Log.d(Logging.debug, "Method checkUser() - user exist: " + settings.getString(USER_NAME, ""));
            getApplicationSettings();
            getChatSettings(settings.getString(USER_NAME, ""));
        } else {
            RetrofitClient.
                    getClient(RestAPI.URL_API_MAIN)
                    .create(RestAPI.class)
                    .createUser(getResources().getConfiguration().locale.getLanguage(),
                            getResources().getConfiguration().locale.getCountry(),
                            RestAPI.PLATFORM_NUMBER,
                            getDeviceInfo())
                    .enqueue(new Callback<UserLoginResponse>() {
                        @Override
                        public void onResponse(@NotNull Call<UserLoginResponse> call, @NotNull Response<UserLoginResponse> response) {
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.putString(USER_NAME, response.body().getLogin()).apply();
                                    Log.d(Logging.debug, "Method checkUser() - created user: " + settings.getString(USER_NAME, ""));
                                    getChatSettings(settings.getString(USER_NAME, ""));
                                    getApplicationSettings();
                                } else {
                                    Log.e(Logging.error, "Method checkUser() - by some reason response is null!");
                                }
                            } else {
                                Log.e(Logging.error, "Method checkUser() - response is not successful. " +
                                        "Code: " + response.code() + "Message: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<UserLoginResponse> call, @NotNull Throwable t) {
                            Log.e(Logging.error, "Method checkUser() - failure: " + t.toString());
                        }
                    });
        }
    }

    //we get main settings of app
    private void getApplicationSettings() {
        RetrofitClient.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                getAppSettings(RestAPI.PLATFORM_NUMBER,
                        getResources().getConfiguration().locale.getLanguage(),
                        getResources().getConfiguration().locale.getCountry()
                ).
                enqueue(new Callback<ApplicationSettings>() {
                    @Override
                    public void onResponse(@NotNull Call<ApplicationSettings> call, @NotNull final Response<ApplicationSettings> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
//                                Log.d(Logging.debug, "Method getApplicationSettings() - MERCHANT_PUBLIC_ID: " +
//                                        " " + response.body().getSettings().getPublicId());
                                Constants.MERCHANT_PUBLIC_ID = response.body().getSettings().getPublicId();
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString(MIN_PRICE_FOR_FREE_DELIVERY, response.body().getSettings().getMinDeliveryPrice());
                                editor.putString(MIN_ORDER_PRICE, response.body().getSettings().getMinOrderPrice());
                                editor.putString(CURRENCY, response.body().getCurrency());
                                editor.putString(COLOR, response.body().getSettings().getTheme());
                                editor.putString(PRICE_IN, response.body().getSymbol());
                                editor.putString(COUNTRY_CODE, response.body().getSettings().getRegionCode());
                                editor.apply();
                                launchMain();
                            } else {
                                Log.e(Logging.error, "Method getApplicationSettings(): by some reason response is null!");
                            }
                        } else {
                            Log.e(Logging.error, "Method getApplicationSettings() response is not successful." +
                                    " Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ApplicationSettings> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method getApplicationSettings() failure: " + t.toString());
                    }
                });
    }

    private void launchMain() {
        Bundle args = getIntent().getExtras();
        Intent intent = new Intent(LoaderActivity.this, MainActivity.class);
        if (args != null) {
            String data = args.getString("data");
            if (data != null) {
                Log.d(Logging.debug, "LoaderActivity - Notification data: " + data);
                try {
                    JSONObject jsonObj = new JSONObject(data);
                    Log.d(Logging.debug, "jsonObj id: " + jsonObj.getString("id"));
                    Log.d(Logging.debug, "jsonObj name: " + jsonObj.getString("name"));
                    intent.putExtra("id", jsonObj.getString("id"));
                    intent.putExtra("name", jsonObj.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(Logging.debug, "LoaderActivity - id: " + args.getString("id"));
                Log.d(Logging.debug, "LoaderActivity - name: " + args.getString("name"));
                intent.putExtra("id", args.getString("id"));
                intent.putExtra("name", args.getString("name"));
            }
        } else {
            Log.d(Logging.debug, "LoaderActivity - args is NULL");
        }
        startActivity(intent);
        finish();
    }

    private void initRoom() {
        Common.sDatabase = Database.getInstance(this);
        Common.basketCartRepository = BasketCartRepository.getInstance(BasketCartDataSource.getInstance(Common.sDatabase.basketCartDao()));
        Common.userAddressesRepository = UserAddressesRepository.getInstance(UserAddressesDataSource.getInstance(Common.sDatabase.addressesDao()));
    }

    private void init() {
        if (isNetworkConnected()) {
            Log.d(Logging.debug, "Method init() - NetworkConnected successfully");
            checkUser();
        } else {
            Log.d(Logging.debug, "Method init() - NetworkConnected not successful");
            Snackbar snackbar = Snackbar.make(
                    findViewById(R.id.layout),
                    R.string.loaderActivityNoNetworkConnection,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.loaderActivityUpdateNetworkConnection, view -> init());
            snackbar.show();
        }
    }

    private void getChatSettings(String login) {
        RetrofitClient.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                getChatSettings(login).
                enqueue(new Callback<ChatSettingsClass>() {
                    @Override
                    public void onResponse(@NotNull Call<ChatSettingsClass> call, @NotNull final Response<ChatSettingsClass> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                Log.d(Logging.debug, "ChatSettingsClass: " + response.body().toString());
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString(API_TOKEN, response.body().getApiToken());
                                editor.putString(SHOP_CHAT_ID, response.body().getShop());
                                editor.putString(ROOM_CHAT_ID, response.body().getRoomId());
                                editor.putString(CLIENT_CHAT_ID, response.body().getClient());
                                editor.apply();
                            } else {
                                Log.e(Logging.error, "Method getChatSettings(): by some reason response is null!");
                            }
                        } else {
                            Log.e(Logging.error, "Method getChatSettings() response is not successful." +
                                    " Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ChatSettingsClass> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method getChatSettings() failure: " + t.toString());
                    }
                });
    }
}