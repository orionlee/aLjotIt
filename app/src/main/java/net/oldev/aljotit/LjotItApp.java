package net.oldev.aljotit;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import net.oldev.aljotit.lsn.LockScreenNotificationService;

public class LjotItApp extends Application {

    private static final String TAG = "LJI-App";
    
    private final MainLockScreenReceiverManager mMainLockScreenReceiverManager =
            new MainLockScreenReceiverManager();

    private LjotItModel mModel;

    /**
     * Convenience helper to access LjotItApp instance
     */
    public static LjotItApp getApp(@NonNull Activity activity) {
        return ((LjotItApp)activity.getApplication());
    }

    public static LjotItApp getApp(@NonNull Service service) {
        return ((LjotItApp)service.getApplication());
    }
    
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
                AbstractLockScreenReceiver.registerToLockScreenChanges(LjotItApp.this, mLockScreenReceiver);
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
                    try {
                        AbstractLockScreenReceiver.unregisterFromLockScreenChanges(LjotItApp.this,
                                                                           mLockScreenReceiver);
                    } catch (Throwable t) {
                        // In some edge case, the MainLockScreenReceiver is not registered
                        // (or has been unregistered), unregister it here would
                        // result in IllegalArgumentException and causes the app to crash
                        // handle it by logging such cases
                        Log.w(TAG, "unregister from lock screen changes failed unexpectedly", t);
                    } finally {
                        mLockScreenReceiverRegistered = false;
                    }
                }
            }

            mLatestActivity = null;
        }
    }

    // Used by other components
    public LjotItModel getModel() {
        return mModel;
    }
    
    /**
     * Control MainActivity's UI behavior by listening to
     * whether the screen is locked / unlocked.
     */
    private class MainLockScreenReceiver extends AbstractLockScreenReceiver {

        private @Nullable
        Activity getLatestActivity() {
            return mMainLockScreenReceiverManager.getLatestActivity();
        }

        @Override
        protected @NonNull String tag() { return TAG; }

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
                Toast.makeText(getApplicationContext(), R.string.msg_auto_open_app_post_unlock,
                               Toast.LENGTH_SHORT).show();

                MainActivity.startFromOutsideActivityContext(getApplicationContext());
            }
        }

// --Commented out by Inspection START (4/20/2018 12:46 PM):
//        // OPEN: remove it if action cannot be made working.
//        @SuppressLint("RtlHardcoded")
//        private void showSnackBarLikeToast(@NonNull String msg,
//                                           @Nullable String actionText,
//                                           @Nullable View.OnClickListener actionOnClickListener) {
//            //inflate the custom toast
//            View layout = getLatestActivity()
//                    .getLayoutInflater()
//                    .inflate(R.layout.snackbar_like_toast,
//                             (ViewGroup) getLatestActivity().findViewById(R.id.snackbar_like_toast));
//
//            // Set the Text to show in TextView
//            TextView text = (TextView) layout.findViewById(R.id.snackbar_text);
//            text.setText(msg);
//
//            if (!TextUtils.isEmpty(actionText) && actionOnClickListener != null) {
//                Button button = (Button) layout.findViewById(R.id.snackbar_action);
//                button.setText(actionText);
//                button.setOnClickListener(actionOnClickListener);
//            }
//            Toast toast = new Toast(getApplicationContext());
//
//            //Setting up toast position, similar to Snackbar
//            toast.setGravity(Gravity.BOTTOM | Gravity.LEFT | Gravity.FILL_HORIZONTAL, 0, 0);
//            toast.setDuration(Toast.LENGTH_LONG);
//            toast.setView(layout);
//            toast.show();
//        }
// --Commented out by Inspection STOP (4/20/2018 12:46 PM)

    }
}
