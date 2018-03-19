package net.oldev.alsscratchpad;

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import net.oldev.alsscratchpad.LSScratchPadModel.ThemeOption;

import static net.oldev.alsscratchpad.AppCompatPreferenceUtil.bindPreferenceSummaryToValue;

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
        ThemeSwitcher.setTheme(new LSScratchPadModel(getApplicationContext()), this);

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

    // OPEN: declaring parent activity in AndroidManifest.xml has no effect,
    // I have to hardcode navigate up logic here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                Preference prefAutoThemeDarkTimeRange = findPreference(LSScratchPadModel.PREF_AUTO_THEME_DARK_TIME_RANGE);
                bindPreferenceSummaryToValue(prefAutoThemeDarkTimeRange);

                // data bind prefAutoThemeDarkTimeRange enabled/disabled based on current theme
                @ThemeOption String curTheme = new LSScratchPadModel(getActivity().getApplicationContext()).getTheme();
                updatePrefAutoThemeEnabledStatus(curTheme, prefAutoThemeDarkTimeRange);
            }

            { // Pref Theme (to use)
                Preference prefTheme = findPreference(LSScratchPadModel.PREF_THEME);
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
        }

        private void updatePrefAutoThemeEnabledStatus(@ThemeOption String themeOption,
                                           Preference prefAutoThemeDarkTimeRange) {
            boolean enabled = (LSScratchPadModel.THEME_AUTO.equals(themeOption));
            prefAutoThemeDarkTimeRange.setEnabled(enabled);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

    }

}
