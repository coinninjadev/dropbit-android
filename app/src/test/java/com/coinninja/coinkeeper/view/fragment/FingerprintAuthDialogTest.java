package com.coinninja.coinkeeper.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.presenter.fragment.FingerprintAuthPresenter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;

import junitx.util.PrivateAccessor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class FingerprintAuthDialogTest {

    private FingerprintAuthPresenter mockPresentor = mock(FingerprintAuthPresenter.class);
    private LayoutInflater mockLayoutInflater = mock(LayoutInflater.class);
    private ViewGroup mockViewGroup = mock(ViewGroup.class);
    private Bundle mockBundle = mock(Bundle.class);
    private View mockDialog = mock(View.class);
    private View mockNegativeButton = mock(View.class);
    private FingerprintAuthDialog dialog = mock(FingerprintAuthDialog.class);
    private FragmentController<FingerprintAuthDialog> fragmentController;

    @Before
    public void setUp() {
        fragmentController = Robolectric.buildFragment(FingerprintAuthDialog.class);
        dialog = fragmentController.get();
        dialog.setAuthUIPresentor(mockPresentor);
        dialog.setLayoutId(R.layout.dialog_fingerprint);
        fragmentController.create().resume().start().visible();
    }

    @After
    public void tearDown() {
        mockPresentor = null;
        mockLayoutInflater = null;
        mockViewGroup = null;
        mockBundle = null;
        mockNegativeButton = null;
        dialog = null;
        fragmentController = null;
    }

    @Test
    public void setsDialogOnPresentorWhenAttached() {
        verify(mockPresentor).setDialog(dialog);
    }

    @Test
    public void dialogFingerprintPreferenceViewInflatesOnCreateView() {
        assertNotNull(dialog.getView().findViewById(R.id.fingerprint_pref));
    }

    @Test
    public void onclickListenerIsSetOnNegativeButton() {
        mockOnCreateView();

        dialog.onCreateView(mockLayoutInflater, mockViewGroup, null);

        verify(mockNegativeButton).setOnClickListener(any());

    }

    @Test
    public void dialogIsNotCancelable() {
        assertThat(dialog.isCancelable(), equalTo(false));
    }

    @Test
    public void negativeResponsesForwardToParentActivity() {
        mockOnCreateView();

        FingerprintAuthDialog.newInstance(mockPresentor, R.layout.dialog_fingerprint).onCreateView(mockLayoutInflater, mockViewGroup, mockBundle);

        verify(mockNegativeButton).setOnClickListener(any());
    }

    @Test
    public void onAuthCancelForwardsToPresentor() {
        FingerprintAuthDialog dialog = FingerprintAuthDialog.newInstance(mockPresentor, R.layout.dialog_fingerprint);

        dialog.onAuthCancel();

        verify(mockPresentor).onAuthCancel();
    }

    @Test
    public void observesFingerprintAuthWhenResumed() {
        verify(mockPresentor).startListeningForTouch();
    }

    @Test
    public void stopsObservingFingerprintAuthWhenAppBackgrounded() {
        fragmentController.pause();

        verify(mockPresentor).stopListeningForTouch();
    }


    // Auth Success
    @Test
    public void updatesViewWhenAuthIsSuccessfull() throws NoSuchFieldException {
        dialog.onSucces();

        assertThat(((TextView) dialog.getView().findViewById(R.id.instructions)).getText().toString(),
                equalTo("Fingerprint recognized"));
    }

    // Auth Fail
    @Test
    public void updatesViewWhenAuthFails() throws NoSuchFieldException {
        dialog.onFailure();
        assertThat(((TextView) dialog.getView().findViewById(R.id.instructions)).getText().toString(),
                equalTo("Fingerprint not recognized. Try again"));

    }

    // Auth Error
    @Test
    public void updatesViewWhenAuthError() throws NoSuchFieldException {
        String message = "The Universe Just Exploaded";

        dialog.onError(1, message);

        assertThat(((TextView) dialog.getView().findViewById(R.id.instructions)).getText().toString(),
                equalTo(message));
    }

    // Auth Help
    @Test
    public void updatesViewWhenAuthHelp() throws NoSuchFieldException {
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

        verify(mockPresentor, times(1)).onSuccessfulTransition();
    }

    @Test
    public void dialogCanRenderSuppliedLayout() {

        String headerText = dialog.getResources().getString(R.string.sign_in);

        assertThat(((TextView) dialog.getView().findViewById(R.id.header)).getText().toString(), equalTo(headerText));
    }

    private void mockOnCreateView() {
        when(mockLayoutInflater.inflate(R.layout.dialog_fingerprint, mockViewGroup, false)).thenReturn(mockDialog);
        when(mockDialog.findViewById(R.id.negative_button)).thenReturn(mockNegativeButton);
    }
}