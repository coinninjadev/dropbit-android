package com.coinninja.coinkeeper.view.fragment;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.view.activity.CalculatorActivity;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class PayDialogFragmentInstrumentationTest {

    @Rule
    public ActivityTestRule<CalculatorActivity> activityActivityTestRule = new ActivityTestRule<>(CalculatorActivity.class, true, false);
    private CalculatorActivity activity;

    @Before
    public void setup() {
        activity = activityActivityTestRule.getActivity();
        CoinKeeperApplication application = (CoinKeeperApplication) getInstrumentation().getTargetContext().getApplicationContext();
        application.getAuthentication().setAuthenticated();

        activityActivityTestRule.launchActivity(null);
    }


    @Ignore
    @Test
    public void pasting_address_with_amount_changes_primary_currency_to_btc() {
        onView(withId(R.id.one)).perform(click());
        onView(withId(R.id.two)).perform(click());
        onView(withId(R.id.request_btn)).perform(click());
        onView(withId(R.id.request_copy_button)).perform(click());
        onView(withId(R.id.close_btn)).perform(click());
        onView(withId(R.id.send_btn)).perform(click());

    }

}
