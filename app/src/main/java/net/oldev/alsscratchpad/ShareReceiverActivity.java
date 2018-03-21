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
        new LSScratchPadModel(getApplicationContext()).appendToContent(textToAdd);
    }
    
    @NonNull
    private static String getNonNullStringExtra(@NonNull Intent intent,
                                                @NonNull String name) {
        String value = intent.getStringExtra(name);
        return value != null ? value : "";
    }

}
