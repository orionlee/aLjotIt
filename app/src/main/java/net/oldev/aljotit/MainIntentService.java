package net.oldev.aljotit;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Used to start MainActivity from a Lock Screen Notification
 * (which cannot start activity otherwise)
 */
public class MainIntentService extends IntentService {
    
    public MainIntentService() {
        super("MainIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("LSSP-MainIn", "onHandleIntent()");
        if (intent != null) {
            MainActivity.startFromOutsideActivityContext(this);
        }
    }

}
