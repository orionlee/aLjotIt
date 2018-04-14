package net.oldev.aljotit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class LockScreenNotificationService extends Service {
    private static final String Tag = "LJI-LsnS";

    private final LockScreenNotificationReceiver mLockScreenNotificationReceiver =
            new LockScreenNotificationReceiver();

    public LockScreenNotificationService() {
        Log.v(TAG, "<init>");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate()");
        super.onCreate();

        // Notification channel setup required for Oreo+, NO-OP otherwise
        LockScreenNotificationReceiver.createNotificationChannel(this);

        // Registration is done at onCreate(), rather than onStartCommand,
        // so that the service can be potentially started multiple times (at boot time, or app start up)
        // while registering exactly once.
        LockScreenNotificationReceiver.registerToLockScreenChanges(getApplicationContext(),
                                                                   mLockScreenNotificationReceiver);

    }

    // NO-OP for onStartCommand, hence no override

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
        LockScreenNotificationReceiver.unregisterFromLockScreenChanges(getApplicationContext(),
                                                                       mLockScreenNotificationReceiver);
    }
}
