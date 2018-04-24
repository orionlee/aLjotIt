package net.oldev.aljotit.intro;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import net.oldev.aljotit.LjotItApp;
import net.oldev.aljotit.LjotItModel;
import net.oldev.aljotit.MainActivity;
import net.oldev.aljotit.QSPanelUtil;
import net.oldev.aljotit.R;

import static net.oldev.aljotit.intro.LockScreenSettingsUtil.tryToOpenLockScreenSettings;

public class IntroActivity extends AppIntro {
    private boolean mNotShowAgain = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final @ColorInt int bgColor = getResources().getColor(R.color.colorPrimaryDark);

        addSlide(AppIntroFragment.newInstance("Welcome to LjotIt",
                                              "Jot down notes on lock screen without unlocking. Click the notification to bring up a notepad.",
                                              R.drawable.ic_intro_lockscreen_notification_cropped_marked, bgColor));

        addSlide(AppIntroFragment.newInstance("Write your note without unlock",
                                              "Write down your note and click send button.",
                                              R.drawable.ic_intro_ljotit_cropped_marked, bgColor));

        addSlide(AppIntroFragment.newInstance("Integrated with Google Keep",
                                              "Once unlocked, the note is sent to Google Keep.",
                                              R.drawable.ic_intro_post_unlock_cropped_marked, bgColor));


        addSlide(LSConfigSlide.newInstance(bgColor)); // Lock Screen Access config information

