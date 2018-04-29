package net.oldev.aljotit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Method;

public class QSPanelUtil {

    private static final String TAG = "LJI-Utils";

    public static boolean expandQuickSettingsPanel(@NonNull Context context) {
        // Using system undocumented methods so it might not work on certain devices.
        // Adapted from
        // https://stackoverflow.com/a/15582509
        try {
            @SuppressLint("WrongConstant") Object sbservice = context.getSystemService("statusbar");
            @SuppressLint("PrivateApi") Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method showsb;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                showsb = statusbarManager.getMethod("expandSettingsPanel");
            } else {
                showsb = statusbarManager.getMethod("expand");
            }
            showsb.invoke(sbservice);
            return true;
        } catch (Throwable t) {
            Log.w(TAG, "Fail to expand quick settings panel.", t);
            return false;
        }
    }
}

