package yelm.io.yelm.rest.query;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yelm.io.yelm.support_stuff.Logging;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.rest.rest_api.RestAPI;
import yelm.io.yelm.rest.client.RetrofitClient;

public class Statistic {

    static public void sendStatistic(String type) {
        RetrofitClient.
                getClient(RestAPI.URL_API_MAIN).
                create(RestAPI.class).
                sendStatistic(
                        RestAPI.PLATFORM_NUMBER,
                        LoaderActivity.settings.getString(LoaderActivity.USER_NAME, ""),
                        type).
                enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NotNull Call<ResponseBody> call, @NotNull final Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Log.e(Logging.error, "Method sendStatistic() - response is not successful." +
                                    "Code: " + response.code() + "Message: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                        Log.e(Logging.error, "Method sendStatistic() - failure: " + t.toString());
                    }
                });
    }
}