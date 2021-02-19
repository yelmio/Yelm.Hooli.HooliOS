package yelm.io.yelm.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.retrofit.RestAPI;
import yelm.io.yelm.retrofit.RetrofitClient;
import yelm.io.yelm.constants.Logging;

public class FcmMessageService extends FirebaseMessagingService {

    private static final int NOTIFY_ID = 101;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("AlexDebug", "Refreshed token: " + s);
        sendRegistrationToServer(s);
    }

    private void sendRegistrationToServer(String s) {
        new Handler(Looper.getMainLooper()) {{
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    String user = LoaderActivity.settings.getString(LoaderActivity.USER_NAME, "");
                    Log.d("AlexDebug", "FCM user: " + user);
                    RetrofitClient
                            .getClient(RestAPI.URL_API_MAIN)
                            .create(RestAPI.class)
                            .putFCM(RestAPI.PLATFORM_NUMBER, user, s)
                            .enqueue(new retrofit2.Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        Log.d("AlexDebug", "FCM Token registered");
                                        Log.d("AlexDebug", "FCM response: " + response);
                                    } else {
                                        Log.d("AlexDebug", "FCM Code: " + response.code() + "Message: " + response.message());
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Log.d("AlexDebug", "FCM Throwable: " + t.toString());
                                }
                            });
                }
            }, 2000);
        }};
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(Logging.debug, "From: " + remoteMessage.getFrom());
        Log.d(Logging.debug, "remoteMessage.getData(): " + remoteMessage.getData().toString());
        Log.d(Logging.debug, "remoteMessage.toString(): " + remoteMessage.toString());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("AlexDebug", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.d("AlexDebug", "Message Notification Title: " + remoteMessage.getNotification().getTitle());
        }
        showNotification(remoteMessage);
    }

    private void showNotification(RemoteMessage remoteMessage) {

        Intent i = new Intent(this, LoaderActivity.class);
        i.putExtra("data", remoteMessage.getData().toString());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 333, i, PendingIntent.FLAG_UPDATE_CURRENT);
//FLAG_ONE_SHOT       FLAG_UPDATE_CURRENT

        String channelId = getString(R.string.default_notification_channel_id);
        String channelName = getString(R.string.default_notification_channel_name);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yelm_media);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setAutoCancel(true)
                .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setLargeIcon(bitmap)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_notify)
                .setColor(getResources().getColor(R.color.mainThemeColor))
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(NOTIFY_ID, builder.build());
    }
}