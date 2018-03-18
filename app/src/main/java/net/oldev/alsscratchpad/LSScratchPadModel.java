package net.oldev.alsscratchpad;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LSScratchPadModel {
    private static final String PREFERENCES_KEY = "net.oldev.alsscratchpad";
    private static final String PREF_CONTENT = "content";

    static final String PREF_THEME = "theme"; // used by UI code

    public static final String THEME_LIGHT = "LIGHT";
    public static final String THEME_DARK = "DARK";
    public static final String THEME_AUTO = "AUTO";

    @StringDef({THEME_LIGHT, THEME_DARK, THEME_AUTO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ThemeOption {
    }

    private final @NonNull
    Context mContext;

    public LSScratchPadModel(@NonNull Context context) {
        mContext = context;
    }


    // Access Scratch Pad text

    public void setContent(@NonNull String content) {
        setPref(PREF_CONTENT, content);
    }

    public @NonNull
    String getContent() {
        return getStringPref(PREF_CONTENT, "");
    }

    // Access Settings

    public @NonNull
    @ThemeOption
    String getTheme() {
        return getStringPref(PREF_THEME, THEME_LIGHT);
    }

    public void setTheme(@NonNull @ThemeOption String theme) {
        setPref(PREF_THEME, theme);
    }


    //
    // SharedPreferences helpers
    //

    private @NonNull
    SharedPreferences getPreferences() {
        SharedPreferences prefs =
                mContext.getSharedPreferences(PREFERENCES_KEY,
                                              Context.MODE_PRIVATE);
        return prefs;
    }

    private @NonNull
    String getStringPref(@NonNull String key, @NonNull String defValue) {
        return getPreferences().getString(key, defValue);
    }

    private void setPref(@NonNull String key, @NonNull String value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(key, value);

        // use asynchronous apply. I can't handle commit() failures other than reporting it anyway.
        editor.apply();
    }

}
