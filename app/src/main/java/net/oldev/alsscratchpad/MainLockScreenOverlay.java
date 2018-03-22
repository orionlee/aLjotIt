package net.oldev.alsscratchpad;


import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import static android.content.Context.WINDOW_SERVICE;

// Goal: draw a clickable widget on lockscreen, so that it can be used to launch LS Scratch Pad
// instead of using QS Tile
//
// Adapted from
//   https://stackoverflow.com/questions/35327328/android-overlay-textview-on-lockscreen
// Also see:
//   https://stackoverflow.com/questions/4481226/creating-a-system-overlay-window-always-on-top
//   https://stackoverflow.com/questions/37138546/when-adding-view-to-window-with-windowmanager-layoutparams-type-system-overlay/37348311#37348311
public class MainLockScreenOverlay {
    private static final String TAG = "LSSP-MLSO";

    private final @NonNull Context mCtx;

    private WindowManager windowManager;
    private View mainView;
    private WindowManager.LayoutParams params;

    private boolean isShowing = false;

    public MainLockScreenOverlay(@NonNull Context ctx) {
        mCtx = ctx;
        windowManager = (WindowManager)mCtx.getSystemService(WINDOW_SERVICE);

        //add textview and its properties
/*
        TextView textview = new TextView(mCtx);
        textview.setText("Hello There!");
        textview.setTextColor(mCtx.getResources().getColor(android.R.color.white));
        textview.setTextSize(32f);
        mainView = textView;
*/
        Button button = new Button(mCtx);
        button.setText("Close!");
        button.setOnClickListener(v -> hide() );
/* With TYPE_SYSTEM_ERROR, onclick works!
        button.setOnTouchListener((v, event) -> {
            Log.v(TAG, "motionEvent: " + event);
            return false;
        });
*/
        mainView = button;

        /* Requires draw over other app permission,
        one that programmatically enable it (Marshmallow+) with:
         if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays()) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 1234);
         */

        //set parameters for the mainView
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;


        /*
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
        Verdict:
        I can use TYPE_SYSTEM_ERROR to create an the actual floating scratchpad
        However, it cannot be used as a launch point on lockscreen (MS Parchi style)
        as _ERROR precludes the rest of the screen from being interactive.
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,

        Details:
        1. WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,

         works on normal screen (onclick works), but it doesn't show on lock screen
         ditto: changed targetSkVersion to 22 (before Marshmallow)
         ditto: TYPE_PHONE

         2. WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
         shows on lock screen, but I can't get any touch event

         3. WindowManager.LayoutParams.TYPE_SYSTEM_ERROR shows up on lock screen and is clickable
         problem: it precludes the rest of the screen from being clicked.

         4. WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG does not show on lock screen
           permission denied for window type 2008

        5. TYPE_APPLICATION_PANEL, TYPE_TOAST
          Caused by: android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?

         */

    }

    public boolean isShowing() {
        return isShowing;
    }

    public void show() {
        if (!isShowing) {
            Log.v(TAG, "show() - to show");
            windowManager.addView(mainView, params);
            isShowing = true;
        }
    }

    public void hide() {
        if (isShowing) {
            Log.v(TAG, "hide() - to hide");
            windowManager.removeViewImmediate(mainView);
            isShowing = false;
        }
    }

}
