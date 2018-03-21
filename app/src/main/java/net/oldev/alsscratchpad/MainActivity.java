package net.oldev.alsscratchpad;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LSSP-Main"; // Lock Screen Scratch Pad abbreviation

    private static final int REQUEST_CODE_SHARE_TEXT = 987;

    private EditText mScratchPad;
    private Menu mOptionsMenu;

    private LSScratchPadModel mModel;
    private final LockScreenReceiver mLockScreenReceiver = new HideOnLockScreenReceiver();


    public static final String EXTRA_START_FROM_LOCK_SCREEN =
            MainActivity.class.getPackage().getName() + ".EXTRA_START_FROM_LOCK_SCREEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        mModel = new LSScratchPadModel(getApplicationContext());
        LockScreenReceiver.registerToLockScreenChanges(this, mLockScreenReceiver);

        // setting attributes on the service declaration in AndroidManifest.xml
        // does not work for some reason android:showOnLockScreen="true", android:showOnLockScreen="true"
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        ThemeSwitcher.setTheme(mModel, this); // MUST be done before setContentView, consider setting the theme
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_SHARE_TEXT == requestCode) {
            promptUserToClearContent();
        } else {
            Log.e(TAG, "Unsupported requestCode " + requestCode);
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
            Log.d(TAG, "onOptionsItemSelected() - To be implemented. menuItem.id=" + id);
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
        super.onStop();
        String content = mScratchPad.getText().toString();
        mModel.setContentWithCursorIdx(content, mScratchPad.getSelectionStart());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Note: mLockScreenReceiver CANNOT be unregistered earlier, e.g., onStop().
        // If doing so, when using the app and the screen is locked, (i.e., unavailable to user),
        // onStop() will be invoked (and screen lock listening will be unregistered)
        // At the time, the app is still at the foreground visually speaking.
        // After that, when user clicks power, expecting to see lock screen, he/she
        // will see the Scratch Pad instead, because it has never been moved back in the first place.
        LockScreenReceiver.unregisterFromLockScreenChanges(this, mLockScreenReceiver);
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

    static final String GKEEP_PACKAGE_NAME = "com.google.android.keep";
    static final String GKEEP_CLASSNAME = "com.google.android.keep.activities.ShareReceiverActivity";

    // Standard share UI
    private void sendToShareChooser() {
        sendTo(null, null);
    }

    private void sendToKeep() {
        /// Use Keep
        sendTo(GKEEP_CLASSNAME, GKEEP_CLASSNAME);
    }

    private void sendTo(@Nullable String packageName, @Nullable String className) {
        if (isDeviceLocked()) {
            notifyUserSentDisabledOnLockscreen();
            return;
        }
        // else normal workflow
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, mScratchPad.getText().toString()); // MUST cast to string or it won't be accepted by google Keep
        intent.setType("text/plain"); // MUST be set for the system the share chooser to show up.

        /// Use Keep
        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
            intent.setClassName("com.google.android.keep", "com.google.android.keep.activities.ShareReceiverActivity");
        }  else {
            // Customized chooser label
            String labelText = getString(R.string.label_send_to);
            SpannableString chooserLabel = new SpannableString(labelText);
            chooserLabel.setSpan(new TextAppearanceSpan(this,
                                                        android.R.style.TextAppearance_Material_Medium),
                                 0, labelText.length(), 0); // larger that default
            intent = intent.createChooser(intent, chooserLabel);
        }
        ///Log.e(TAG, "text to send: " + textView.getText());

        startActivityForResult(intent, REQUEST_CODE_SHARE_TEXT);

    }

    //
    // Lock Screen UI customization logic
    //

    private boolean mSendPostponed = false; // PENDING: might need better persistence

    private void customizeMainUiForLockScreen() {
        Log.v(TAG, "customizeMainUiForLockScreen()");
        final boolean locked = isDeviceLocked();

        // LATER: make disabled color defined in resources?
        final @ColorInt int color = locked ? Color.LTGRAY : getResources().getColor(R.color.keepBackground);

        // Make the button look disabled
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(color));

        // TODO: add a lock icon with android.R.drawable.ic_lock_lock?!
    }

    private void customizeOptionsMenuForLockScreen(@NonNull Menu optionsMenu) {
        Log.v(TAG, "customizeOptionMenuForLockScreen()");

        final boolean locked = isDeviceLocked();

        final boolean visible = locked ? false : true;

        // Hide all send UIs
        // if I disable them rather than making them invisible
        // the send menu will be grayed out correctly, however,
        // send_to_keep, being an icon, shows no visible change, and is confusing.
        optionsMenu.findItem(R.id.action_send_to_keep).setVisible(visible);
        optionsMenu.findItem(R.id.action_share).setVisible(visible);

    }

    private boolean isDeviceLocked() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = km.inKeyguardRestrictedInputMode();
        Log.v(TAG, "isDeviceLocked()  locked :" + locked);
        return locked;
    }

    private void notifyUserSentDisabledOnLockscreen() {
        mSendPostponed = true;

        Snackbar.make(findViewById(R.id.activity_main), "Wait until the screen is unlocked to send.",
                      Snackbar.LENGTH_LONG)
                .show();
    }

    private class HideOnLockScreenReceiver extends LockScreenReceiver {
        @Override
        protected void onLocked() {
            // hide the app when the screen is locked, so that it will not stay
            // on lock screen uninvited.
            moveTaskToBack(true);
        }

        @Override
        protected void onUnlocked() {
            Log.v(TAG, "HideOnLockScreenReceiver.onUnlocked()");
            if (mSendPostponed) {
                try {
                    // delay showing snack bar as unlocking screen takes time
                    // with no delay, the snack bar will be shown prematurely,
                    // when the user cannot fully see the screen yet.
                    final Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        // OPEN: the action does not work on custom toast
                        showSnackBarLikeToast("Note not sent yet.",
                                              "View", (v) -> {
                                Log.v(TAG, "  in Snackbar toast OnClickListener");
                                Intent intent = new Intent(MainActivity.this,
                                                           MainActivity.class);

                                Log.v(TAG,"  back to MainActivity. intent:" + intent);
                                MainActivity.this.startActivity(intent);
                                });
                    }, 1000);
                } finally {
                    mSendPostponed = false;
                }
            }
        }

        private void showSnackBarLikeToast(@NonNull String msg,
                                           @Nullable String actionText,
                                           @Nullable View.OnClickListener actionOnClickListener) {
            //inflate the custom toast
            View layout = getLayoutInflater().inflate(R.layout.snackbar_like_toast,
                                                      (ViewGroup) findViewById(R.id.snackbar_like_toast));

            // Set the Text to show in TextView
            TextView text = (TextView)layout.findViewById(R.id.snackbar_text);
            text.setText(msg);

            if (!TextUtils.isEmpty(actionText) && actionOnClickListener != null) {
                Button button = (Button) layout.findViewById(R.id.snackbar_action);
                button.setText(actionText);
                button.setOnClickListener(actionOnClickListener);
            }
            Toast toast = new Toast(getApplicationContext());

            //Setting up toast position, similar to Snackbar
            toast.setGravity(Gravity.BOTTOM | Gravity.LEFT | Gravity.FILL_HORIZONTAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    }

}