package net.oldev.alsscratchpad;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.util.Log;

import net.oldev.alsscratchpad.LSScratchPadModel.ThemeOption;

import java.util.Calendar;

// Utility to set the theme for an Activity
public class ThemeSwitcher {

    private static final String TAG = "LSSP-Theme";

    /**
     * Set the activity to the theme based on the named model
     */
    public static void setTheme(LSScratchPadModel model, Activity activity) {
        @StyleRes int themeId = findThemeIdByOption(model, activity);
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

    private static boolean isDarkEffectiveForAutoTheme(@NonNull LSScratchPadModel model) {
        final TimeRange darkThemeTimeRange = model.getAutoThemeDarkTimeRange();

        Calendar curTime = Calendar.getInstance();
        return darkThemeTimeRange.contains(curTime);
    }

    private static @StyleRes int findThemeIdByOption(@NonNull LSScratchPadModel model,
                                                     @NonNull Activity activity) {
        @ThemeOption String theme = model.getTheme();

        // For auto case, resolve the actual theme option (light or dark)
        if (LSScratchPadModel.THEME_AUTO.equals(theme)) {
            theme = ( isDarkEffectiveForAutoTheme(model) ? LSScratchPadModel.THEME_DARK :
                    LSScratchPadModel.THEME_LIGHT );
        }

        // For light or dark option
        // resolve the actual style based on the option and the activity currently requesting it
        // (SettingsActivity, based on Android Studio-generated template,
        //  does not define its own support action bar. Hence the style to be used is of the
        //  variety that uses system's own action bar, rather than the default _NoActionBar variant)
        @StyleRes int themeId;
        switch (theme) {
            case LSScratchPadModel.THEME_DARK:
                themeId = ( activity instanceof SettingsActivity ? R.style.AppTheme_Dark :
                        R.style.AppTheme_Dark_NoActionBar_TransparentBG );
                break;
            case LSScratchPadModel.THEME_LIGHT:
                themeId = ( activity instanceof SettingsActivity ? R.style.AppTheme :
                        R.style.AppTheme_NoActionBar_TransparentBG );
                break;
            default:
                Log.w(TAG, "Unexpected theme option +[" + theme + "]. Use default");
                themeId = R.style.AppTheme;
        }
        return themeId;
    }

}
