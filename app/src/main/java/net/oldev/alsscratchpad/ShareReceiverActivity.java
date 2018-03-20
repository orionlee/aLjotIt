package net.oldev.alsscratchpad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

/**
 * A Non-UI activity that handles share from other applications.
 */
public class ShareReceiverActivity extends Activity {

    private static final String TAG = "LSSP-ShrRcvr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action)) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else {
                // Should not happen based on intent-filter configured for the activity
                Log.w(TAG, "Unsupported data type " + type + ". Do nothing.");
            }
        } else {
            // Should not happen based on intent-filter configured for the activity
            Log.w(TAG, "Unsupported intent action " + action + ". Do nothing.");
        }

        // Done backend handling. Navigate to MainActivity
        finish();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));

    }

    private void handleSendText(Intent intent) {
        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);

        String textToAdd = TextUtils.isEmpty(subject) ? text : subject + "\n" + text;
        appendToContent(textToAdd);
    }

    /**
     * Append the supplied text to the content.
     *
     * OPEN: Consider move this operation to the model
     * @param textToAdd
     */
    private void appendToContent(String textToAdd) {
        // OPEN: consider return existing content length so that we can
        // set the cursor to the position of the beginning of the text just appended.
        if (!TextUtils.isEmpty(textToAdd)) {
            LSScratchPadModel model = new LSScratchPadModel(getApplicationContext());
            final String contentCurrent = model.getContent();
            // The new text starts at a new line, if there is any existing one.
            final String contentNew = TextUtils.isEmpty(contentCurrent) ? textToAdd :
                    contentCurrent + "\n" + textToAdd;
            model.setContent(contentNew);
        } // else nothing needs to be done.
    }

    @NonNull
    private static String getNonNullStringExtra(@NonNull Intent intent,
                                                @NonNull String name) {
        String value = intent.getStringExtra(name);
        return value != null ? value : "";
    }

}
