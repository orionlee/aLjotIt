package net.oldev.aljotit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import net.oldev.aljotit.LjotItModel.ThemeOption;

import static net.oldev.aljotit.AppCompatPreferenceUtil.bindPreferenceSummaryToValue;

// Simplified from Android Studio-generated template, which supports
// a more complex two-pane(screen) UI.
public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Theme must be set before calling super (which does some view inflation)
        // Note: MainActivity has similar code, but they cannot be abstracted into common
        // superclass because they inherit from different base class.
        // Furthermore, using common superclass to enforce the behavior might not be desirable
        // to begin with (too rigid): Theme-aware activity is better described as an aspect / mix-in.
        ThemeSwitcher.setTheme(new LjotItModel(getApplicationContext()), this);

        super.onCreate(savedInstanceState);
        setupActionBar();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(android.R.id.content, new GeneralPreferenceFragment());
        fragmentTransaction.commit();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        public static final String CATEGORY_KEY_UI = "pref_category_ui";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //
            // Each Preference has its own additional specific logic
            //
            { // Pref auto theme dark time range
                Preference prefAutoThemeDarkTimeRange = findPreference(LjotItModel.PREF_AUTO_THEME_DARK_TIME_RANGE);
                bindPreferenceSummaryToValue(prefAutoThemeDarkTimeRange);

                // data bind prefAutoThemeDarkTimeRange enabled/disabled based on current theme
                @ThemeOption String curTheme = new LjotItModel(getActivity().getApplicationContext()).getTheme();
                updatePrefAutoThemeEnabledStatus(curTheme, prefAutoThemeDarkTimeRange);
            }

            { // Pref Theme (to use)
                Preference prefTheme = findPreference(LjotItModel.PREF_THEME);
                bindPreferenceSummaryToValue(prefTheme);

                // Refresh Settings itself upon theme change
                // Listen to the changes on the UI level (perfTheme) is sufficient.
                //
                // If I listen to the changes at the model level (SharedPreference itself),
                // the result is somehow not reliable: Change listener is on fired intermittently.
                prefTheme.setOnPreferenceChangeListener((preference, newValue) -> {
                    ThemeSwitcher.refreshActivity(getActivity());
                    // the activity is restarted. no further data binding update work is needed.
                    // Otherwise prefAutoThemeDarkTimeRange will need updates.
                    return true;
                });
            }

            { // Pref Lock screen notification
                Preference prefLSN = findPreference(LjotItModel.PREF_LOCK_SCREEN_NOTIFICATION_ENABLED);

                LjotItModel model = LjotItApp.getApp(getActivity()).getModel();
                if (model.isLockScreenNotificationSupported()) {
                    checkLockScreenNotificationSettings(getActivity());

                    prefLSN.setOnPreferenceChangeListener((preference, newValue) -> {
                        boolean newBVal = Boolean.parseBoolean(newValue.toString());
                        checkLockScreenNotificationSettings(getActivity(), newBVal);
                        return true;
                    });
                } else { // case lock screen notification not supported, aka Android 4 devices
                    removePreferenceFromCategory(prefLSN, CATEGORY_KEY_UI);
                }

            }
        }

        private void updatePrefAutoThemeEnabledStatus(@ThemeOption String themeOption,
                                           Preference prefAutoThemeDarkTimeRange) {
            boolean enabled = (LjotItModel.THEME_AUTO.equals(themeOption));
            prefAutoThemeDarkTimeRange.setEnabled(enabled);
        }

        @SuppressWarnings("SameParameterValue")
        private void removePreferenceFromCategory(Preference preference, String categoryKey) {
            PreferenceCategory category = (PreferenceCategory)findPreference(categoryKey);
            category.removePreference(preference);
        }

        //
        // Lock Screen Notification Preference helpers
        //

        private static void checkLockScreenNotificationSettings(@NonNull Activity ctx) {
            boolean enabledInPreference =
                    ((LjotItApp)ctx.getApplication()).getModel().isLockScreenNotificationEnabled();
            checkLockScreenNotificationSettings(ctx, enabledInPreference);
        }

        private static void checkLockScreenNotificationSettings(@NonNull Activity ctx, boolean enabledInPreference) {
            // OPEN: using Snackbar to show the warning is rather clumsy.
            // consider place it inline to the Lock Screen Notification Preference
            // using a custom layout (or maybe just widgetLayout)
            if ( enabledInPreference &&
                    !LockScreenNotificationReceiver.isNotificationEnabledInSystem(ctx) ) {
                final Snackbar snackbar = Snackbar.make(ctx.findViewById(android.R.id.content),
                                                        R.string.msg_warn_app_notifications_disabled,
                                                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.text_dismiss, v -> {
                    snackbar.dismiss();
                    promptUserToEnableAppNotifications(ctx);
                });
                snackbar.show();
            }
        }

        private static void promptUserToEnableAppNotifications(@NonNull Context ctx) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage(R.string.prompt_enable_app_notifications)
                   .setNegativeButton(R.string.text_no, (d, w) -> {})
                   .setPositiveButton(R.string.text_yes, (d, w) ->
                           LockScreenNotificationReceiver.startAppNotificationSettingsActivity(ctx))
                   .show();
        }

    }

}
