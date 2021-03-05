package yelm.io.raccoon.notification;

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

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import yelm.io.raccoon.R;
import yelm.io.raccoon.loader.controller.LoaderActivity;
import yelm.io.raccoon.rest.rest_api.RestAPI;
import yelm.io.raccoon.rest.client.RetrofitClient;
import yelm.io.raccoon.support_stuff.Logging;

public class FcmMessageService extends FirebaseMessagingService {

    private static final int NOTIFY_ID = 101;

    @Override
    public void onNewToken(@NotNull String s) {
        super.onNewToken(s);
        Log.d(Logging.debug, "Refreshed token: " + s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(Logging.debug, "From: " + remoteMessage.getFrom());
        Log.d(Logging.debug, "remoteMessage.getData(): " + remoteMessage.getData().toString());
        Log.d(Logging.debug, "remoteMessage.toString(): " + remoteMessage.toString());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(Logging.debug, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.d(Logging.debug, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
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

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.raccoon_icon);

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