package net.oldev.aljotit;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Method;

public class QSPanelUtil {

    public static boolean expandQuickSettingsPanel(@NonNull Context context) {
        // Using system undocumented methods so it might not work on certain devices.
        // Adapted from
        // https://stackoverflow.com/a/15582509
        try {
            Object sbservice = context.getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method showsb;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                showsb = statusbarManager.getMethod("expandSettingsPanel");
            } else {
                showsb = statusbarManager.getMethod("expand");
            }
            showsb.invoke(sbservice);
            return true;
        } catch (Throwable t) {
            Log.w("LJI-Utils", "Fail to expand quick settings panel.", t);
            return false;
        }
    }
}

