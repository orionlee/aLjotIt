package net.oldev.alsscratchpad;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.TileService;

@TargetApi(Build.VERSION_CODES.N)
public class LSScratchPadTileService extends TileService {
    public LSScratchPadTileService() {
    }

    @Override
    public void onClick() {
        super.onClick();
        // Launch new Google Keep note on normal screen.
        // On lock screen, launch ScratchPad as Google Keep is not available.
        Intent intent;
        if (isLocked()) {
            intent = new Intent(getApplicationContext(), MainActivity.class);
        } else {
            intent = new Intent();
            intent.setClassName(MainActivity.GKEEP_PACKAGE_NAME, MainActivity.GKEEP_CLASSNAME);
        }
        startActivityAndCollapse(intent);
    }
    
}
