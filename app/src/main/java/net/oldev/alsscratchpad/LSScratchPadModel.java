package net.oldev.alsscratchpad;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LSScratchPadModel {
    // preference file name for the text
    private static final String P_CONTENT = "net.oldev.alsscratchpad";

    private static final String PREF_CONTENT = "content";

    // preference file name for settings (typically exposed in UI)
    private static final String P_SETTINGS  = BuildConfig.APPLICATION_ID + "_preferences";

    // used by UI code
    static final String PREF_THEME = "theme";

    public static final String THEME_LIGHT = "LIGHT";
    public static final String THEME_DARK = "DARK";
    public static final String THEME_AUTO = "AUTO";

    @StringDef({THEME_LIGHT, THEME_DARK, THEME_AUTO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ThemeOption {}

    // used by UI code
    static final String PREF_AUTO_THEME_DARK_TIME_RANGE = "autoThemeDarkTimeRange";


    private final @NonNull
    Context mContext;

    public LSScratchPadModel(@NonNull Context context) {
        mContext = context;
    }


    // Access Scratch Pad text

    public void setContent(@NonNull String content) {
        setPref(P_CONTENT, PREF_CONTENT, content);
    }

    public @NonNull
    String getContent() {
        return getStringPref(P_CONTENT, PREF_CONTENT, "");
    }

    // Access Settings

    public @NonNull
    @ThemeOption
    String getTheme() {
        return getStringPref(P_SETTINGS, PREF_THEME, THEME_LIGHT);
    }

    public void setTheme(@NonNull @ThemeOption String theme) {
        setPref(P_SETTINGS, PREF_THEME, theme);
    }

    public @NonNull TimeRange getAutoThemeDarkTimeRange() {
        String timeRangeStr = getStringPref(P_SETTINGS,
                                            PREF_AUTO_THEME_DARK_TIME_RANGE,
                                            "[23:00,07:00]");
        return TimeRange.parse(timeRangeStr);
    }


    //
    // SharedPreferences helpers
    //


    private @NonNull
    SharedPreferences getPreferences(@NonNull String prefName) {
        SharedPreferences prefs =
                mContext.getSharedPreferences(prefName,
                                              Context.MODE_PRIVATE);
        return prefs;
    }

    private @NonNull
    String getStringPref(@NonNull String prefName,
                         @NonNull String key, @NonNull String defValue) {
        return getPreferences(prefName).getString(key, defValue);
    }

    private void setPref(@NonNull String prefName,
                         @NonNull String key, @NonNull String value) {
        SharedPreferences.Editor editor = getPreferences(prefName).edit();
        editor.putString(key, value);

        // use asynchronous apply. I can't handle commit() failures other than reporting it anyway.
        editor.apply();
    }

}
