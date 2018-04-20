package net.oldev.aljotit;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Bring up a notification on lock screen so that the user can access the app there,
 * intended for Android 5 /6 devices that do not support Quick Settings Tile.
 *
 * Life cycle of the receiver: it is implemented as a long-running background service that
 * starts on boot, so that users can have the notification ready without bringing up the app first.
 *
 * The actual initialization is done at LjotItApp#onCreate(). A BootReceiver is declared
 * to start it at boot time.
 *
 * Issue: startup on boot does not appear to be working on an emulator. The BootReceiver is invoked,
 * but the app is quickly terminated afterwards.
 * Nevertheless, it still works once the app is accessed on normal screen.
 *
 * Drawback: the background service will be terminated quickly for Android 8+ devices. However,
 * the feature is used for Android 5/6 devices so it does not matter.
 *
 * Reference: https://developer.android.com/about/versions/oreo/background.html
 *
 * Other lifecycle implementation considered and dropped.
 * 1. Declare (and register) the receiver at AndroidManifest.xml : it is not possible because
 *    SCREEN_ON / SCREEN_OFF cannot be registered via xml (neither USER_PRESENT starting Android 8)
 * Reference: https://developer.android.com/guide/components/broadcast-exceptions.html
 *
 * 2. Register the receiver programmatically at BootReceiver (without service): it does not work
 *    because the registration will not last beyond initial BootReceiver,
 *    as described in Context.registerReceiver() documentation.
 *
 * 3. A foreground service: it will work, but with the drawback of having an annoying notification
 * shown all the time on normal screen. It will work on Android 8+ devices (not a priority).
 *
 */
public class LockScreenNotificationReceiver extends LockScreenReceiver {

    private static final String TAG = "LJI-LsnR";

    private Context mCurContext; // OPEN: move it to parameters of onLocked(), etc.

    public LockScreenNotificationReceiver() {
        Log.v(TAG, "<init>");
    }

    @Override
    protected @NonNull String tag() { return TAG; }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        mCurContext = context; // to be used by onLocked(), etc.
        super.onReceive(context, intent);  // super class check action to dispatch
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onLocked() {
        Log.v(TAG, "LockScreenNotificationReceiver.onLocked()");
        showLockScreenNotification();
    }

    @Override
    protected void onUnlocked() {
        Log.v(TAG, "LockScreenNotificationReceiver.onUnLocked()");
        cancelLockScreenNotification();
    }

    // Helpers to emulate a Context.

    private @NonNull Context getApplicationContext() {
        return mCurContext.getApplicationContext();
    }


    @SuppressWarnings("SameParameterValue")
    private Object getSystemService(@NonNull String name) {
        return mCurContext.getSystemService(name);
    }

    private String getPackageName() {
        return mCurContext.getPackageName();
    }

    // Lock Screen Notification related logic

    private static final String LOCK_SCREEN_CHANNEL = "lockScreen";

