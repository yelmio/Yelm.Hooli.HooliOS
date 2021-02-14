package yelm.io.yelm.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import yelm.io.yelm.R;
import yelm.io.yelm.loader.controller.LoaderActivity;

public class ForegroundService extends Service {
    public ForegroundService() {
    }

    private static final int NOTIF_ID = 3;

    String channelId = "345";
    String channelName = "foreground";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // do your jobs here
        startForeground();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground() {

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, LoaderActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setAutoCancel(true)
                .setContentTitle("title")
                .setContentText("body")
                //.setLargeIcon(bitmap)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_notify)
                .setColor(getResources().getColor(R.color.mainThemeColor))
                .setContentIntent(pendingIntent)
                //.setSound(defaultSoundUri)
                ;

        startForeground(NOTIF_ID, builder.build());
    }
}
