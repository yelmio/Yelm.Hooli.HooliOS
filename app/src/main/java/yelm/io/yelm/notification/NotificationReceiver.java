package yelm.io.yelm.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import yelm.io.yelm.support_stuff.Logging;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Logging.debug, "NotificationReceiver");
        Log.d(Logging.debug, "intent: " + intent.getStringExtra("data"));

    }
}
