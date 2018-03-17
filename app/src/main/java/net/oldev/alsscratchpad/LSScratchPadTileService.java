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
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityAndCollapse(intent);
    }
    
}
