package net.oldev.aljotit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Provides access to
 * 1. the states (content, cursor position, etc.) of the editor, and
 * 2. preferences / settings used by the application
 *
 */
public class LjotItModel {
    // preference file name for the text
    private static final String P_CONTENT = "net.oldev.aljotit";

    private static final String PREF_CONTENT = "content";
    private static final String PREF_CONTENT_CURSOR_IDX = "contentCursorIdx";
    private static final String PREF_STATE_SEND_POSTPONED = "SendPostponed";

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
    static final String PREF_LOCK_SCREEN_NOTIFICATION_ENABLED = "lockScreenNotificationEnabled";

    private final @NonNull
    Context mContext;

    public LjotItModel(@NonNull Context context) {
        mContext = context;
    }


    // Access Scratch Pad text

    public void setContentWithCursorIdx(@NonNull String content, int cursorIdx) {
        setContent(content);
        // Validate to ensure cursorIdx is proper within content?
        setPref(P_CONTENT, PREF_CONTENT_CURSOR_IDX, cursorIdx);
    }

    /**
     * Append the supplied text to the existing content.
     */
    public void appendToContent(String textToAdd) {
        if (!TextUtils.isEmpty(textToAdd)) {
            final String contentCurrent = getContent();
            // The new text starts at a new line, if there is any existing one.
            final String contentNew = TextUtils.isEmpty(contentCurrent) ? textToAdd :
                    contentCurrent + "\n" + textToAdd;
            setContent(contentNew);
        } // else nothing needs to be done.
    }

    // Note: setContent is set to private as being used by outside callers
    // arbitrary is potentially dangerous, as the model also maintains the cursor
    // position within the content.
    private void setContent(@NonNull String content) {
        setPref(P_CONTENT, PREF_CONTENT, content);
    }

    public @NonNull
    String getContent() {
        return getStringPref(P_CONTENT, PREF_CONTENT, "");
    }

    public int getContentCursorIdx() {
        return getIntPref(P_CONTENT, PREF_CONTENT_CURSOR_IDX, -1);
    }

    public boolean isSendPostponed() {
        return getBooleanPref(P_CONTENT, PREF_STATE_SEND_POSTPONED, false);
    }

    public void setSendPostponed(boolean isPostponed) {
        setPref(P_CONTENT, PREF_STATE_SEND_POSTPONED, isPostponed);
    }

    // Access Settings

    public static final String DEFAULT_AUTO_THEME_DARK_TIME_RANGE = "[23:00,07:00]";
    
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
                                            DEFAULT_AUTO_THEME_DARK_TIME_RANGE);
        return TimeRange.parse(timeRangeStr);
    }

    public boolean isUIWidgetStyle() {
        // OPEN: might allow end users to change it.
        // For now, it is hardcoded while Widget-style UI is being experimented.
        // The switch lets developers to change back to normal full screen relatively quickly.
        return true;
    }

    // lock screen notification defaulted to true for Android 5 /6 devices,
    // which do not support QSTile, the preferred way to access the app from lock screen.
    private final boolean mLockScreenNotificationEnabledDefault =
            ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.N );

    public boolean isLockScreenNotificationEnabled() {
        return getBooleanPref(P_SETTINGS, PREF_LOCK_SCREEN_NOTIFICATION_ENABLED,
                              mLockScreenNotificationEnabledDefault);
    }


    public void setLockScreenNotificationEnabled(boolean enabled) {
        setPref(P_SETTINGS, PREF_LOCK_SCREEN_NOTIFICATION_ENABLED, enabled);

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

    private int getIntPref(@NonNull String prefName,
                         @NonNull String key, int defValue) {
        return getPreferences(prefName).getInt(key, defValue);
    }

    private boolean getBooleanPref(@NonNull String prefName,
                                   @NonNull String key, boolean defValue) {
        return getPreferences(prefName).getBoolean(key, defValue);
    }

    private void setPref(@NonNull String prefName,
                         @NonNull String key, @NonNull String value) {
        SharedPreferences.Editor editor = getPreferences(prefName).edit();
        editor.putString(key, value);

        // use asynchronous apply. I can't handle commit() failures other than reporting it anyway.
        editor.apply();
    }

    private void setPref(@NonNull String prefName,
                         @NonNull String key, int value) {
        SharedPreferences.Editor editor = getPreferences(prefName).edit();
        editor.putInt(key, value);

        // use asynchronous apply. I can't handle commit() failures other than reporting it anyway.
        editor.apply();
    }

    private void setPref(@NonNull String prefName,
                         @NonNull String key, boolean value) {
        SharedPreferences.Editor editor = getPreferences(prefName).edit();
        editor.putBoolean(key, value);

        // use asynchronous apply. I can't handle commit() failures other than reporting it anyway.
        editor.apply();
    }

}
