package com.coinninja.coinkeeper.tutorialflow;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.view.activity.SplashActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.action.ViewActions;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TutorialScreenTest {

    @Rule
    public ActivityTestRule<SplashActivity> splashActivityIntentsTestRule = new ActivityTestRule<>(SplashActivity.class, true, false);

    private UserHelper userHelper;

    @Before
    public void setUp() throws Exception {

        CoinKeeperApplication application = (CoinKeeperApplication) getInstrumentation().getTargetContext().getApplicationContext();

        //--- Spike! Learn how to use DI for this object - So we can control its state locally
        userHelper = application.getUser();
    }

    @Test
    public void on_fresh_install_user_most_go_through_training() {
        setCompletedTraining(false);
        splashActivityIntentsTestRule.launchActivity(null);
        wait(2000);// wait for splash screen to finish
        whenHelpIsViewed();

        onView(withId(R.id.training_footer_action_button)).perform(click());

        onView(withText("NEW WALLET")).check(matches(isDisplayed()));
        onView(withText("RESTORE")).check(matches(isDisplayed()));
    }

    @Test
    public void on_install_with_completed_training_user_skips_training_screens_and_is_sent_to_home_screen() {
        setCompletedTraining(true);
        splashActivityIntentsTestRule.launchActivity(null);
        wait(2000);// wait for splash screen to finish

        onView(withText("NEW WALLET")).check(matches(isDisplayed()));
        onView(withText("RESTORE")).check(matches(isDisplayed()));
    }

    private void whenHelpIsViewed() {
        onView(withText("What is Bitcoin?")).check(matches(isDisplayed()));

        onView(withId(R.id.training_pager)).perform(ViewActions.swipeLeft());
        onView(withText("Why the system is broken")).check(matches(isDisplayed()));


        onView(withId(R.id.training_pager)).perform(ViewActions.swipeLeft());
        onView(withText("Recovery Words")).check(matches(isDisplayed()));


        onView(withId(R.id.training_pager)).perform(ViewActions.swipeLeft());
        onView(withText("Send Bitcoin via SMS")).check(matches(isDisplayed()));

        onView(withId(R.id.training_pager)).perform(ViewActions.swipeLeft());
    }


    //TODO -- for now, this is a hard coded wait, but will need to find out how to get express to wait on its own
    //TODO -- research hot to use expresso to wait for things like the SplashScreen animation to finish
    private void wait(int waitMS) {
        try {
            Thread.sleep(waitMS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //TODO -- remove this and use DI to create object and control its state
    private void setCompletedTraining(boolean hasCompetedTraining) {
        try {
            userHelper.setCompletedTraining(hasCompetedTraining);
        } catch (NullPointerException e) {
        }
    }
}
