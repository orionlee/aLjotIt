package net.oldev.aljotit.intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import net.oldev.aljotit.MainActivity;
import net.oldev.aljotit.R;

public class IntroActivity extends AppIntro {
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


        addSlide(AppIntroCustomSlide.newInstance(R.layout.fragment_content_intro_config, bgColor));

        addSlide(AppIntroCustomSlide.newInstance(R.layout.fragment_content_intro_done, bgColor));

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        currentFragment.getActivity().finish();

        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainIntent);
    }


    //
    // Based on https://github.com/apl-devs/AppIntro/blob/v4.2.2/example/src/main/java/com/amqtech/opensource/appintroexample/util/SampleSlide.java
    //
    public static class CustomSlide extends Fragment {

        protected static final String ARG_LAYOUT_RES_ID = "layoutResId";

        private int mLayoutResId;
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

            Bundle args = CustomSlide.createBundleForNewInstance(VAL_LAYOUT_RES_ID);
            args.putInt(ARG_CONTENT_LAYOUT_RES_ID, contentLayoutResId);
            args.putInt(ARG_BG_COLOR, bgColor);
            customSlide.setArguments(args);

            return customSlide;
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

            View mainLayout = fragmentView.findViewById(R.id.appintro_container_main);
            mainLayout.setBackgroundColor(mBgColor);

            // Inflate content view
            inflater.inflate(mContentLayoutResId, fragmentView, true);

            return fragmentView;
        }

    }

}
