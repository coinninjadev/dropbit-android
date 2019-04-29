package com.coinninja.coinkeeper.view.fragment;

import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.presenter.fragment.FingerprintAuthPresenter;
import com.coinninja.coinkeeper.view.activity.SplashActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitx.util.PrivateAccessor;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class FingerprintAuthDialogTest {

    private FingerprintAuthPresenter authPresenter = mock(FingerprintAuthPresenter.class);
    private FingerprintAuthDialog dialog;
    private ActivityScenario<SplashActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(SplashActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);
        dialog = FingerprintAuthDialog.newInstance(authPresenter, R.layout.dialog_fingerprint);

        scenario.onActivity(new ActivityScenario.ActivityAction<SplashActivity>() {
            @Override
            public void perform(SplashActivity activity) {
                dialog.show(activity.getSupportFragmentManager(), FingerprintAuthDialog.class.getName());
            }
        });
    }

    @After
    public void tearDown() {
        authPresenter = null;
        dialog = null;
        scenario.close();
    }

    @Test
    public void setsDialogOnPresentorWhenAttached() {
        verify(authPresenter).setDialog(dialog);
    }

    @Test
    public void dialogFingerprintPreferenceViewInflatesOnCreateView() {
        assertNotNull(dialog.getView().findViewById(R.id.fingerprint_pref));
    }

    @Test
    public void dialogIsNotCancelable() {
        assertThat(dialog.isCancelable(), equalTo(false));
    }

    @Test
    public void negativeResponsesForwardToParentActivity() {
        clickOn(withId(dialog.getView(), R.id.negative_button));

        verify(authPresenter).onAuthCancel();
    }

    @Test
    public void onAuthCancelForwardsToPresentor() {
        FingerprintAuthDialog dialog = FingerprintAuthDialog.newInstance(authPresenter, R.layout.dialog_fingerprint);

        dialog.onAuthCancel();

        verify(authPresenter).onAuthCancel();
    }

    @Test
    public void observesFingerprintAuthWhenResumed() {
        verify(authPresenter).startListeningForTouch();
    }

    @Test
    public void stopsObservingFingerprintAuthWhenAppBackgrounded() {
        scenario.moveToState(Lifecycle.State.DESTROYED);

        verify(authPresenter).stopListeningForTouch();
    }


    // Auth Success
    @Test
    public void updatesViewWhenAuthIsSuccessfull() {
        dialog.onSucces();

        assertThat(((TextView) dialog.getView().findViewById(R.id.instructions)).getText().toString(),
                equalTo("Fingerprint recognized"));
    }

    // Auth Fail
    @Test
    public void updatesViewWhenAuthFails() {
        dialog.onFailure();
        assertThat(((TextView) dialog.getView().findViewById(R.id.instructions)).getText().toString(),
                equalTo("Fingerprint not recognized. Try again"));

    }

    // Auth Error
    @Test
    public void updatesViewWhenAuthError() {
        String message = "The Universe Just Exploaded";

        dialog.onError(1, message);

        assertThat(((TextView) dialog.getView().findViewById(R.id.instructions)).getText().toString(),
                equalTo(message));
    }

    // Auth Help
    @Test
    public void updatesViewWhenAuthHelp() {
        String message = "Touch sensor";
        int color = 0xf00000;

        dialog.onHelp(1, message);

        assertThat(((TextView) dialog.getView().findViewById(R.id.instructions)).getText().toString(),
                equalTo(message));
    }

    @Test
    public void notifiesPresentorOfSuccessfulTransition() throws NoSuchFieldException {
        Runnable successRunnable = (Runnable) PrivateAccessor.getField(dialog, "successRunnable");

        successRunnable.run();

        verify(authPresenter, times(1)).onSuccessfulTransition();
    }

    @Test
    public void dialogCanRenderSuppliedLayout() {
        String headerText = dialog.getResources().getString(R.string.sign_in);

        assertThat(((TextView) dialog.getView().findViewById(R.id.header)).getText().toString(), equalTo(headerText));
    }
}