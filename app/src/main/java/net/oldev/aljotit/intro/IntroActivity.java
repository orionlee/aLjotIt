package net.oldev.aljotit.intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import net.oldev.aljotit.MainActivity;
import net.oldev.aljotit.R;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final @ColorInt int bgColor = getResources().getColor(R.color.colorPrimary);

        addSlide(AppIntroFragment.newInstance("Welcome to LjotIt",
                                              "Jot down notes on lock screen without unlocking. Click the notification to bring up a notepad.",
                                              R.drawable.ic_intro_lockscreen_notification_cropped_marked, bgColor));

        addSlide(AppIntroFragment.newInstance("Write your note without unlock",
                                              "Write down your note and click send button.",
                                              R.drawable.ic_intro_ljotit_cropped_marked, bgColor));

        addSlide(AppIntroFragment.newInstance("Integrated with Google Keep",
                                              "Once unlocked, the note is sent to Google Keep.",
                                              R.drawable.ic_intro_post_unlock_cropped_marked, bgColor));

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

}
