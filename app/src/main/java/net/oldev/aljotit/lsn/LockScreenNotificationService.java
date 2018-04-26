package net.oldev.aljotit.lsn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import net.oldev.aljotit.LjotItApp;

public class LockScreenNotificationService extends Service {
    private static final String TAG = "LJI-LsnS";

    private final LockScreenNotificationReceiver mLockScreenNotificationReceiver =
            new LockScreenNotificationReceiver();

    // TODO:  Development-use use only - change Log.v to Log.i for debug on real devices
    public LockScreenNotificationService() {
        Log.i(TAG, "<init>");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        super.onCreate();

        // Skip all the work if the Android version does not support lock screen notifications
        if (!isSupportedByOS()) {
            stopSelf();
            return;
        }

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
        Log.i(TAG, "onDestroy()");
        super.onDestroy();

        if (!isSupportedByOS()) {
            return;
        }

        LockScreenNotificationReceiver.unregisterFromLockScreenChanges(getApplicationContext(),
                                                                       mLockScreenNotificationReceiver);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isSupportedByOS() {
        return LjotItApp.getApp(this).getModel().isLockScreenNotificationSupported();
    }
    
}
