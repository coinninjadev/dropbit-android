package com.coinninja.coinkeeper.cn.service.runner;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.service.CoinNinjaServiceCheck.DeverifiedCause;
import com.coinninja.coinkeeper.cn.service.UserVerificationServiceCheck;
import com.coinninja.coinkeeper.cn.service.WalletVerificationServiceCheck;
import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.NotificationUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountDeverificationServiceRunnerTest {

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private DropbitAccountHelper dropbitAccountHelper;

    @Mock
    private UserVerificationServiceCheck userVerificationServiceCheck;

    @Mock
    private WalletVerificationServiceCheck walletVerificationServiceCheck;

    @Mock
    private NotificationUtil notificationUtil;

    @Mock
    private Analytics analytics;

    @Mock
    private ServiceWorkUtil serviceWorkUtil;

    @InjectMocks
    private AccountDeverificationServiceRunner runner;

    @Before
    public void setUp() {
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(false);
        when(walletHelper.hasAccount()).thenReturn(false);
    }

    @After
    public void tearDown() throws Exception {
        walletHelper = null;
        dropbitAccountHelper = null;
        userVerificationServiceCheck = null;
        walletVerificationServiceCheck = null;
        notificationUtil = null;
        analytics = null;
        runner = null;
        serviceWorkUtil = null;
    }

    @Test
    public void reports_wallet_deverification__dropped() throws CNServiceException, JSONException {
        String expectedMessage = String.format(runner.debugMessage, AccountDeverificationServiceRunner.WALLET, "JSON: error_message -- dropped");
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        when(walletHelper.hasAccount()).thenReturn(true);
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(false);
        when(walletVerificationServiceCheck.isVerified()).thenReturn(false);
        when(walletVerificationServiceCheck.getRaw()).thenReturn("JSON: error_message -- dropped");

        runner.run();

        verify(analytics).trackEvent(eq(Analytics.Companion.EVENT_PHONE_AUTO_DEVERIFIED), captor.capture());

        JSONObject response = captor.getValue();
        assertThat(response.get("debugMessage"), equalTo(expectedMessage));
    }

    @Test
    public void reports_account_deverification__mismatch() throws CNServiceException, JSONException {
        String expectedMessage = String.format(runner.debugMessage, AccountDeverificationServiceRunner.USER, "JSON: error_message -- mismatched");
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        when(walletHelper.hasAccount()).thenReturn(true);
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(true);
        when(userVerificationServiceCheck.isVerified()).thenReturn(false);
        when(userVerificationServiceCheck.deverificaitonReason()).thenReturn(DeverifiedCause.MISMATCH);
        when(walletVerificationServiceCheck.isVerified()).thenReturn(true);
        when(userVerificationServiceCheck.getRaw()).thenReturn("JSON: error_message -- mismatched");

        runner.run();


        verify(analytics).trackEvent(eq(Analytics.Companion.EVENT_PHONE_AUTO_DEVERIFIED), captor.capture());

        JSONObject response = captor.getValue();
        assertThat(response.get("debugMessage"), equalTo(expectedMessage));
    }

    @Test
    public void reports_account_deverification__dropped() throws CNServiceException, JSONException {
        String expectedMessage = String.format(runner.debugMessage, AccountDeverificationServiceRunner.USER, "JSON: error_message -- dropped");
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        when(walletHelper.hasAccount()).thenReturn(true);
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(true);
        when(userVerificationServiceCheck.isVerified()).thenReturn(false);
        when(userVerificationServiceCheck.deverificaitonReason()).thenReturn(DeverifiedCause.DROPPED);
        when(walletVerificationServiceCheck.isVerified()).thenReturn(true);
        when(userVerificationServiceCheck.getRaw()).thenReturn("JSON: error_message -- dropped");

        runner.run();

        verify(analytics).trackEvent(eq(Analytics.Companion.EVENT_PHONE_AUTO_DEVERIFIED), captor.capture());

        JSONObject response = captor.getValue();
        assertThat(response.get("debugMessage"), equalTo(expectedMessage));
    }

    @Test
    public void notifies_of_account_mismatch() throws CNServiceException {
        when(walletHelper.hasAccount()).thenReturn(true);
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(true);
        when(userVerificationServiceCheck.isVerified()).thenReturn(false);
        when(userVerificationServiceCheck.deverificaitonReason()).thenReturn(DeverifiedCause.MISMATCH);

        runner.run();

        verify(notificationUtil).dispatchInternal(R.string.mismatch_401_user_deverifcation_message);
        verify(serviceWorkUtil).deVerifyPhoneNumber();
        verify(serviceWorkUtil).deVerifyTwitter();
    }

    @Test
    public void notifies_of_dropped_accounts() throws CNServiceException {
        when(walletHelper.hasAccount()).thenReturn(true);
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(true);
        when(userVerificationServiceCheck.isVerified()).thenReturn(false);
        when(userVerificationServiceCheck.deverificaitonReason()).thenReturn(DeverifiedCause.DROPPED);

        runner.run();

        verify(notificationUtil).dispatchInternal(R.string.default_401_user_deverifcation_message);
    }

    @Test
    public void performs_deverification_of_wallet_when_account_and_wallet_are_not_verified() throws CNServiceException {
        when(walletHelper.hasAccount()).thenReturn(true);
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(true);
        when(userVerificationServiceCheck.isVerified()).thenReturn(false);
        when(walletVerificationServiceCheck.isVerified()).thenReturn(false);

        runner.run();

        verify(walletVerificationServiceCheck).performDeverification();
    }

    @Test
    public void performs_deverification_of_account_when_account_is_not_verified() throws CNServiceException {
        when(walletHelper.hasAccount()).thenReturn(true);
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(true);
        when(userVerificationServiceCheck.isVerified()).thenReturn(false);

        runner.run();

        verify(userVerificationServiceCheck).performDeverification();
    }

    @Test
    public void does_not_verify_wallet_when_account_is_verified() throws CNServiceException {
        when(walletHelper.hasAccount()).thenReturn(true);
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(true);
        when(userVerificationServiceCheck.isVerified()).thenReturn(true);

        runner.run();

        verify(walletVerificationServiceCheck, times(0)).isVerified();
    }

    @Test
    public void verifies_wallet_when_account_is_not_verified() throws CNServiceException {
        when(walletHelper.hasAccount()).thenReturn(true);
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(true);
        when(userVerificationServiceCheck.isVerified()).thenReturn(false);

        runner.run();

        verify(walletVerificationServiceCheck).isVerified();
    }

    @Test
    public void performs_wallet_deverification_when_wallet_is_not_verified() throws CNServiceException {
        when(walletHelper.hasAccount()).thenReturn(true);
        when(walletVerificationServiceCheck.isVerified()).thenReturn(false);

        runner.run();

        verify(walletVerificationServiceCheck).performDeverification();
    }

    @Test
    public void verifies_wallet_when_account_not_verified() throws CNServiceException {
        when(walletHelper.hasAccount()).thenReturn(true);

        runner.run();

        verify(walletVerificationServiceCheck).isVerified();
    }

    @Test
    public void verifies_account_when_present() throws CNServiceException {
        when(dropbitAccountHelper.getHasVerifiedAccount()).thenReturn(true);
        when(walletHelper.hasAccount()).thenReturn(true);

        runner.run();

        verify(userVerificationServiceCheck).isVerified();
    }

    @Test
    public void does_not_verify_wallet_when_no_account() throws CNServiceException {
        runner.run();

        verify(walletVerificationServiceCheck, times(0)).isVerified();
    }

    @Test
    public void does_not_verify_account_when_no_account_to_verify() throws CNServiceException {
        runner.run();

        verify(userVerificationServiceCheck, times(0)).isVerified();
    }

}