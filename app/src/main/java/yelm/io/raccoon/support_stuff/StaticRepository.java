package yelm.io.raccoon.support_stuff;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

public class StaticRepository {

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

}
