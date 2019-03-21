package com.coinninja.coinkeeper.view.activity.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.receiver.AuthenticationCompleteReceiver;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.view.activity.AuthenticateActivity;
import com.coinninja.coinkeeper.view.activity.CreatePinActivity;
import com.coinninja.coinkeeper.view.activity.RecoverWalletActivity;
import com.coinninja.coinkeeper.view.activity.RestoreWalletActivity;
import com.coinninja.coinkeeper.view.activity.StartActivity;
import com.coinninja.coinkeeper.view.activity.TransactionHistoryActivity;
import com.coinninja.coinkeeper.view.adapter.RestoreWalletPageAdapter;

import org.greenrobot.greendao.query.LazyList;
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

import androidx.viewpager.widget.ViewPager;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class SecuredActivityTest {

    private ShadowActivity shadowActivity;
    private SecuredActivity activity;
    private TestCoinKeeperApplication application;

    @Mock
    private PinEntry pinEntry;
    @Mock
    private CNWalletManager cnWalletManager;
    @Mock
    private Authentication authentication;

    @Mock
    WalletHelper walletHelper;

    @Mock
    LazyList<TransactionsInvitesSummary> transactions;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(transactions.isEmpty()).thenReturn(true);
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        application.authentication = authentication;
        application.pinEntry = pinEntry;
        application.cnWalletManager = cnWalletManager;
        application.walletHelper = walletHelper;
        when(application.authentication.isAuthenticated()).thenReturn(false);
    }

    @After
    public void tearDown() {
        shadowActivity = null;
        activity = null;
        application = null;
        authentication = null;
        pinEntry = null;
        cnWalletManager = null;
    }

    public void setupActivity(Class activityClass) {
        ActivityController activityController = Robolectric.buildActivity(activityClass);
        activity = (SecuredActivity) activityController.get();
        activityController.create();
        shadowActivity = shadowOf(activity);
        activityController.start().resume().visible();
    }

    @Test
    public void create_pint_activity_does_not_requrie_a_pin_or_auth() {
        setupActivity(CreatePinActivity.class);

        assertNull(shadowActivity.getNextStartedActivity());
    }

    @Test
    public void recover_wallet_does_not_require_a_pin_or_auth() {
        setupActivity(RecoverWalletActivity.class);

        assertNull(shadowActivity.getNextStartedActivity());
    }

    @Test
    public void restore_wallet_does_not_require_a_pin_or_auth() {
        ActivityController<RestoreWalletActivity> activityController = Robolectric.buildActivity(RestoreWalletActivity.class);
        SecuredActivity activity = activityController.get();
        activityController.create();
        ((ViewPager) activity.findViewById(R.id.recovery_words_pager)).setAdapter(mock(RestoreWalletPageAdapter.class));
        activityController.start().resume().visible();
        shadowActivity = shadowOf(activity);
        assertNull(shadowActivity.getNextStartedActivity());
    }

    @Test
    public void start_activity_excluded_from_no_pin_redirect() {
        setupActivity(StartActivity.class);

        assertNull(shadowActivity.getNextStartedActivity());
    }

    @Test
    public void on_auth_successful_send_ON_USER_AUTH_SUCCESSFUL_broadcast() {
        when(authentication.isAuthenticated()).thenReturn(true);
        setupActivity(TransactionHistoryActivity.class);

        activity.onAuthenticationResult(Activity.RESULT_OK);

        verify(activity.localBroadCastUtil).sendGlobalBroadcast(AuthenticationCompleteReceiver.class, Intents.ACTION_ON_USER_AUTH_SUCCESSFULLY);
    }

    @Test
    public void authenticates_user_on_session_resume() {
        when(authentication.isAuthenticated()).thenReturn(false);
        when(application.cnWalletManager.hasWallet()).thenReturn(true);
        when(application.pinEntry.hasExistingPin()).thenReturn(true);

        setupActivity(TransactionHistoryActivity.class);

        ShadowActivity.IntentForResult intent = shadowActivity.getNextStartedActivityForResult();

        assertThat(intent.requestCode, equalTo(SecuredActivity.AUTHENTICATION_REQUEST_CODE));
        assertThat(intent.intent.getComponent().getClassName(),
                equalTo(AuthenticateActivity.class.getName()));
    }

    @Test
    public void successful_authentication_does_not_reauth() {
        when(application.authentication.isAuthenticated()).thenReturn(false).thenReturn(true);
        when(application.pinEntry.hasExistingPin()).thenReturn(true);
        when(application.cnWalletManager.hasWallet()).thenReturn(true);

        setupActivity(TransactionHistoryActivity.class);

        ShadowActivity.IntentForResult intent = shadowActivity.getNextStartedActivityForResult();
        assertThat(intent.requestCode, equalTo(SecuredActivity.AUTHENTICATION_REQUEST_CODE));
        assertThat(intent.intent.getComponent().getClassName(),
                equalTo(AuthenticateActivity.class.getName()));

        activity.onActivityResult(SecuredActivity.AUTHENTICATION_REQUEST_CODE, Activity.RESULT_OK, null);

        intent = shadowActivity.getNextStartedActivityForResult();

        assertNull(intent);
    }

    @Test
    public void failed_authentication_requthenticates() {
        when(authentication.isAuthenticated()).thenReturn(false);
        when(application.pinEntry.hasExistingPin()).thenReturn(true);
        when(application.cnWalletManager.hasWallet()).thenReturn(true);

        setupActivity(TransactionHistoryActivity.class);

        ShadowActivity.IntentForResult intent = shadowActivity.getNextStartedActivityForResult();
        assertThat(intent.requestCode, equalTo(SecuredActivity.AUTHENTICATION_REQUEST_CODE));
        assertThat(intent.intent.getComponent().getClassName(),
                equalTo(AuthenticateActivity.class.getName()));

        activity.onActivityResult(SecuredActivity.AUTHENTICATION_REQUEST_CODE, Activity.RESULT_OK, null);

        intent = shadowActivity.getNextStartedActivityForResult();


        assertThat(intent.requestCode, equalTo(SecuredActivity.AUTHENTICATION_REQUEST_CODE));
        assertThat(intent.intent.getComponent().getClassName(),
                equalTo(AuthenticateActivity.class.getName()));
    }

    @Test
    public void canceling_authentication_finishes_activity() {
        when(authentication.isAuthenticated()).thenReturn(false);
        when(application.pinEntry.hasExistingPin()).thenReturn(true);
        when(application.cnWalletManager.hasWallet()).thenReturn(true);
        setupActivity(TransactionHistoryActivity.class);

        activity.onActivityResult(SecuredActivity.AUTHENTICATION_REQUEST_CODE, Activity.RESULT_CANCELED, null);

        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void does_not_inturupt_users_screen_when_authenticated() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(application.pinEntry.hasExistingPin()).thenReturn(true);

        setupActivity(TransactionHistoryActivity.class);

        assertNull(shadowActivity.getNextStartedActivity());
    }

    @Test
    public void does_not_navigate_to_create_pin_with_verify_phone_when_isinstance_of_create_pin() {
        when(application.cnWalletManager.hasWallet()).thenReturn(true);

        setupActivity(CreatePinActivity.class);

        Intent intent = shadowActivity.getNextStartedActivity();

        assertNull(intent);
    }

    @Test
    public void navigates_to_create_pin_with_verify_phone_next_when_no_pin_and_backup() {
        when(application.pinEntry.hasExistingPin()).thenReturn(false);
        when(application.cnWalletManager.hasWallet()).thenReturn(true);

        setupActivity(TransactionHistoryActivity.class);

        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getComponent().getClassName(),
                equalTo(CreatePinActivity.class.getName()));
        assertThat(intent.getStringExtra(Intents.EXTRA_NEXT),
                equalTo(VerifyPhoneNumberActivity.class.getName()));
    }

    @Test
    public void redirects_user_to_start_actiivty_when_pin_is_empty_and_no_backup() {
        when(application.pinEntry.hasExistingPin()).thenReturn(false);
        when(application.cnWalletManager.hasWallet()).thenReturn(false);

        setupActivity(TransactionHistoryActivity.class);

        assertThat(shadowActivity.getNextStartedActivity().getComponent().getClassName(),
                equalTo(StartActivity.class.getName()));
    }

    @Test
    public void forwards_bundle_to_next_activity() {
        when(application.pinEntry.hasExistingPin()).thenReturn(true);
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_NEXT, TransactionHistoryActivity.class.getName());
        Bundle bundle_to_forward = new Bundle();
        String[] recovery_words = {"foo", "bar"};
        bundle_to_forward.putStringArray(Intents.EXTRA_RECOVERY_WORDS, recovery_words);
        intent.putExtra(Intents.EXTRA_NEXT_BUNDLE, bundle_to_forward);


        ActivityController<CreatePinActivity> activityController =
                Robolectric.buildActivity(CreatePinActivity.class, intent);
        CreatePinActivity activity = activityController.get();
        activityController.newIntent(intent).create();
        activityController.start().resume().visible();
        ShadowActivity shadowActivity = shadowOf(activity);


        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(TransactionHistoryActivity.class.getName()));
        assertThat(startedIntent.getExtras().getStringArray(Intents.EXTRA_RECOVERY_WORDS),
                equalTo(recovery_words));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void shows_next_activity_from_creation_intent() {
        when(application.pinEntry.hasExistingPin()).thenReturn(true);
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_NEXT, TransactionHistoryActivity.class.getName());
        ActivityController<CreatePinActivity> activityController =
                Robolectric.buildActivity(CreatePinActivity.class, intent);
        CreatePinActivity activity = activityController.get();
        activityController.newIntent(intent).create();
        activityController.start().resume().visible();
        ShadowActivity shadowActivity = shadowOf(activity);


        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(TransactionHistoryActivity.class.getName()));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void pull_data_out_of_Bundle_using_key_then_add_to_root_intent() {
        when(application.pinEntry.hasExistingPin()).thenReturn(true);
        Bundle extraData = new Bundle();
        Intent intent = new Intent();
        extraData.putString("random key", "random arbitrary data");
        intent.putExtra(Intents.EXTRA_NEXT_BUNDLE, extraData);

        ActivityController<TransactionHistoryActivity> activityController =
                Robolectric.buildActivity(TransactionHistoryActivity.class, intent).create();
        TransactionHistoryActivity activity = activityController.get();

        activity.showNext();
        ShadowActivity shadowActivity = shadowOf(activity);


        Intent startedIntent = shadowActivity.getNextStartedActivity();
        activity.startActivity(startedIntent);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getString("random key"), equalTo("random arbitrary data"));
        assertTrue(shadowActivity.isFinishing());
    }

}