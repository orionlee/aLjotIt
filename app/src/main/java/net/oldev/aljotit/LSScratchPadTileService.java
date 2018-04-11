package net.oldev.aljotit;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
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
            intent = MainActivity.getStartActivityIntentFromOutsideActivityContext(this);
        } else {
            intent = new Intent(Intent.ACTION_SEND); // create a new note by sending an empty text
            intent.setClassName(MainActivity.GKEEP_PACKAGE_NAME, MainActivity.GKEEP_CLASSNAME);
            intent.putExtra(Intent.EXTRA_TEXT, "");
            intent.setType("text/plain");
        }
        startActivityAndCollapse(intent);
    }
    
}
