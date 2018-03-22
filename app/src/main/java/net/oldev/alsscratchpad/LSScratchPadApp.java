package net.oldev.alsscratchpad;

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

public class LSScratchPadApp extends Application {

    private static final String TAG = "LSSP-App";
    
    private final LockScreenReceiver mLockScreenReceiver = new HideOnLockScreenReceiver();
    private final ActivityLifeCycleTracker mActivityLifeCycleTracker = new ActivityLifeCycleTracker();

    private boolean mLockScreenReceiverRegistered = false;
    private LSScratchPadModel mModel;


    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate()");
        super.onCreate();
        mModel = new LSScratchPadModel(getApplicationContext());
        registerActivityLifecycleCallbacks(mActivityLifeCycleTracker);
    }


    private class ActivityLifeCycleTracker implements ActivityLifecycleCallbacks {
        /**
         * Track the last activity instance that the user sees
         * It is used by HideOnLockScreenReceiver#onLocked() to
         * hide MainActivity from screen when the screen is locked.
         */
        private @Nullable Activity mLatestActivity = null;

        public @Nullable Activity getLatestActivity() {
            return mLatestActivity;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.v(TAG, "onActivityCreated()");
            if (!mLockScreenReceiverRegistered) {
                LockScreenReceiver.registerToLockScreenChanges(LSScratchPadApp.this, mLockScreenReceiver);
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
            // 4. HideOnLockScreenReceiver.onLocked() will be called,
            //    *AFTER* MainActivity.onStop()
            // By the time HideOnLockScreenReceiver.onLocked()
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
                    LockScreenReceiver.unregisterFromLockScreenChanges(LSScratchPadApp.this,
                                                                       mLockScreenReceiver);
                    mLockScreenReceiverRegistered = false;
                }
            }

            mLatestActivity = null;
        }
    }

    private class HideOnLockScreenReceiver extends LockScreenReceiver {

        private @Nullable Activity getLatestActivity() {
            return mActivityLifeCycleTracker.getLatestActivity();
        }

        @Override
        protected void onLocked() {
            // hide the app when the screen is locked, so that it will not stay
            // on lock screen uninvited.
            if (getLatestActivity() != null) {
                getLatestActivity().moveTaskToBack(true);
            }
        }

        @Override
        protected void onUnlocked() {
            // TODO: this is not called when on lock screen, user presses back button to
            // exit Scratch Pad, rather than pressing home button
            // By using back button, the user exits the activity (which invokes onDestroy(),
            // killing tha activity, and hence all the listeners)
            // Potential solutions:
            // 1. Maybe we can have the listening to changes done on application level
            // 2. change the UI so that it shows as a dialog / overlay, so that user
            //   cannot press back button to exit the activity
            Log.v(TAG, "HideOnLockScreenReceiver.onUnlocked()");
            if (mModel.isSendPostponed()) {
                // Start MainActivity takes some noticeable delay
                // Show a toast to let user know what to expect.
                Toast.makeText(getApplicationContext(), "Opening LS Scratch Pad to send the note...",
                               Toast.LENGTH_SHORT).show();

                bringActivityToFrontOrStart(MainActivity.class);
            }
        }

        /**
         * Start an activity, or if it is already there (in the background)
         * bring it to the front.
         * If normal <code>Intent</code> is used, the existing activity
         * will still be on the history stack, i.e., when the user presses
         * back button, it will navigate back to the same activity.
         */
        private void bringActivityToFrontOrStart(Class<?> activityClass) {
            Intent intent = new Intent(getApplicationContext(),
                                       activityClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            getApplicationContext().startActivity(intent);
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
