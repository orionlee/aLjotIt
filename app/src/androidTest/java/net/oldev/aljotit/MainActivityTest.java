package net.oldev.aljotit;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * A sanity test suite, to go through typical user interaction upon installation.
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SmallTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    private static final boolean RELAUNCH_ACTIVITY_TRUE = true;

    // Note: The test relies on the fact the activity is relaunched per test method,
    // so that we can test state persistence.
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, false, RELAUNCH_ACTIVITY_TRUE);

    private static final String TEST_NOTE_TEXT = "Testing message.\nLine 2";
    private static final String TEST_NOTE_TEXT2 = "Testing message edited.\nLine 2";


    /**
     * Logic to simulate initial launch.
     *
     * It clears out show intro flag so that the app will behave as if it is
     * first launched after installation.
     *
     * @see #t1aInitialRun()  semnatically this is a before setup or {@link #t1aInitialRun()}
     *
     */
    @BeforeClass
    public static void setupBeforeInitialRun() {
        LjotItModel model = new LjotItModel(InstrumentationRegistry.getTargetContext());
        model.removeShowIntro();
    }

    @Test
    public void t1aInitialRun() {

        // For initial run, the user will see introduction
        IntroTestUtils introTestUtils = new IntroTestUtils();
        introTestUtils.clickNext(); // on screen 1
        introTestUtils.swipeLeft(); // on screen 2, , try swipe instead of clicking next
        introTestUtils.clickNext(); // on screen 3
        introTestUtils.clickNext(); // on screen 4
        introTestUtils.clickDone(); // finish intro

        // Intro finished. Redirected to MainActivity.
        onScratchPad(); // verify I am on MainActivity now
    }

    /**
     * Logically, this is the second part of initial run test, but it needs to be put as
     * a separate test method, because
     * 1) running it requires mocking send to Note in MainActivity instance.
     * 2) the test cannot get the appropriate MainActivity instance because in {@link #t1aInitialRun()}
     * , it redirects to IntroActivity, and subsequently sending back to MainActivity with a new instance,
     * i.e., the instance returned by <code>mActivityTestRule.getActivity()</code> is no longer the active running instance.
     *
     * Setup by using @Before method won't work for the same reason
     * (MainActivity is created and launched by IntroActivity, thus bypassing test setup)
     *
     */
    @Test
    public void t1bPostIntroRun() {
        setupMainForTest();

        // Try normal actions on the scratch pad
        setScratchPadText(TEST_NOTE_TEXT);
        clickSendFAB();
        // Actual send is mocked, and callback MainActivity immediately
        clickYesToClearOnPostSendDialog();
        assertEquals("Scratch Pad should be cleared after clicking yes",
                     "", getScratchPadText());
        clickUndoClear();
        assertEquals("Scratch Pad should be restored after undo",
                     TEST_NOTE_TEXT, getScratchPadText());

        setScratchPadText(TEST_NOTE_TEXT2); // for subsequent run test
    }

    @Test
    public void t2SubsequentAverageRun() {
        // 1. Intro should not be shown anymore. Straight to MainActivity
        // 2. The text is persisted after the activity is destroyed, relying on that
        //    mActivityTestRule's launchActivity flag set to true
        //    to ensure this is a new instance of MainActivity
        assertEquals("Scratch Pad should remain the same as when it last was",
                     TEST_NOTE_TEXT2, getScratchPadText());
    }

    //
    // Backend test setup for MainActivity
    //
    private void setupMainForTest() {
        mActivityTestRule.getActivity().mActivityLauncher = new MockActivityLauncher(mActivityTestRule.getActivity());
    }


    /**
     * Avoiding launch external note app,
     * just test the intent and simulate return back to MainActivity
     */
    private static class MockActivityLauncher extends MainActivity.ActivityLauncher {
        public MockActivityLauncher(@NonNull MainActivity activity) {
            super(activity);
        }

        @Override
        public void startActivityForResult(@NonNull Intent intent, int requestCode) throws ActivityNotFoundException {
            // verify the intent sent (at least the text)
            assertEquals(Intent.ACTION_SEND, intent.getAction());
            assertEquals(TEST_NOTE_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT));
            assertNotNull("Intent should be an explicit intent with a specific package/activity",
                                           intent.getComponent());
            int resultCodeForTest = 1;
            ((MainActivity)mActivity).onActivityResult(requestCode, resultCodeForTest, intent);
        }
    }


    //
    // UI Helpers for MainActivity
    //

    private ViewInteraction onScratchPad() {
        return onView(
                allOf(withId(R.id.scratch_pad_content),
                      childAtPosition(
                              childAtPosition(
                                      withId(R.id.activity_main),
                                      1),
                              0),
                      isDisplayed()));
    }

    private void setScratchPadText(@NonNull String text) {
        ViewInteraction appCompatEditText = onScratchPad();
        appCompatEditText.perform(replaceText(text), closeSoftKeyboard());
    }

    private String getScratchPadText() {
        return mActivityTestRule.getActivity().mScratchPad.getText().toString();
    }

    private void clickSendFAB() {
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab),
                      childAtPosition(
                              allOf(withId(R.id.activity_main),
                                    childAtPosition(
                                            withId(android.R.id.content),
                                            0)),
                              2),
                      isDisplayed()));
        floatingActionButton.perform(click());
    }

    private void clickYesToClearOnPostSendDialog() {
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button1), withText("Yes, clear it."),
                      childAtPosition(
                              childAtPosition(
                                      withId(R.id.buttonPanel),
                                      0),
                              3)));
        appCompatButton2.perform(scrollTo(), click());
    }

    private void clickUndoClear() {
        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.snackbar_action), withText("Undo"),
                      childAtPosition(
                              childAtPosition(
                                      withClassName(is("android.support.design.widget.Snackbar$SnackbarLayout")),
                                      0),
                              1),
                      isDisplayed()));
        appCompatButton3.perform(click());
    }


    // IntroActivity helpers
    private class IntroTestUtils {
        void clickNext() {
            ViewInteraction appCompatImageButton = onView(
                    allOf(withId(R.id.next),
                          childAtPosition(
                                  allOf(withId(R.id.bottomContainer),
                                        childAtPosition(
                                                withId(R.id.bottom),
                                                1)),
                                  3),
                          isDisplayed()));
            appCompatImageButton.perform(click());
        }

        void swipeLeft() {
            ViewInteraction appIntroViewPager = onView(
                    allOf(withId(R.id.view_pager),
                          childAtPosition(
                                  childAtPosition(
                                          withId(android.R.id.content),
                                          0),
                                  0),
                          isDisplayed()));
            appIntroViewPager.perform(ViewActions.swipeLeft());
        }

        void clickDone() {
            ViewInteraction appCompatButton = onView(
                    allOf(withId(R.id.done), withText("DONE"),
                          childAtPosition(
                                  allOf(withId(R.id.bottomContainer),
                                        childAtPosition(
                                                withId(R.id.bottom),
                                                1)),
                                  4),
                          isDisplayed()));
            appCompatButton.perform(click());
        }
    }


    // Generic helpers
    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }


}