        addSlide(AppIntroCustomSlide.newInstance(R.layout.fragment_content_intro_done, bgColor));

    }


    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        goToLastSlide();

    }

    private void goToLastSlide() {
        int lastSlideIdx = getSlides().size() - 1;
        getPager().setCurrentItem(lastSlideIdx);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        currentFragment.getActivity().finish();

        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.putExtra(MainActivity.EXTRA_FROM_INTRO, true);
        startActivity(mainIntent);


        // Persist the settings on whether to show intro on next startup
        LjotItModel model = ((LjotItApp)getApplication()).getModel();
        model.setShowIntro(!mNotShowAgain);

    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

    public void onNotShowAgainCheckboxClicked(View view) {
        // Is the view now checked?
        mNotShowAgain = ((CheckBox) view).isChecked();
    }

    //
    // Based on https://github.com/apl-devs/AppIntro/blob/v4.2.2/example/src/main/java/com/amqtech/opensource/appintroexample/util/SampleSlide.java
    //
    public static class CustomSlide extends Fragment {

        protected static final String ARG_LAYOUT_RES_ID = "layoutResId";

        private int mLayoutResId;

        @SuppressWarnings("unused")
        public static CustomSlide newInstance(int layoutResId) {
            CustomSlide customSlide = new CustomSlide();
            Bundle args = createBundleForNewInstance(layoutResId);
            customSlide.setArguments(args);

            return customSlide;
        }

        protected static Bundle createBundleForNewInstance(int layoutResId) {
            Bundle args = new Bundle();
            args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
            return args;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
                mLayoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(mLayoutResId, container, false);
        }
    }

    /**
     * A peer to AppIntroFragment, except that the content is defined in a layout.
     * The fragment's container styling is the same as AppIntroFragment
     */
    public static class AppIntroCustomSlide extends CustomSlide {

        // The container layout that is meant to be equivalent to AppIntroFragment
        private static final int VAL_LAYOUT_RES_ID = R.layout.fragment_container_appintro;

        private static final String ARG_CONTENT_LAYOUT_RES_ID = "contentLayoutResId";
        protected static final String ARG_BG_COLOR = "bgColor";

        private int mContentLayoutResId;
        private @ColorInt int mBgColor;

        public static AppIntroCustomSlide  newInstance(int contentLayoutResId, @ColorInt int bgColor) {
            AppIntroCustomSlide  customSlide = new AppIntroCustomSlide();

            Bundle args = createBundleForNewInstance(contentLayoutResId, bgColor);
            customSlide.setArguments(args);

            return customSlide;
        }

        protected static Bundle createBundleForNewInstance(int contentLayoutResId, @ColorInt int bgColor) {
            Bundle args = CustomSlide.createBundleForNewInstance(VAL_LAYOUT_RES_ID);
            args.putInt(ARG_CONTENT_LAYOUT_RES_ID, contentLayoutResId);
            args.putInt(ARG_BG_COLOR, bgColor);
            return args;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null) {
                if (getArguments().containsKey(ARG_CONTENT_LAYOUT_RES_ID)) {
                    mContentLayoutResId = getArguments().getInt(ARG_CONTENT_LAYOUT_RES_ID);
                }
                mBgColor = getArguments().getInt(ARG_BG_COLOR);
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {

            // inflate the container (from super)
            // then add the contentView specified in this class
            
            // the container layout MUST be a ViewGroup
            ViewGroup fragmentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

            assert fragmentView != null;
            View mainLayout = fragmentView.findViewById(R.id.appintro_container_main);
            mainLayout.setBackgroundColor(mBgColor);

            // Inflate content view
            inflater.inflate(mContentLayoutResId, fragmentView, true);

            return fragmentView;
        }

    }

    public static class LSConfigSlide extends AppIntroCustomSlide {

        public static LSConfigSlide  newInstance(@ColorInt int bgColor) {
            LSConfigSlide  customSlide = new LSConfigSlide();

            Bundle args = AppIntroCustomSlide.createBundleForNewInstance(R.layout.fragment_content_intro_config, bgColor);
            customSlide.setArguments(args);

            return customSlide;
        }

        private final Html.ImageGetter mImageGetter = new LSCImageGetter();

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            LjotItModel model = LjotItApp.getApp(getActivity()).getModel();

            // Show information about 1) lock screen notifications, 2) quick settings
            // only if the device supports the feature.

            final int lsnVisibility =
                    ( model.isLockScreenNotificationSupported() ? View.VISIBLE : View.GONE );
            final int qsVisibility =
                    ( model.isQSTileSupported() ? View.VISIBLE : View.GONE);

            if (!model.isLockScreenNotificationEnabled()) {
                makeViewGone(view, R.id.intro_ls_conf_lsn_head);
                makeViewGone(view, R.id.intro_ls_conf_lsn_desc);
            }

            if (!model.isQSTileSupported()) {
                makeViewGone(view, R.id.intro_ls_conf_qs_head);
                makeViewGone(view, R.id.intro_ls_conf_qs_desc);
                makeViewGone(view, R.id.intro_ls_conf_qs_btn);
            } else { // case QSTile is supported

                // bind rich text to description
                setHtmlText(view.findViewById(R.id.intro_ls_conf_qs_desc),
                        R.string.intro_ls_conf_qs_desc, mImageGetter);

                // bind action to open quick settings panel button
                View openQSPanelBtn = view.findViewById(R.id.intro_ls_conf_qs_btn);
                openQSPanelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean success = QSPanelUtil.expandQuickSettingsPanel(getActivity());
                        Log.v("LJI-Intro", "Open Quick Settings Panel result: " + success);
                        // OPEN: Consider show an message to users if opening it fails
                    }
                });

            }

            // Lock screen shortcuts section
            // always visible to users
            {
                View openLSSPanelBtn = view.findViewById(R.id.intro_ls_conf_lss_btn);
                openLSSPanelBtn.setOnClickListener(v -> {
                    tryToOpenLockScreenSettings(getActivity());
                });
            }

        }


        private class LSCImageGetter implements Html.ImageGetter {
            @Override
            public Drawable getDrawable(String source) {
                @DrawableRes int id;
                if (source != null &&
                        source.equals("ic_edit_white_20dp")) {
                    id = R.drawable.ic_edit_white_20dp;
                } else {
                    return null;
                }

                Drawable d = getResources().getDrawable(id);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                return d;
            }
        }

        private void setHtmlText(@NonNull TextView view,
                                 @StringRes int htmlTextId,
                                 Html.ImageGetter imageGetter) {
            String htmlText = getString(htmlTextId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.setText(Html.fromHtml(htmlText,
                        Html.FROM_HTML_MODE_LEGACY,
                        imageGetter,
                        null));
            } else {
                view.setText(Html.fromHtml(htmlText,
                        imageGetter,
                        null));
            }
        }

        private static void makeViewGone(@NonNull View container,  @IdRes int id) {
            container.findViewById(id).setVisibility(View.GONE);
        }

    }

}
