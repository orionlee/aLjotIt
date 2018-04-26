package net.oldev.aljotit;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
            LjotItModel model = getModel();
            intent = new Intent(Intent.ACTION_SEND); // create a new note by sending an empty text
            intent.setClassName(model.getSendToPreferredPackageName(),
                                model.getSendToPreferredClassName());
            intent.putExtra(Intent.EXTRA_TEXT, "");
            intent.setType("text/plain");
            showUnlockedScreenHelpMessageIfNotShown();
        }
        startActivityAndCollapse(intent);
    }


    @Override
    public void onTileAdded() {
        super.onTileAdded();
        getModel().setQSTileAdded(true);
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        getModel().setQSTileAdded(false);
    }

    private LjotItModel getModel() {
        return LjotItApp.getApp(this).getModel();
    }


    private void showUnlockedScreenHelpMessageIfNotShown() {
        final String msg = "On unlocked screen, press the quick settings tile to create a new Keep note.\nLong-press to bring up LjotIt.";
        //showSnackBarLikeToast(msg, null, null);
        //showMessage(msg);
    }

    private void showMessage(@NonNull String msg) {
        SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                msg, Snackbar.LENGTH_LONG);
        snackbarWrapper.show();
    }

    private void showSnackBarLikeToast(@NonNull String msg,
                                       @Nullable String actionText,
                                       @Nullable View.OnClickListener actionOnClickListener) {

        //inflate the custom toast
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View layout = inflater.inflate(R.layout.snackbar_like_toast, null);

        // Set the Text to show in TextView
        TextView text = (TextView) layout.findViewById(R.id.snackbar_text);
        text.setText(msg);

        Button button = (Button) layout.findViewById(R.id.snackbar_action);
        if (!TextUtils.isEmpty(actionText) && actionOnClickListener != null) {
            button.setText(actionText);
            button.setOnClickListener(actionOnClickListener);
        } else {
            button.setVisibility(View.GONE);
        }
        Toast toast = new Toast(getApplicationContext());

        //Setting up toast position, similar to Snackbar
        // Set Gravity.TOP (rather than the customary BOTTOM) to avoid colliding with soft keyboard
        toast.setGravity(Gravity.TOP | Gravity.LEFT  | Gravity.START | Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

}
