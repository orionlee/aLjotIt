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
                onLocked();
            }
            else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
            {
                // Screen is unlocked
                Log.d(TAG, "[USER_PRESENT]");
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
