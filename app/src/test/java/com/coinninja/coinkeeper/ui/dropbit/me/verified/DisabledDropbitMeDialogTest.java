package com.coinninja.coinkeeper.ui.dropbit.me.verified;

import android.app.Dialog;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.home.HomeActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.matchers.IntentFilterMatchers;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowDialog;

import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class DisabledDropbitMeDialogTest {
    private ActivityScenario<HomeActivity> scenario;

    @Mock
    private LazyList transactions;

    @Mock
    private ActivityNavigationUtil activityNavigationUtil;

    @Mock
    private ServiceWorkUtil serviceWorkUtil;

    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @Mock
    private DropbitMeConfiguration dropbitMeConfiguration;
    private TestCoinKeeperApplication application;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        application = ApplicationProvider.getApplicationContext();
        application.activityNavigationUtil = activityNavigationUtil;
        application.dropbitMeConfiguration = dropbitMeConfiguration;
        application.serviceWorkUtil = serviceWorkUtil;
        application.localBroadCastUtil = localBroadCastUtil;
        when(application.walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.size()).thenReturn(0);

        when(dropbitMeConfiguration.hasVerifiedAccount()).thenReturn(true);
        when(dropbitMeConfiguration.shouldShowWhenPossible()).thenReturn(true);
        when(dropbitMeConfiguration.isDisabled()).thenReturn(true).thenReturn(false);
        when(dropbitMeConfiguration.getShareUrl()).thenReturn("https://dropbit.me/1234");

        scenario = ActivityScenario.launch(HomeActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    @After
    public void tearDown() {
        scenario.close();
        transactions = null;
        activityNavigationUtil = null;
        dropbitMeConfiguration = null;
        serviceWorkUtil = null;
        localBroadCastUtil = null;
        application = null;
    }

    @Test
    public void renders_content() {
        Dialog dialog = ShadowDialog.getLatestDialog();

        TextView view = dialog.findViewById(R.id.message);

        assertThat(view, hasText(application.getString(R.string.dropbit_me_disabled_paragraph_1)));
    }

    @Test
    public void configures_primary_button() {
        Dialog dialog = ShadowDialog.getLatestDialog();

        Button view = dialog.findViewById(R.id.dialog_primary_button);
        view.performClick();

        assertThat(view.getText().toString(), equalTo(application.getString(R.string.dropbit_me_enable_account_button_label)));
        verify(serviceWorkUtil).enableDropBitMe();
    }

    @Test
    public void configures_secondary_button_to_enable_public_account() {
        Dialog dialog = ShadowDialog.getLatestDialog();

        Button button = dialog.findViewById(R.id.dialog_secondary_button);
        button.performClick();

        ViewMatchers.assertThat(button.getText().toString(),
                equalTo(application.getString(R.string.dropbit_me_learn_more)));

        assertThat(button, isVisible());

        scenario.onActivity(activity -> {
            verify(activityNavigationUtil).learnMoreAboutDropbitMe(activity);
        });
    }

    @Test
    public void observes_account_disabled_change_when_disabling_dropbit_me() {
        scenario.onActivity(activity -> {
            DisabledDropbitMeDialog dialog = (DisabledDropbitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);
            assertThat(dialog.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_ENABLED));
            verify(localBroadCastUtil).registerReceiver(dialog.receiver, dialog.intentFilter);
        });
    }

    @Test
    public void unregisters_receiver_when_paused() {
        scenario.onActivity(activity -> {
            DisabledDropbitMeDialog dialog = (DisabledDropbitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);
            dialog.dismiss();
            verify(localBroadCastUtil).unregisterReceiver(dialog.receiver);
        });
    }

    @Test
    public void disabling_dropbit_me_account_shows_option_to_enable_account() {
        scenario.onActivity(activity -> {
            DisabledDropbitMeDialog dialog = (DisabledDropbitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);
            dialog.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_ENABLED));
        });


        scenario.onActivity(activity -> {
            DropBitMeDialog dialog = (DropBitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);
            Button primaryButton = dialog.getView().findViewById(R.id.dialog_primary_button);
            assertThat(primaryButton.getText().toString(), equalTo("SHARE ON TWITTER"));
        });
    }
}