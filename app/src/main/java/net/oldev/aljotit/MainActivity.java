package net.oldev.aljotit;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LJI-Main"; // Lock Screen Scratch Pad abbreviation

    private static final int REQUEST_CODE_SHARE_TEXT = 987;

    private EditText mScratchPad;
    private Menu mOptionsMenu;
    private @StyleRes int mThemeId; // id of the theme used

    private LjotItModel mModel;


    public static final String EXTRA_START_FROM_LOCK_SCREEN =
            MainActivity.class.getPackage().getName() + ".EXTRA_START_FROM_LOCK_SCREEN";

    public static void startFromOutsideActivityContext(@NonNull Context startContext) {
        Intent mainIntent = getStartActivityIntentFromOutsideActivityContext(startContext);
        startContext.getApplicationContext().startActivity(mainIntent);
    }

    public static @NonNull Intent getStartActivityIntentFromOutsideActivityContext(@NonNull Context startContext) {
        Intent mainIntent = new Intent(startContext.getApplicationContext(), MainActivity.class);
        // FLAG_ACTIVITY_NEW_TASK is a requirement for starting activity
        // from Services, BroadcastReceivers, etc., to avoid ActivityManager from
        // throwing exception about startActivity from outside of an activity context
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return mainIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        mModel = new LjotItModel(getApplicationContext());

        // setting attributes on the service declaration in AndroidManifest.xml
        // does not work for some reason android:showOnLockScreen="true", android:showOnLockScreen="true"
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        mThemeId = ThemeSwitcher.setTheme(mModel, this); // MUST be done before setContentView, consider setting the theme
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mScratchPad = (EditText)findViewById(R.id.scratch_pad_content);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((view) -> { sendToKeep(); });
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart()");
        super.onStart();

        // Switch theme if the preference is changed, e.g., returning to this screen
        // after changing the theme in SettingsActivity
        // Also useful for auto theme case when the app is brought back to foreground
        // and the theme should be changed due to time.
        if (mThemeId != ThemeSwitcher.findThemeIdByOption(mModel, this)) {
            Log.v(TAG, "  Theme changed. Restart activity...");
            ThemeSwitcher.refreshActivity(this);
            return;
        }

        // data binding
        // @see #onStop() for when content gets persisted.
        // onStart is the correspond life cycle method, so it is used to load the content
        mScratchPad.setText(mModel.getContent());
        int cursorIdx = mModel.getContentCursorIdx();
        if (cursorIdx >= 0) {
            mScratchPad.setSelection(cursorIdx);
        }

        customizeMainUiForLockScreen();
        // Depending on activity is brought up, menu may or may not be initialized at this point
        // Case starting from new:
        //   onStart() [no menu yet], then onOptionsMenuCreated()
        // Case the activity is brought back up from background
        //   onStart() [menu is there], onOptionsMenuCreated() is NOT invoked.
        // Therefore, menu customization needs to be done at both entry points.
        if (mOptionsMenu != null) {
            customizeOptionsMenuForLockScreen(mOptionsMenu);
        }

        // For post lock screen workflow
        if (mModel.isSendPostponed()) {
            mModel.setSendPostponed(false);
            sendToKeep();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_SHARE_TEXT == requestCode) {
            // UI tweak: temporarily hide soft keyboard as it might obstruct snackbar messages
            // after the prompt
            hideSoftKeyboard();
            promptUserToClearContent();
        } else {
            Log.w(TAG, "onActivityResult() - Unsupported requestCode " + requestCode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mOptionsMenu = menu;
        customizeOptionsMenuForLockScreen(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_send_to_keep) {
            sendToKeep();
            return true;
        } else if (id == R.id.action_share) {
            sendToShareChooser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Persist content. onStop is used as it is the life cycle method
     * when the activity will get destroyed.
     *
     * @see https://developer.android.com/guide/components/activities/activity-lifecycle.html
     */
    @Override
    protected void onStop() {
        Log.v(TAG, "onStop()");
        super.onStop();
        String content = mScratchPad.getText().toString();
        mModel.setContentWithCursorIdx(content, mScratchPad.getSelectionStart());
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
    }


    //
    // Clear content implementation
    //

    private void promptUserToClearContent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.prompt_clear_all_after_sent)
               .setNegativeButton(R.string.prompt_clear_all_no, (d, w) -> {})
               .setPositiveButton(R.string.prompt_clear_all_yes, (d, w) -> { clearContent(); })
               .show();
    }

    private void clearContent() {
        String oldContent = mScratchPad.getText().toString();
        mScratchPad.setText("");

        Snackbar.make(findViewById(R.id.activity_main), R.string.msg_clear_all_done,
                      Snackbar.LENGTH_LONG)
                .setAction(R.string.action_clear_all_undo, v -> {
                    mScratchPad.setText(oldContent);
                })
                .show();
    }


    //
    // Actual sendTo action implementation
    //

    // Standard share UI
    private void sendToShareChooser() {
        sendTo(null, null);
    }

    private void sendToKeep() {
        /// Use Keep
        sendTo(mModel.getSendToPreferredPackageName(),
               mModel.getSendToPreferredClassName());
    }

    private void sendTo(@Nullable String packageName, @Nullable String className) {
        if (isDeviceLocked()) {
            notifyUserSentDisabledOnLockscreen();
            return;
        }
        // else normal workflow

        // Core logic
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, mScratchPad.getText().toString()); // MUST cast to string or it won't be accepted by google Keep
        intent.setType("text/plain"); // MUST be set for the system the share chooser to show up.

        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
            intent.setClassName(packageName, className);
        }  else { // Send-to activity not specified. Let user decides.
            // Customized chooser label
            String labelText = getString(R.string.label_send_to);
            SpannableString chooserLabel = new SpannableString(labelText);
            chooserLabel.setSpan(new TextAppearanceSpan(this,
                                                        android.R.style.TextAppearance_Material_Medium),
                                 0, labelText.length(), 0); // larger that default
            intent = intent.createChooser(intent, chooserLabel);
        }

        try {
            startActivityForResult(intent, REQUEST_CODE_SHARE_TEXT);
        } catch (ActivityNotFoundException anfe) {
            Toast.makeText(this, R.string.msg_err_note_app_not_found, Toast.LENGTH_LONG).show();
            Log.w(TAG, "sendTo() - target note application is not found", anfe);
        }

    }

    private void hideSoftKeyboard() {
        ((InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mScratchPad.getWindowToken(), 0);
    }


    //
    // Lock Screen UI customization logic
    //

    private void customizeMainUiForLockScreen() {
        Log.v(TAG, "  customizeMainUiForLockScreen()");
        final boolean locked = isDeviceLocked();

        // LATER: make disabled color defined in resources?
        final @ColorInt int color = locked ? Color.LTGRAY : getResources().getColor(R.color.keepBackground);

        // Make the button look disabled
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(color));

        // Show an indicator on action bar to remind the user the Scratch Pad is on lock screen
        ActionBar actionBar = getSupportActionBar();
        if (locked) {
            actionBar.setDisplayShowHomeEnabled(true);
            // OPEN: Can't figure out how to specify the icon
            // in main_content.xml or styles.xml so it is hardcoded here.
            actionBar.setIcon(R.drawable.ic_menu_lock);
        } else {
            actionBar.setDisplayShowHomeEnabled(false);
            // setDisplayShowHomeEnabled to false is sufficient actionBar.setIcon(null);
        }
    }

    private void customizeOptionsMenuForLockScreen(@NonNull Menu optionsMenu) {
        Log.v(TAG, "  customizeOptionMenuForLockScreen()");

        final boolean locked = isDeviceLocked();

        final boolean visible = locked ? false : true;

        // Hide all send UIs
        // if I disable them rather than making them invisible
        // the send menu will be grayed out correctly, however,
        // send_to_keep, being an icon, shows no visible change, and is confusing.
        optionsMenu.findItem(R.id.action_send_to_keep).setVisible(visible);
        optionsMenu.findItem(R.id.action_share).setVisible(visible);
        optionsMenu.findItem(R.id.action_settings).setVisible(visible);

    }

    boolean isDeviceLocked() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = km.inKeyguardRestrictedInputMode();
        Log.v(TAG, "  isDeviceLocked()  locked :" + locked);
        return locked;
    }

    private void notifyUserSentDisabledOnLockscreen() {
        mModel.setSendPostponed(true);

        Snackbar.make(findViewById(R.id.activity_main), "Wait until the screen is unlocked to send.",
                      Snackbar.LENGTH_LONG)
                .show();
    }

}