    /**
     * Setup Notification Channel for lockscreen notification.
     * Should be called before receiver instance methods are invoked.
     *
     * For Oreo+ devices, NO-OP otherwise.
     */
    public static void createNotificationChannel(@NonNull Context ctx) {
        // Notification channel applies only to Oreo+
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(LOCK_SCREEN_CHANNEL,
                                                              ctx.getString(R.string.label_lockscreen_notification_channel_name),
                                                              NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setDescription(ctx.getString(R.string.label_lockscreen_notification_channel_desc));
        // Oreo defaults have sounds. Disable sound as it will be a nuisance.
        channel.setSound(null, null);

        //noinspection ConstantConditions
        ((NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE)).
                createNotificationChannel(channel);
    }


    /**
     * A UI utility to check if notification (or the specific lock screen notification for Oreo+)
     * is enabled in system, i.e., app notification settings.
     *
     * The use case is if the lock screen notification is enabled in app preference,
     * but the correspond Android system-wide app notification is disabled, the net result  
     * is that lock screen is still disabled, which will confuse users.
     *
     * @param ctx context for the calling activity
     *
     */
    public static boolean isNotificationEnabledInSystem(@NonNull Context ctx) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            // MUST use Compat for Android 5/6 devices.
            NotificationManagerCompat mgrCompat = NotificationManagerCompat.from(ctx);
            return mgrCompat.areNotificationsEnabled();
        } else {
            // case Oreo+: need to check the specific channel, in addition to app settings
            NotificationManager notifyMgr = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            assert notifyMgr != null;
            NotificationChannel channel = notifyMgr.getNotificationChannel(LOCK_SCREEN_CHANNEL);
            return ( notifyMgr.areNotificationsEnabled() &&
                     (channel.getImportance() != NotificationManager.IMPORTANCE_NONE) );
        }

    }

    /**
     * A UI utility to go to they system app notification settings screen
     */
    public static void startAppNotificationSettingsActivity(@NonNull Context ctx) {
        // Note: ACTION_APP_NOTIFICATION_SETTINGS officially supports in Oreo
        // here I  added unofficial support for Android 5 - 7
        // Reference: https://stackoverflow.com/a/32368604
        @SuppressLint("InlinedApi") Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
           // Android 5 - 7
           intent.putExtra("app_package", ctx.getPackageName());
           intent.putExtra("app_uid", ctx. getApplicationInfo().uid);
        } else {
            // Android 8+
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, ctx.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, LOCK_SCREEN_CHANNEL);
        }

        try {
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            Toast.makeText(ctx,
                           R.string.msg_err_start_app_notifications_settings_failed,
                           Toast.LENGTH_LONG)
                 .show();
        }
    }

    private static final int LOCK_SCREEN_NOTIFICATION_ID = 7344;

    private void cancelLockScreenNotification() {
        final LjotItModel model = new LjotItModel(mCurContext);
        if (!model.isLockScreenNotificationEnabled()) {
            return;
        }

        NotificationManager notifyMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.cancel(LOCK_SCREEN_NOTIFICATION_ID);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showLockScreenNotification() {
        final LjotItModel model = new LjotItModel(mCurContext);
        if (!model.isLockScreenNotificationEnabled()) {
            return;
        }

        final int lockScreenRequestCode = 2345;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
                                                                            LOCK_SCREEN_CHANNEL)
                .setCategory(Notification.CATEGORY_SERVICE) // OPEN: the proper category is unclear
                .setShowWhen(false)  // ensure the unnecessary timestamp not shown
                .setPriority(Notification.PRIORITY_MAX) // ensure high enough in the priority to be seen on lock screen
                .setVisibility(Notification.VISIBILITY_PUBLIC); // ensure text be seen on lock screen

        // prepare content intent to launch MainActivity (requires a redirect MainIntentService)
        Intent intent = new Intent(getApplicationContext(), MainIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),
                                                               lockScreenRequestCode,
                                                               intent,
                                                               PendingIntent.FLAG_UPDATE_CURRENT);

        // set layout
        // Need to use RemoteViews with custom layout plus IntentService to launch MainActivity
        // @see https://stackoverflow.com/a/27838085
        //
        // Basic notification header (app name / icon) is preserved by
        // customizing the content part of the view
        // @see https://developer.android.com/training/notify-user/custom-notification.html#custom-content
        //
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notiifcation_lockscreen);
        /// does not work on the container LinearLayout, even with setPendingIntentTemplate:
        /// contentView.setPendingIntentTemplate(R.id.notification_content, pendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_content_text, pendingIntent);
        builder.setSmallIcon(R.drawable.ic_tile)
               .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
               .setCustomContentView(contentView)
               .setCustomBigContentView(contentView);

        NotificationManager notifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.notify(LOCK_SCREEN_NOTIFICATION_ID, builder.build()); // builder.build() require jelly_bean

    }

}
