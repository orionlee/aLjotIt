package net.oldev.aljotit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockScreenNotificationBootReceiver extends BroadcastReceiver {

    private static final String TAG = "LJI-LsnB";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive()");

        // No real action is needed. The receiver activation on boot itself
        // will trigger creating LjotItApp, which will start the background service
    }
}
