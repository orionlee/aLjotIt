package net.oldev.aljotit.intro;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroBaseFragment;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import net.oldev.aljotit.MainActivity;
import net.oldev.aljotit.R;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final @ColorInt int bgColor = getResources().getColor(R.color.colorPrimary);

        
        /* Animated screenshots for introduction.
        Problems:
        - on devices with low memory, loading animation will run out of memory
          - On some devices, loading 1 image is all it can handle
        - Potential solutions:
          - Reduce number of screenshots shown
          - crop the image (full size screenshot might be bad for usability anyway)
          - Downsize images (less to load?!)
          - use a custom AnimationDrawable that only load images on demand (and release them right afterwards)
             - @see https://stackoverflow.com/a/10993879
             - also maybe tweak BitmapFactory, e.g.,
               - tweak BitmapFactory.inPreferredConfig (https://stackoverflow.com/a/8889854)
               - tweak BitmapFactory sample size (https://gist.github.com/kvaggelakos/1862570)
         */
        addSlide(AppIntroAnimatedFragment.newInstance("Welcome to LjotIt",
                                                      "You can jot down notes on lock screen without unlocking",
                                                      R.drawable.ss_intro1, bgColor));

        addSlide(AppIntroAnimatedFragment.newInstance("Integrated with Google Keep",
                                                      "The note is sent to Google Keep Once unlocked",
                                                      R.drawable.ss_intro2, bgColor));

/*
        addSlide(AppIntroFragment.newInstance("Welcome to LjotIt",
                                              "Jot down notes on lock screen without unlocking. Click the notification to bring up a notepad.",
                                              R.drawable.ic_intro_lockscreen_notification_cropped_marked, bgColor));

        addSlide(AppIntroFragment.newInstance("Write your note without unlock",
                                              "Write down your note and click send button.",
                                              R.drawable.ic_intro_ljotit_cropped_marked, bgColor));

        addSlide(AppIntroFragment.newInstance("Integrated with Google Keep",
                                              "Once unlocked, the note is sent to Google Keep.",
                                              R.drawable.ic_intro_post_unlock_cropped_marked, bgColor));
*/

        addSlide(AppIntroFragment.newInstance("Configure ways to access from lock screen",
                                              "You can access LjotIt from Quick Settings, Notifications, or Quick Access",
                                              0, bgColor));

        addSlide(AppIntroFragment.newInstance("You are all set.",
                                              "GET STARTED",
                                              R.drawable.ic_intro_check_circle_gray, bgColor));

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        currentFragment.getActivity().finish();

        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainIntent);
    }


    public static class AppIntroAnimatedFragment extends AppIntroBaseFragment {

        public static AppIntroAnimatedFragment newInstance(CharSequence title, CharSequence description,
                                                           @DrawableRes int imageDrawable,
                                                           @ColorInt int bgColor) {
            SliderPage sliderPage = new SliderPage();
            sliderPage.setTitle(title);
            sliderPage.setDescription(description);
            sliderPage.setImageDrawable(imageDrawable);
            sliderPage.setBgColor(bgColor);
            return  newInstance(sliderPage);
        }

        public static AppIntroAnimatedFragment newInstance(SliderPage sliderPage) {
            AppIntroAnimatedFragment slide = new AppIntroAnimatedFragment();

            Bundle args = new Bundle();
            args.putString(ARG_TITLE, sliderPage.getTitleString());
            args.putString(ARG_TITLE_TYPEFACE, sliderPage.getTitleTypeface());
            args.putString(ARG_DESC, sliderPage.getDescriptionString());
            args.putString(ARG_DESC_TYPEFACE, sliderPage.getDescTypeface());
            args.putInt(ARG_DRAWABLE, sliderPage.getImageDrawable());
            args.putInt(ARG_BG_COLOR, sliderPage.getBgColor());
            args.putInt(ARG_TITLE_COLOR, sliderPage.getTitleColor());
            args.putInt(ARG_DESC_COLOR, sliderPage.getDescColor());
            slide.setArguments(args);
            return slide;
        }

        @Override
        public void onSlideSelected() {
            super.onSlideSelected();
            ImageView imageView = getView().findViewById(com.github.paolorotolo.appintro.R.id.image);
            AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
            animationDrawable.start();
        }

        @Override
        public void onSlideDeselected() {
            super.onSlideDeselected();
            ImageView imageView = getView().findViewById(com.github.paolorotolo.appintro.R.id.image);
            AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
            animationDrawable.stop();
        }

        @Override
        protected int getLayoutId() {
            return com.github.paolorotolo.appintro.R.layout.fragment_intro;
        }

    }

}
