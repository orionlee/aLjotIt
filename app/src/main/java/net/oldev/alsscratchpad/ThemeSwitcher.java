package net.oldev.alsscratchpad;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.StyleRes;
import android.util.Log;

import java.util.Calendar;

// Utility to set the theme for an Activity
public class ThemeSwitcher {

    private static final String TAG = "LSSP-Theme";

    /**
     * Set the activity to the theme based on the named model
     */
    public static void setTheme(LSScratchPadModel model, Activity activity) {
        @LSScratchPadModel.ThemeOption String theme = model.getTheme();
        @StyleRes int themeId = findThemeIdByOption(theme);
        activity.setTheme(themeId);
    }

    /**
     * Helper to refresh the named activity.
     * Use case: refresh Settings itself upon theme change.
     */
    public static void refreshActivity(Activity activity) {
        activity.finish();
        Intent refresh = new Intent(activity.getApplicationContext(), activity.getClass());
        activity.startActivity(refresh);
    }

    private static @StyleRes int getThemeIdByTime() {
        final int nightThemeBeginHour = 23; // PENDING: config from mModel
        final int nightThemeEndHour = 7;

        int curHr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (curHr >= nightThemeBeginHour ||
                curHr <= nightThemeEndHour) {
            return R.style.AppTheme_Dark;
        } else {
            return R.style.AppTheme;
        }
    }

    private static @StyleRes int findThemeIdByOption(@LSScratchPadModel.ThemeOption String theme) {
        @StyleRes int themeId;

        switch (theme) {
            case LSScratchPadModel.THEME_AUTO:
                themeId = getThemeIdByTime();
                break;
            case LSScratchPadModel.THEME_DARK:
                themeId = R.style.AppTheme_Dark;
                break;
            case LSScratchPadModel.THEME_LIGHT:
                themeId = R.style.AppTheme;
                break;
            default:
                Log.w(TAG, "Unexpected theme option +[" + theme + "]. Use default");
                themeId = R.style.AppTheme;
        }
        return themeId;
    }

}
