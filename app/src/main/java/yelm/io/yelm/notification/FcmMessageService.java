package yelm.io.yelm.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Response;
import yelm.io.yelm.R;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.retrofit.API;
import yelm.io.yelm.retrofit.RetrofitClient;

public class FcmMessageService extends FirebaseMessagingService {

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
                            .getClient(API.URL_API_MAIN)
                            .create(API.class)
                            .postFCM(user, s)
                            .enqueue(new retrofit2.Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, final Response<String> response) {
                                    if (response.isSuccessful()) {
                                        Log.d("AlexDebug", "FCM Token registered");
                                        Log.d("AlexDebug", "FCM response: " + response);
                                    } else {
                                        Log.d("AlexDebug", "FCM Code: " + response.code() + "Message: " + response.message());
                                    }
                                }
                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Log.d("AlexDebug", "FCM Throwable: " + t.toString());
                                }
                            });
                }
            }, 2000);
        }};
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //showNotification(remoteMessage.getData().get("message"));
        //showNotification(remoteMessage.getNotification().getBody());
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("AlexDebug", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("AlexDebug", "Message data payload: " + remoteMessage.getData());

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
            showNotification(remoteMessage);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void showNotification(RemoteMessage remoteMessage) {

        Intent i = new Intent(this, LoaderActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = getString(R.string.default_notification_channel_id);
        String channelName = getString(R.string.default_notification_channel_name);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yelm_128);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setAutoCancel(true)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setLargeIcon(bitmap)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_notify)
                .setColor(getResources().getColor(R.color.mainThemeColor))
                .setContentIntent(pendingIntent)
                //.setSound(defaultSoundUri)
                ;

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }
}
