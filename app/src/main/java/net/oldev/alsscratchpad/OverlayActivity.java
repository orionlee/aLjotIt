package net.oldev.alsscratchpad;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

// EXPERIMENT Goal: An activity on lockscreen with transparent background (so that part of lock screen remains visible)
// Verdict:
// - no use, it is technically transparent over normal background on lock screen, but
//   the lock screen first got drawn over some black background (or lockscreen wallpaper)
// - on normal screen, the same activity does have a transparent background
public class OverlayActivity extends AppCompatActivity { // Activity : try to see which I ann use to get a transparent background

    private static final String TAG = OverlayActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate]");
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        /// Doesn't help:  window.setFormat(PixelFormat.TRANSPARENT);
        

        // fragment
        Fragment fragment = getFragmentManager().findFragmentByTag(FragmentType.OVERLAY.getTag());
        if (fragment == null) {
            fragment = OverlayFragment.newInstance();
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, fragment, FragmentType.OVERLAY.getTag());
        ft.commit();
    }

    private enum FragmentType {
        OVERLAY("overlay");
        private String tag;

        private FragmentType(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }
}