package net.oldev.aljotit.lsn;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.NonNull;

import net.oldev.aljotit.LjotItApp;

public class LockScreenNotificationService extends Service {
    private static final String TAG = "LJI-LsnS";

    private final LockScreenNotificationReceiver mLockScreenNotificationReceiver =
            new LockScreenNotificationReceiver();

    // TODO:  Development-use use only - change Log.v to Log.i for debug on real devices
    public LockScreenNotificationService() {
        logIWithFile(TAG, "<init>");
    }

    @Override
    public IBinder onBind(Intent intent) {
        logIWithFile(TAG, "onBind()");
        return null;
    }

    @Override
    public void onCreate() {
        logIWithFile(TAG, "onCreate()");
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

    // NO-OP for onStartCommand

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logIWithFile(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        logIWithFile(TAG, "onDestroy()");
        super.onDestroy();

        if (!isSupportedByOS()) {
            return;
        }

        LockScreenNotificationReceiver.unregisterFromLockScreenChanges(getApplicationContext(),
                                                                       mLockScreenNotificationReceiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        logIWithFile(TAG, "onConfigurationChanged()");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        logIWithFile(TAG, "onLowMemory()");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        logIWithFile(TAG, "onTrimMemory() , level=" + level);
        super.onTrimMemory(level);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        logIWithFile(TAG, "onTaskRemoved()");
        super.onTaskRemoved(rootIntent);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isSupportedByOS() {
        return LjotItApp.getApp(this).getModel().isLockScreenNotificationSupported();
    }

    private void logIWithFile(@NonNull String tag, @NonNull String msg) {
        LjotItApp.logIWithFile(this, tag, msg);
    }
    
}
