package com.coinninja.coinkeeper.ui.account.verify;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.ui.settings.RemovePhoneNumberController;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class UserAccountVerificationActivityTest {

    @Mock
    WalletHelper walletHelper;

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    @Mock
    private RemovePhoneNumberController removePhoneNumberController;

    private UserAccountVerificationActivity activity;
    private ShadowActivity shadowActivity;
    private ActivityController<UserAccountVerificationActivity> activityController;
    
    @Mock
    private PhoneNumber phoneNumber;

    private void start() {
        activityController.start().resume().visible();
    }

    @After
    public void tearDown() {
        activityController = null;
        shadowActivity = null;
    }

    @Before
    public void setUp() {
        TestCoinKeeperApplication coinKeeperApplication = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        when(coinKeeperApplication.authentication.isAuthenticated()).thenReturn(true);
        MockitoAnnotations.initMocks(this);
        activityController = Robolectric.buildActivity(UserAccountVerificationActivity.class);
        activityController.create();
        activity = activityController.get();
        activity.removePhoneNumberController = removePhoneNumberController;
        activity.walletHelper = walletHelper;
        activity.localBroadCastUtil = localBroadCastUtil;
        shadowActivity = shadowOf(activity);
    }

    @Test
    public void instructs_controller_to_start() {
        start();

        verify(removePhoneNumberController).onStart();
    }

    @Test
    public void requests_controllers_to_clean_up_when_destroyed() {
        activityController.stop();

        verify(removePhoneNumberController).onStop();
    }

    @Test
    public void selecting_verification_prompts_to_remove_phone_number() {
        Account account = mock(Account.class);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        when(walletHelper.getUserAccount()).thenReturn(account);
        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        start();

        View view = activity.findViewById(R.id.change_remove_button);
        view.performClick();

        verify(removePhoneNumberController).onRemovePhoneNumber(view);
    }

    @Test
    public void phone_verification_preference_show_phone_number() {
        Account account = mock(Account.class);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        when(walletHelper.getUserAccount()).thenReturn(account);
        PhoneNumber phoneNumber = new PhoneNumber("+12223334444");
        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        String national = "(222) 333-4444";
        start();

        TextView phoneNumberValue = activity.findViewById(R.id.verified_number_text_view);
        assertThat(phoneNumberValue.getText().toString(), equalTo(national));
    }

    @Test
    public void navigates_to_phone_verification_screen() {
        start();

        activity.findViewById(R.id.verify_phone_button).performClick();

        Intent startedActivity = shadowActivity.getNextStartedActivity();
        assertThat(startedActivity.getComponent().getClassName(), equalTo(VerifyPhoneNumberActivity.class.getName()));
    }


    @Test
    public void stops_observing_deverification_completed() {
        start();

        activityController.stop();

        verify(localBroadCastUtil).unregisterReceiver(activity.receiver);
    }

    @Test
    public void redraws_phone_verification_when_deverification_completed() {
        Account account = mock(Account.class);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true).thenReturn(false);
        when(walletHelper.getUserAccount()).thenReturn(account);
        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        start();

        activity.receiver.onReceive(activity, new Intent(Intents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED));

        verify(walletHelper, times(2)).hasVerifiedAccount();
        assertThat(activity.findViewById(R.id.unverified_phone_group).getVisibility(), equalTo(View.VISIBLE));
        assertThat(activity.findViewById(R.id.verified_phone_group).getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void observes_deverification_completed() {
        start();


        verify(localBroadCastUtil).registerReceiver(activity.receiver, activity.intentFilter);
    }
}