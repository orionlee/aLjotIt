package net.oldev.aljotit;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

@TargetApi(Build.VERSION_CODES.N)
public class LjotItTileService extends TileService {
    public LjotItTileService() {
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
            LjotItModel model = ((LjotItApp)getApplication()).getModel();
            intent = new Intent(Intent.ACTION_SEND); // create a new note by sending an empty text
            intent.setClassName(model.getSendToPreferredPackageName(),
                                model.getSendToPreferredClassName());
            intent.putExtra(Intent.EXTRA_TEXT, "");
            intent.setType("text/plain");
        }
        startActivityAndCollapse(intent);
    }
    
}
