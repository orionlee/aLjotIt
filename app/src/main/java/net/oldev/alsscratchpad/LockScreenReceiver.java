package net.oldev.alsscratchpad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * A generic class listening to events related to lock screen.
 * Implementation will create subclasses for the behavior desired.
 *
 * @see #onLocked()
 * @see #onShowingLockScreen()
 * @see #onUnlocked()
 */
public abstract class LockScreenReceiver extends BroadcastReceiver {

    private static final String TAG = "LSSP-LsRcvr";

    // last relevant intent handled (to dispatch onShowingLockScreen() correctly)
    private Intent mLastIntent;
    private long mLastIntentTimestamp;
    private boolean mScreenLocked = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null)
        {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            {
                // Screen is on but not unlocked (if any locking mechanism present)
                Log.d(TAG, "[SCREEN_ON]");
                if (mLastIntent.getAction().equals(Intent.ACTION_USER_PRESENT) &&
                        mLastIntentTimestamp > System.currentTimeMillis() - 2000) {
                    Log.d(TAG, "  SCREEN_ON ignored: USER_PRESENT was just sent, indicating it is ON due to using fingerprint (or other non-visual one such as voice) unlocking ");
                    return;
                } else {
                    // normal case
                    onShowingLockScreen();
                }
            }
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
            {
                // Screen is locked, (technically, becomes non-interactive, screen might still be on)
                Log.d(TAG, "[SCREEN_OFF]");
                if (!mScreenLocked) {
                    mScreenLocked = true;
                    onLocked();
                } else {
                    // Avoid repeatedly calling onLocked on lock screen. Scenario:
                    // - User locks the device:
                    //   - SCREEN_OFF received. onLock() is invoked.
                    // - User turns on and sees the lock screen:
                    //   - SCREEN_ON received. onShowingLockScreen() is invoked.
                    // - User turns off the screen (without unlocking the phone)
                    //   - SCREEN_OFF received. The state tracking mScreenLocked prevents onLocked() from being invoked.
                    // - Use finally unlocks the device
                    //   - USER_PRESENT received. onUnlocked() is invoked.
                    //
                    // Preventing onLocked() being invoked repeatedly in locked state is important
                    // for lock screen notification use case. The logic supports the behavior
                    // - On lock screen, user decides he/she does not need LS Scratch Pad and swipes off the notification.
                    // - The user then turns off the screen (without unlocking)
                    // - The user turns on the screen (lock screen). He/she would not want to see the lockscreen notification
                    //
                    // The logic here prevents onLocked() being invoked again, thus preventing lockscreen notification being
                    // shown again.
                    Log.d(TAG, "  SCREEN_OFF ignored: the screen has already been locked. The user just turns off the screen after seeing the lock screen (without any unlock).");
                }
            }
            else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
            {
                // Screen is unlocked
                Log.d(TAG, "[USER_PRESENT]");
                mScreenLocked = false;
                onUnlocked();
            } else {
                return;
            }
            // Record intents handled.
            mLastIntent = intent;
            mLastIntentTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Implementation should override this method to implement the behavior needed.
     */
    protected void onLocked() {}

    /**
     * Implementation should override this method to implement the behavior needed.
     */
    protected void onUnlocked() {}

    /**
     * Implementation should override this method to implement the behavior needed.
     */
    protected void onShowingLockScreen() {}


    public static void registerToLockScreenChanges(@NonNull Context ctx,
                                                   @NonNull LockScreenReceiver receiver) {
        Log.d(TAG, "registerToLockScreenChanges()");
        IntentFilter lockFilter = new IntentFilter();
        lockFilter.addAction(Intent.ACTION_SCREEN_ON);
        lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
        lockFilter.addAction(Intent.ACTION_USER_PRESENT);
        ctx.registerReceiver(receiver, lockFilter);
    }

    public static void unregisterFromLockScreenChanges(@NonNull Context ctx,
                                                       @NonNull LockScreenReceiver receiver) {
        Log.d(TAG, "unregisterFromLockScreenChanges()");
        ctx.unregisterReceiver(receiver);
    }

}
