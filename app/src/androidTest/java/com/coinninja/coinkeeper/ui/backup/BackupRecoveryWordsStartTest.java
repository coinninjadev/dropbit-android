package com.coinninja.coinkeeper.ui.backup;

import com.coinninja.coinkeeper.CoinKeeperApplication;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class BackupRecoveryWordsStartTest {

    @Rule
    public ActivityTestRule<BackupRecoveryWordsStartActivity> activityRule =
            new ActivityTestRule<>(BackupRecoveryWordsStartActivity.class, true, false);

    @Before
    public void setUp() {
        ((CoinKeeperApplication) InstrumentationRegistry.getTargetContext().getApplicationContext()).getAuthentication().setAuthenticated();

        activityRule.launchActivity(null);
    }

    @Test
    public void verify_screen_setup_properly() {
        onView(withText("You will be given a list of 12 words to write down on paper and keep in a safe place."))
                .check(matches(isDisplayed()));

        onView(withText("It’s important to never store your recovery words digitally or online."))
                .check(matches(isDisplayed()));

        onView(withText("Using your recovery words is the only way to restore your DropBit wallet if your phone is lost, broken, stolen or upgraded."))
                .check(matches(isDisplayed()));

        onView(withText("VIEW RECOVERY WORDS"))
                .check(matches(isDisplayed())).perform(click());

        onView(withText("Recovery Words"))
                .check(matches(isDisplayed()));

    }
}