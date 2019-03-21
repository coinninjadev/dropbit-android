package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.presenter.fragment.PinFragmentPresenter;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.util.Intents;
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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CreatePinActivityTest {

    private ActivityController<CreatePinActivity> activityController;
    private CreatePinActivity activity;
    private PinFragmentPresenter pinFragmentPresenter;
    private ShadowActivity shadowActivity;
    private TestCoinKeeperApplication application;

    @Mock
    PinEntry pinEntry;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        application.pinEntry = pinEntry;
        pinFragmentPresenter = mock(PinFragmentPresenter.class);
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
        String nextActivity = VerifyPhoneNumberActivity.class.getName();
        intent.putExtra(Intents.EXTRA_NEXT, nextActivity);

        initWithIntent(intent);
        return nextActivity;
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
    public void activity_attaches_to_controllers() {
        initWithIntent(new Intent());

        verify(pinFragmentPresenter).attachView(activity);
    }

    @Test
    public void requests_pin() {
        initWithIntent(new Intent());

        assertNotNull(activity.getFragmentManager().
                findFragmentByTag(CreatePinActivity.TAG_CREATE_PIN));
    }

    @Test
    public void shows_confirm_pin_on_demand() {
        initWithNextIntent();

        activity.showConfirmPin();

        assertNotNull(activity.getFragmentManager().
                findFragmentByTag(CreatePinActivity.TAG_CONFIRM_PIN));
    }

    @Test
    public void authenticates_when_pin_saved_and_confirmed() {
        initWithNextIntent();

        activity.onPinConfirmedAndSaved("---hashed_pin---");

        verify(application.authentication).setAuthenticated();
    }

    @Test
    public void navigates_to_next_screen_once_authenicated() {
        String nextActivity = initWithNextIntent();

        activity.onPinConfirmedAndSaved("---hashed_pin---");

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
        initWithIntent(intent);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(StartActivity.class.getName()));
    }

    @Test
    public void goes_to_start_screen_when_rubbish_next_supplied() {
        when(pinEntry.hasExistingPin()).thenReturn(true);

        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_NEXT, "rubbish");
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
    }
}