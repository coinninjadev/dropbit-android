package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.presenter.fragment.PinFragmentPresenter;
import com.coinninja.coinkeeper.service.WalletCreationIntentService;
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.view.fragment.FingerprintAuthDialog;
import com.coinninja.coinkeeper.view.fragment.PinConfirmFragment;
import com.coinninja.coinkeeper.view.fragment.PinCreateFragment;

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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CreatePinActivityTest {

    @Mock
    PinEntry pinEntry;
    private ActivityController<CreatePinActivity> activityController;
    private CreatePinActivity activity;
    private PinFragmentPresenter pinFragmentPresenter;
    private ShadowActivity shadowActivity;
    private TestCoinKeeperApplication application;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        application.pinEntry = pinEntry;
        pinFragmentPresenter = mock(PinFragmentPresenter.class);
    }

    @Test
    public void shows_next_activity_if_pin_exists() {
        when(pinEntry.hasExistingPin()).thenReturn(true);

        String nextActivity = initWithNextIntent();

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(nextActivity));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void invokes_completion_service_activity_if_pin_exists() {
        when(pinEntry.hasExistingPin()).thenReturn(true);

        Intent invocationIntent = initWithCompletionAndNextIntents();

        Intent startedActivity = shadowActivity.getNextStartedActivity();
        assertThat(startedActivity.getComponent().getClassName(),
                equalTo(invocationIntent.getStringExtra(DropbitIntents.EXTRA_NEXT)));
        Intent startedService = shadowActivity.getNextStartedService();
        Intent completionIntent = (Intent) invocationIntent.getExtras().get(DropbitIntents.EXTRA_ON_COMPLETION);
        assertThat(startedService.getComponent().getClassName(),
                equalTo(completionIntent.getComponent().getClassName()));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void activity_attaches_to_controllers() {
        initWithIntent(new Intent());

        verify(pinFragmentPresenter).attachView(activity);
    }

    @Test
    public void requests_pin() {
        initWithIntent(new Intent());

        assertNotNull(activity.getSupportFragmentManager().
                findFragmentByTag(CreatePinActivity.TAG_CREATE_PIN));
    }

    @Test
    public void shows_confirm_pin_on_demand() {
        initWithNextIntent();

        activity.showConfirmPin();

        assertNotNull(activity.getSupportFragmentManager().
                findFragmentByTag(CreatePinActivity.TAG_CONFIRM_PIN));
    }

    @Test
    public void authenticates_when_pin_saved_and_confirmed() {
        initWithNextIntent();

        activity.onPinConfirmed("---hashed_pin---");

        verify(application.authentication).setAuthenticated();
    }

    @Test
    public void navigates_to_next_screen_once_authenicated() {
        String nextActivity = initWithNextIntent();

        activity.onPinConfirmed("---hashed_pin---");

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(nextActivity));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void navigates_to_next_when_fingerpirnt_prefrence_canceled() {
        String nextActivity = initWithNextIntent();
        activity.setFingerPrintAuthDialog(mock(FingerprintAuthDialog.class));

        activity.onFingerprintAuthenticationCanceled();

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(nextActivity));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void navigates_to_next_when_fingerprint_not_availabe() {
        String nextActivity = initWithNextIntent();

        activity.onFingerprintAuthenticationNotAvailable();

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(nextActivity));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void forces_soft_keyboard() {
        initWithNextIntent();
        activity.pinFragment = mock(PinCreateFragment.class);

        activity.forceSoftKey();

        verify(activity.pinFragment).forceShowSoftKey();
    }

    @Test
    public void navigates_to_start_when_next_not_specified() {
        when(pinEntry.hasExistingPin()).thenReturn(true);

        Intent intent = new Intent();
        intent.putExtra(Intent.ACTION_BATTERY_LOW, "dummy-extra-so-addressForPubKey-extra-is-not-null");
        initWithIntent(intent);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(StartActivity.class.getName()));
    }

    @Test
    public void goes_to_start_screen_when_rubbish_next_supplied() {
        when(pinEntry.hasExistingPin()).thenReturn(true);

        Intent intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_NEXT, "rubbish");
        initWithIntent(intent);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(StartActivity.class.getName()));
    }

    @Test
    public void onPinMismatch() {
        initWithNextIntent();

        activity.confirmFragment = mock(PinConfirmFragment.class);
        activity.onPinMismatch();

        verify(activity.confirmFragment).showPinMismatch();
        verify(activity.confirmFragment, never()).onDismissRequest();
    }

    @Test
    public void onPinMismatchFATAL() {
        initWithNextIntent();
        activity.confirmFragment = mock(PinConfirmFragment.class);

        activity.onPinMismatchFATAL();

        verify(activity.confirmFragment, never()).showPinMismatch();
        verify(activity.confirmFragment).onDismissRequest();
        verify(pinFragmentPresenter).clearPin();
        verify(activity.getActionBarController()).displayTitle(activity, activity.getString(R.string.set_pin_header));
    }

    private void initWithIntent(Intent intent) {
        activityController = Robolectric.buildActivity(CreatePinActivity.class, intent);
        activity = activityController.get();
        activityController.newIntent(intent).create();
        activity.setPinFragmentPresenter(pinFragmentPresenter);
        activityController.start().resume().visible();
        shadowActivity = shadowOf(activity);
    }

    private String initWithNextIntent() {
        Intent intent = new Intent(application, CreatePinActivity.class);
        String nextActivity = VerificationActivity.class.getName();
        intent.putExtra(DropbitIntents.EXTRA_NEXT, nextActivity);

        initWithIntent(intent);
        return nextActivity;
    }

    private Intent initWithCompletionAndNextIntents() {
        Intent intent = new Intent(application, CreatePinActivity.class);
        String nextActivity = VerificationActivity.class.getName();
        intent.putExtra(DropbitIntents.EXTRA_NEXT, nextActivity);
        intent.putExtra(DropbitIntents.EXTRA_ON_COMPLETION, new Intent(application, WalletCreationIntentService.class));

        initWithIntent(intent);
        return intent;
    }
}