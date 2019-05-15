package com.coinninja.coinkeeper.ui.base;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity;
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryActivity;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class DropbitMeFragmentTest {
    @Mock
    LazyList transactions;
    @Mock
    WalletHelper walletHelper;
    @Mock
    DropbitMeConfiguration dropbitMeConfiguration;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        application.walletHelper = walletHelper;
        application.dropbitMeConfiguration = dropbitMeConfiguration;
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.size()).thenReturn(0);
        when(dropbitMeConfiguration.hasVerifiedAccount()).thenReturn(false);
    }

    @After
    public void tearDown() {
        transactions = null;
        walletHelper = null;
    }

    @Test
    public void does_not_show_dropbit_me_icon_on_settings() {
        ActivityScenario<SettingsActivity> scenario = ActivityScenario.launch(SettingsActivity.class);

        scenario.moveToState(Lifecycle.State.RESUMED);

        onView(withId(R.id.dropbit_me_button)).check(matches(not(isDisplayed())));

        scenario.close();
    }

    @Test
    public void shows_dropbit_me_icon_on_launch_for_transaction_history() {
        ActivityScenario<TransactionHistoryActivity> scenario = ActivityScenario.launch(TransactionHistoryActivity.class);

        scenario.moveToState(Lifecycle.State.RESUMED);

        onView(withId(R.id.dropbit_me_button)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void shows_dropbit_me_icon_on_launch_for_transaction_details() {
        ActivityScenario<TransactionDetailsActivity> scenario = ActivityScenario.launch(TransactionDetailsActivity.class);

        scenario.moveToState(Lifecycle.State.RESUMED);

        onView(withId(R.id.dropbit_me_button)).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void shows_dropbit_me_dialog_when_pressed() {
        ActivityScenario<TransactionHistoryActivity> scenario = ActivityScenario.launch(TransactionHistoryActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);

        onView(withId(R.id.dropbit_me_button)).perform(click());

        scenario.onActivity(activity -> {
            assertNotNull(activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG));
        });

        scenario.close();
    }

    @Test
    public void shows_when_configuration_requested() {
        when(dropbitMeConfiguration.shouldShowWhenPossible()).thenReturn(true);
        ActivityScenario<TransactionHistoryActivity> scenario = ActivityScenario.launch(TransactionHistoryActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);

        scenario.onActivity(activity -> {
            assertNotNull(activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG));
        });

        verify(dropbitMeConfiguration).acknowledge();
        scenario.close();
    }

    @Test
    public void observes_view_requests_when_screen_has_visible_icon() {
        ActivityScenario<TransactionHistoryActivity> scenario = ActivityScenario.launch(TransactionHistoryActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);

        verify(dropbitMeConfiguration).setOnViewDropBitMeViewRequestedObserver(any());
        scenario.close();
    }

    @Test
    public void does_not_observe_view_requests_when_screen_has_invisible_icon() {
        ActivityScenario<SettingsActivity> scenario = ActivityScenario.launch(SettingsActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);

        verifyZeroInteractions(dropbitMeConfiguration);
        scenario.close();
    }

    @Test
    public void shows_dialog_when_observing_request() {
        doCallRealMethod().when(dropbitMeConfiguration).setOnViewDropBitMeViewRequestedObserver(any());
        doCallRealMethod().when(dropbitMeConfiguration).showWhenPossible();
        doCallRealMethod().when(dropbitMeConfiguration).shouldShowWhenPossible();
        ActivityScenario<TransactionHistoryActivity> scenario = ActivityScenario.launch(TransactionHistoryActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);

        dropbitMeConfiguration.showWhenPossible();

        scenario.onActivity(activity -> {
            assertNotNull(activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG));
        });
        scenario.close();
    }
}