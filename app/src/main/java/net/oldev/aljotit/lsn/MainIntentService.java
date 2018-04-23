package net.oldev.aljotit.lsn;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import net.oldev.aljotit.MainActivity;

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
        Log.v("LJI-MainIn", "onHandleIntent()");
        if (intent != null) {
            MainActivity.startFromOutsideActivityContext(this);
        }
    }

}
