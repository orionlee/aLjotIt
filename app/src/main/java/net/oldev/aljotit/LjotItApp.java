package net.oldev.aljotit;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LjotItApp extends Application {

    private static final String TAG = "LSSP-App";
    
    private final MainLockScreenReceiverManager mMainLockScreenReceiverManager =
            new MainLockScreenReceiverManager();

    private LjotItModel mModel;


    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate()");
        super.onCreate();
        mModel = new LjotItModel(getApplicationContext());
        registerActivityLifecycleCallbacks(mMainLockScreenReceiverManager);

        // Long-running background service for notification on lockscreen feature,
        // targeted for Android 5/6 devices
        startService(new Intent(getApplicationContext(),
                                LockScreenNotificationService.class));
    }


    /**
     * Manages MainLockScreenReceiver
     * - life cycle, and registration /un-registration (as broadcast receiver)
     * - any necessary references (MainActivity) it needs.
     *
     * OPEN: consider splitting tracking MainLockScreenReceiver and tracking MainActivity
     * to 2 separate classes, as they are really 2 orthogonal aspects that do not necessarily
     * tie together, other than the current usage
     * where MainLockScreenReceiver needs to find MainActivity. As it is the codes are
     * too inter-dependent.
     */
    private class MainLockScreenReceiverManager implements ActivityLifecycleCallbacks {

        private final MainLockScreenReceiver mLockScreenReceiver = new MainLockScreenReceiver();
        private boolean mLockScreenReceiverRegistered = false;

        /**
         * Track the last activity instance that the user sees
         * It is used by MainLockScreenReceiver#onLocked() to
         * hide MainActivity from screen when the screen is locked.
         */
        private @Nullable Activity mLatestActivity = null;

        // used by MainLockScreenReceiver
        public @Nullable Activity getLatestActivity() {
            return mLatestActivity;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.v(TAG, "onActivityCreated()");
            if (!mLockScreenReceiverRegistered) {
                LockScreenReceiver.registerToLockScreenChanges(LjotItApp.this, mLockScreenReceiver);
                mLockScreenReceiverRegistered = true;
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            mLatestActivity = activity;
        }

        @Override
        public void onActivityResumed(Activity activity) {}

        @Override
        public void onActivityPaused(Activity activity) {}

        @Override
        public void onActivityStopped(Activity activity) {
            // Note: I cannot set mLatestActivity to null here, because for
            // Lock screen case, if MainActivity is on foreground,
            // the sequence of event would be:
            // 1. User presses power to turn off and lock the screen.
            // 2. <screen is off>
            // 3. MainActivity.onStop() will be called, because it is
            // not visible anymore , triggering onActivityStopped() here
            // 4. MainLockScreenReceiver.onLocked() will be called,
            //    *AFTER* MainActivity.onStop()
            // By the time MainLockScreenReceiver.onLocked()
            // is called, it still needs a reference to MainActivity instance.
            // Hence, the instance cannot be cleared here.
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.v(TAG, "onActivityDestroyed()");
            // Unregister mLockScreenReceiver if MainActivity is gone
            //
            // Note: mLockScreenReceiver CANNOT be unregistered earlier, e.g., onStop().
            // If doing so, when using the app and the screen is locked, (i.e., unavailable to user),
            // onStop() will be invoked (and screen lock listening will be unregistered)
            // At the time, the app is still at the foreground visually speaking.
            // After that, when user clicks power, expecting to see lock screen, he/she
            // will see the Scratch Pad instead, because it has never been moved back in the first place.
            if (activity instanceof  MainActivity) {
                MainActivity mainActivity = (MainActivity)activity;
                if (!mainActivity.isDeviceLocked()) {
                    LockScreenReceiver.unregisterFromLockScreenChanges(LjotItApp.this,
                                                                       mLockScreenReceiver);
                    mLockScreenReceiverRegistered = false;
                }
            }

            mLatestActivity = null;
        }
    }

    /**
     * Control MainActivity's UI behavior by listening to
     * whether the screen is locked / unlocked.
     */
    private class MainLockScreenReceiver extends LockScreenReceiver {

        private @Nullable
        Activity getLatestActivity() {
            return mMainLockScreenReceiverManager.getLatestActivity();
        }

        @Override
        protected void onLocked() {
            Log.v(TAG, "MainLockScreenReceiver.onLocked()");
            // hide the app when the screen is locked, so that it will not stay
            // on lock screen uninvited.
            if (getLatestActivity() != null) {
                getLatestActivity().moveTaskToBack(true);
            }
        }

        @Override
        protected void onUnlocked() {
            Log.v(TAG, "MainLockScreenReceiver.onUnlocked()");
            if (mModel.isSendPostponed()) {
                // Start MainActivity takes some noticeable delay
                // Show a toast to let user know what to expect.
                Toast.makeText(getApplicationContext(), "Opening LS Scratch Pad to send the note...",
                               Toast.LENGTH_SHORT).show();

                MainActivity.startFromOutsideActivityContext(getApplicationContext());
            }
        }

        // OPEN: remove it if action cannot be made working.
        private void showSnackBarLikeToast(@NonNull String msg,
                                           @Nullable String actionText,
                                           @Nullable View.OnClickListener actionOnClickListener) {
            //inflate the custom toast
            View layout = getLatestActivity()
                    .getLayoutInflater()
                    .inflate(R.layout.snackbar_like_toast,
                             (ViewGroup) getLatestActivity().findViewById(R.id.snackbar_like_toast));

            // Set the Text to show in TextView
            TextView text = (TextView) layout.findViewById(R.id.snackbar_text);
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
