package com.coinninja.coinkeeper.ui.dropbit.me.verified;

import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.home.HomeActivity;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class NewlyVerifiedDropbitMeDialogTest {
    private ActivityScenario<HomeActivity> scenario;

    @Mock
    private WalletHelper walletHelper;
    @Mock
    private LazyList transactions;

    @Mock
    private ActivityNavigationUtil activityNavigationUtil;

    @Mock
    private DropbitMeConfiguration dropbitMeConfiguration;
    private TestCoinKeeperApplication application;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        application = ApplicationProvider.getApplicationContext();
        application.activityNavigationUtil = activityNavigationUtil;
        application.dropbitMeConfiguration = dropbitMeConfiguration;
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.size()).thenReturn(0);

        when(dropbitMeConfiguration.hasVerifiedAccount()).thenReturn(true);
        when(dropbitMeConfiguration.isNewlyVerified()).thenReturn(true);
        when(dropbitMeConfiguration.shouldShowWhenPossible()).thenReturn(true);

        scenario = ActivityScenario.launch(HomeActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    @After
    public void tearDown() {
        scenario.close();
        transactions = null;
        walletHelper = null;
        dropbitMeConfiguration = null;
    }

    @Test
    public void hides_close_button() {
        scenario.onActivity(activity -> {
            DropBitMeDialog dialog = (DropBitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);

            assert dialog != null;
            assertThat(dialog.getView().findViewById(R.id.dialog_close).getVisibility(), equalTo(View.GONE));
        });
    }

    @Test
    public void shows_title() {
        scenario.onActivity(activity -> {
            DropBitMeDialog dialog = (DropBitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);

            assert dialog != null;
            TextView title = dialog.getView().findViewById(R.id.dialog_title);
            assert title != null;
            assertThat(title.getVisibility(), equalTo(View.VISIBLE));
            assertThat(title, hasText(activity.getString(R.string.dropbit_me_you_have_been_verified_title)));
        });
    }
}