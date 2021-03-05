package yelm.io.raccoon.support_stuff;

import android.app.Activity;
import android.util.DisplayMetrics;

public class ScreenDimensions {

    //Convert from px to dip.  (dip * activity.getResources().getDisplayMetrics().density + 0.5f);

    private Activity activity;

    public ScreenDimensions(Activity activity) {
        this.activity = activity;

    }

    private DisplayMetrics getScreenDimension(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public float getScreenDensity() {
        return activity.getResources().getDisplayMetrics().density;
    }

    public float getWidthPX() {
        DisplayMetrics displayMetrics = getScreenDimension(activity);
        return displayMetrics.widthPixels;
    }

    public float getHeightPX() {
        DisplayMetrics displayMetrics = getScreenDimension(activity);
        return displayMetrics.heightPixels ;
    }

    public float getWidthDP() {
        DisplayMetrics displayMetrics = getScreenDimension(activity);
        return displayMetrics.widthPixels / displayMetrics.density;
    }

    public float getHeightDP() {
        DisplayMetrics displayMetrics = getScreenDimension(activity);
        return displayMetrics.heightPixels / displayMetrics.density;
    }
}
