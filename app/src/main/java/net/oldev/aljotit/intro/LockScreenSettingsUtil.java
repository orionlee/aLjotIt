package net.oldev.aljotit.intro;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LockScreenSettingsUtil {

    private static final String TAG = "LJI-Utils";

    /**
     * Attempt to open Lock screen settings screen.
     * As there is no standard, if it fails. It will fallback to open general
     * system settings.
     *
     * @param context
     */
    public static void tryToOpenLockScreenSettings(@NonNull Context context) {
        // fallback for general settings
        final Intent fallbackIntent = new Intent(Settings.ACTION_SETTINGS);

        // Intent to open lock screen settings
        // Since it is manufacturer/device dependent,
        // the following are a list of possible intents to try
        final List<Intent> lssIntents = new ArrayList<>();
        // Directly to Lock Screen options, with configurable shortcuts, on some Samsung devices
        lssIntents.add(newIntentByClassName("com.android.settings",
                "com.android.settings.Settings$LockScreenSettingsActivity"));

        // Directly to Lock Screen settings, with configurable quick access, on some ASUS devices
        lssIntents.add(newIntentByClassName("com.android.settings",
                "com.android.settings.Settings$AsusLockScreenSettingsActivity"));

        // More general lock screen settings on some Samsung devices.
        // Include it here, as the classname is generic enough that might reach some other devices
        lssIntents.add(newIntentByClassName("com.android.settings",
                "com.android.settings.Settings$LockscreenMenuActivity"));
        lssIntents.add(newIntentByClassName("com.android.settings",
                "com.android.settings.LockscreenMenuSettings"));

        for(Intent intent : lssIntents) {
            boolean success = safeStartActivity(context, intent, "Try next one.");
            if (success) {
                return;
            }
        }
        // All of the above fails. Try the last resort
        safeStartActivity(context, fallbackIntent, "No other fallback.");
    }

    @SuppressWarnings("SameParameterValue")
    private static @NonNull Intent newIntentByClassName(@NonNull String packageName, @NonNull String className) {
        Intent intent = new Intent();
        intent.setClassName(packageName, className);
        return intent;
    }

    private static boolean safeStartActivity(@NonNull Context context,
                                             @NonNull Intent intent,
                                             @NonNull String errMsg) {
        try {
            context.startActivity(intent);
            return true;
        } catch (Throwable t) {
            Log.v(TAG, "safeStartActivity() fails - " + errMsg + " | failed intent: <" + intent + ">", t);
            return false;
        }
    }

}
