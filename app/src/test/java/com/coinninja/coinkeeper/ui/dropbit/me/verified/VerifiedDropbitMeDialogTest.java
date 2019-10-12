package com.coinninja.coinkeeper.ui.dropbit.me.verified;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Button;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.home.HomeActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.matchers.IntentFilterMatchers;

import org.greenrobot.greendao.query.LazyList;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowDialog;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class VerifiedDropbitMeDialogTest {
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

        when(dropbitMeConfiguration.isDisabled()).thenReturn(false).thenReturn(true);
        when(dropbitMeConfiguration.hasVerifiedAccount()).thenReturn(true);
        when(dropbitMeConfiguration.shouldShowWhenPossible()).thenReturn(true);
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

        ViewMatchers.assertThat(dialog.findViewById(R.id.message),
                hasText(application.getString(R.string.dropbit_me_verified_paragraph_1)));
    }

    @Test
    public void configures_primary_action() {
        Dialog dialog = ShadowDialog.getLatestDialog();

        Button button = dialog.findViewById(R.id.dialog_primary_button);
        button.performClick();

        ViewMatchers.assertThat(button.getText().toString(),
                equalTo(application.getString(R.string.dropbit_me_verified_account_button)));

        Drawable[] altCompoundDrawables = button.getCompoundDrawables();
        MatcherAssert.assertThat(shadowOf(altCompoundDrawables[0]).getCreatedFromResId(), equalTo(R.drawable.twitter_icon));
        assertNull(altCompoundDrawables[1]);
        assertNull(altCompoundDrawables[2]);
        assertNull(altCompoundDrawables[3]);

        scenario.onActivity(activity -> {
            verify(activityNavigationUtil).shareWithTwitter(activity,
                    "Pay me in #Bitcoin using my Dropbit.me https://dropbit.me/1234");
        });

        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void sets_copy_buttons_text_with_share() {

        Dialog dialog = ShadowDialog.getLatestDialog();
        Button button = dialog.findViewById(R.id.dropbit_me_url);

        assertThat(button.getText().toString(), equalTo(dropbitMeConfiguration.getShareUrl()));
    }

    @Test
    public void configures_secondary_button_to_disable_public_account() {
        Dialog dialog = ShadowDialog.getLatestDialog();

        Button button = dialog.findViewById(R.id.dialog_secondary_button);
        button.performClick();

        ViewMatchers.assertThat(button.getText().toString(),
                equalTo(application.getString(R.string.dropbit_me_disable_account_button_label)));

        assertThat(button, isVisible());

        verify(serviceWorkUtil).disableDropBitMe();
    }

    @Test
    public void observes_account_disabled_change_when_disabling_dropbit_me() {
        scenario.onActivity(activity -> {
            VerifiedDropbitMeDialog dialog = (VerifiedDropbitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);
            assertThat(dialog.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_DISABLED));
            verify(localBroadCastUtil).registerReceiver(dialog.receiver, dialog.intentFilter);
        });
    }

    @Test
    public void unregisters_receiver_when_paused() {
        scenario.onActivity(activity -> {
            VerifiedDropbitMeDialog dialog = (VerifiedDropbitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);
            dialog.dismiss();
            verify(localBroadCastUtil).unregisterReceiver(dialog.receiver);
        });
    }

    @Test
    public void disabling_dropbit_me_account_shows_option_to_enable_account() {
        scenario.onActivity(activity -> {
            VerifiedDropbitMeDialog dialog = (VerifiedDropbitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);
            dialog.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_DISABLED));
        });


        scenario.onActivity(activity -> {
            DropBitMeDialog dialog = (DropBitMeDialog) activity.getSupportFragmentManager().findFragmentByTag(DropBitMeDialog.TAG);
            Button primaryButton = dialog.getView().findViewById(R.id.dialog_primary_button);
            assertThat(primaryButton.getText().toString(), equalTo("ENABLE MY URL"));
        });
    }
}