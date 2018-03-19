package net.oldev.alsscratchpad;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LSSP-Main"; // Lock Screen Scratch Pad abbreviation

    private static final int REQUEST_CODE_SHARE_TEXT = 987;

    private EditText mScratchPad;

    private LSScratchPadModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mModel = new LSScratchPadModel(getApplicationContext());

        // setting attributes on the service declaration in AndroidManifest.xml
        // does not work for some reason android:showOnLockScreen="true", android:showOnLockScreen="true"
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        ThemeSwitcher.setTheme(mModel, this); // MUST be done before setContentView, consider setting the theme
        setContentView(R.layout.activity_main);

        mScratchPad = (EditText)findViewById(R.id.scratch_pad_content);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((view) -> { sendToKeep(); });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // data binding
        // @see #onStop() for when content gets persisted.
        // onStart is the correspond life cycle method, so it is used to load the content
        mScratchPad.setText(mModel.getContent());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_SHARE_TEXT == requestCode) {
            Log.d(TAG, "To be implemented: ask if the user wants to clear the text");
            Toast toast = Toast.makeText(getApplicationContext(),
                           "Text sent. TODO: option to clear the scratch pad.",
                           Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            Log.e(TAG, "Unsupported requestCode " + requestCode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d(TAG, "onOptionsItemSelected() - To be implemented. menuItem.id=" + id);
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_send_to_keep) {
            sendToKeep();
            return true;
        } else if (id == R.id.action_share) {
            sendToShareChooser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Persist content. onStop is used as it is the life cycle method
     * when the activity will get destroyed.
     *
     * @see https://developer.android.com/guide/components/activities/activity-lifecycle.html
     */
    @Override
    protected void onStop() {
        super.onStop();
        String content = mScratchPad.getText().toString();
        mModel.setContent(content);
    }

    //
    // Actual sendTo action implementation
    //

    static final String GKEEP_PACKAGE_NAME = "com.google.android.keep";
    static final String GKEEP_CLASSNAME = "com.google.android.keep.activities.ShareReceiverActivity";

    // Standard share UI
    private void sendToShareChooser() {
        sendTo(null, null);
    }

    private void sendToKeep() {
        /// Use Keep
        sendTo(GKEEP_CLASSNAME, GKEEP_CLASSNAME);
    }

    private void sendTo(@Nullable String packageName, @Nullable String className) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, mScratchPad.getText().toString()); // MUST cast to string or it won't be accepted by google Keep
        intent.setType("text/plain"); // MUST be set for the system the share chooser to show up.

        /// Use Keep
        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
            intent.setClassName("com.google.android.keep", "com.google.android.keep.activities.ShareReceiverActivity");
        }  else {
            // Customized chooser label
            String labelText = getString(R.string.label_send_to);
            SpannableString chooserLabel = new SpannableString(labelText);
            chooserLabel.setSpan(new TextAppearanceSpan(this,
                                                        android.R.style.TextAppearance_Material_Medium),
                                 0, labelText.length(), 0); // larger that default
            intent = intent.createChooser(intent, chooserLabel);
        }
        ///Log.e(TAG, "text to send: " + textView.getText());

        startActivityForResult(intent, REQUEST_CODE_SHARE_TEXT);
        //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //        .setAction("Action", null).show();

    }

}