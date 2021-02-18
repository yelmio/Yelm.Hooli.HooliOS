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
    public static final String ACTION_GET_DATA = "notification.RESPONSE";
    public static final String DATA_KEY = "data";
    public static SharedPreferences settings;
    private static final String APP_PREFERENCES = "settings";
    public static final String NOTIFICATION_DATA = "NOTIFICATION_DATA";

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

//    public void sendBroadcastNotification(String data) {
//        Log.d(Logging.debug, "Sending broadcast notification: " + data);
//        Intent intentBroadcast = new Intent(ACTION_GET_DATA);
//        intentBroadcast.putExtra(DATA_KEY, data);
//        sendBroadcast(intentBroadcast);
//    }

//    @Override
//    public void handleIntent(Intent intent) {
//        super.handleIntent(intent);
//        Log.d(Logging.debug, "handleIntent");
//        Log.d(Logging.debug, "intent: " + intent.getStringExtra("data"));
//
//        Intent responseIntent = new Intent();
//        responseIntent.setAction(ACTION_GET_DATA);
//        responseIntent.addCategory(Intent.CATEGORY_DEFAULT);
//        responseIntent.putExtra(DATA_KEY, "test");
//        sendBroadcast(responseIntent);
//    }


    private void sendMessageToActivity(String data) {

//        Intent sendLevel = new Intent();
//        sendLevel.setAction("GET_WORK_TIME");
//        sendLevel.putExtra( "WORK_TIME",time);
//        sendBroadcast(sendLevel);

        Intent intent = new Intent("NOTIFICATION");
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //showNotification(remoteMessage.getData().get("message"));
        //showNotification(remoteMessage.getNotification().getBody());
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(Logging.debug, "From: " + remoteMessage.getFrom());
        Log.d(Logging.debug, "remoteMessage.getData(): " + remoteMessage.getData().toString());
        Log.d(Logging.debug, "remoteMessage.toString(): " + remoteMessage.toString());

        sendMessageToActivity("broadcast DATA");

        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(NOTIFICATION_DATA, "data").apply();
        // Check if message contains a data payload.

        if (remoteMessage.getData().size() > 0) {
            // Log.d(Logging.debug, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("AlexDebug", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.d("AlexDebug", "Message Notification Title: " + remoteMessage.getNotification().getTitle());
        }
        showNotification(remoteMessage);

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void showNotification(RemoteMessage remoteMessage) {

        Intent i = new Intent(this, LoaderActivity.class);
        i.putExtra("data", "text");
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