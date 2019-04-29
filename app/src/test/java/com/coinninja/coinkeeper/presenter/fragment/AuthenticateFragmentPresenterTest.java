package com.coinninja.coinkeeper.presenter.fragment;

import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.interfaces.PinEntry.PinCompare;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.service.tasks.LockUserTask;
import com.coinninja.coinkeeper.util.DateUtil;
import com.coinninja.coinkeeper.util.DropbitIntents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import junitx.util.PrivateAccessor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateFragmentPresenterTest {
    @Mock
    private Authentication authentication;
    @Mock
    private PinEntry pinEntry;
    @Mock
    private AuthenticateFragmentPresenter.View view;
    @Mock
    private LockUserTask lockUserTask;
    @Mock
    private DateUtil dateUtil;
    @Mock
    private UserHelper userHelper;

    private long lockedUntilTime;

    @InjectMocks
    AuthenticateFragmentPresenter presenter;

    @Before
    public void setUp() throws Exception {
        lockedUntilTime = 1533243360357L;
        when(userHelper.getLockedUntilTime()).thenReturn(lockedUntilTime);
        when(dateUtil.getCurrentTimeInMillis()).thenReturn(lockedUntilTime + 50000L);
    }

    @Test
    public void instructs_view_to_show_lockout_when_auth_disabled() {
        long currentTime = lockedUntilTime - DropbitIntents.LOCK_DURRATION;
        when(dateUtil.getCurrentTimeInMillis()).thenReturn(currentTime);
        presenter.attach(view);

        presenter.startAuth(false);

        verify(view).onWalletLock();
    }

    @Test
    public void instructs_view_to_lock_user_out_after_six_failed_attempts() {
        when(pinEntry.comparePins(anyString(), anyString())).thenReturn(PinCompare.NON_MATCH);
        when(pinEntry.hasExistingPin()).thenReturn(true);
        when(pinEntry.getSavedPin()).thenReturn("--saved-pin--");
        presenter.attach(view);

        presenter.verifyPin(new int[]{1, 2, 3, 4, 5, 6});
        presenter.verifyPin(new int[]{1, 2, 3, 4, 5, 6});
        presenter.verifyPin(new int[]{1, 2, 3, 4, 5, 6});
        presenter.verifyPin(new int[]{1, 2, 3, 4, 5, 6});
        presenter.verifyPin(new int[]{1, 2, 3, 4, 5, 6});
        presenter.verifyPin(new int[]{1, 2, 3, 4, 5, 6});

        verify(view).onWalletLock();
        verify(lockUserTask).execute();
    }

    @Test
    public void attach() throws Exception {
        presenter.attach(view);
        AuthenticateFragmentPresenter.View viewset = (AuthenticateFragmentPresenter.View) PrivateAccessor.getField(presenter, "view");

        assertEquals(view, viewset);
    }

    @Test
    public void startAuth_Already_Authenticated() throws Exception {
        presenter.attach(view);
        when(authentication.isAuthenticated()).thenReturn(true);

        presenter.startAuth(false);

        verify(view).userHasAuthenticated();
    }

    @Test
    public void startAuth_hasFingerprint() throws Exception {
        presenter.attach(view);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authentication.hasOptedIntoFingerprintAuth()).thenReturn(true);


        presenter.startAuth(false);

        verify(view).showFingerprintAuth();
    }

    @Test
    public void force_user_auth_even_if_user_has_already_auth() throws Exception {
        presenter.attach(view);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.hasOptedIntoFingerprintAuth()).thenReturn(false);
        boolean forceAuth = true;

        presenter.startAuth(forceAuth);

        verify(view, times(1)).showPinAuth();
        verify(view, times(0)).userHasAuthenticated();
    }

    @Test
    public void not_force_user_auth_even_if_user_has_already_auth() throws Exception {
        presenter.attach(view);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.hasOptedIntoFingerprintAuth()).thenReturn(false);
        boolean forceAuth = false;

        presenter.startAuth(forceAuth);

        verify(view, times(0)).showPinAuth();
        verify(view, times(1)).userHasAuthenticated();
    }

    @Test
    public void startingAuth_authenticatesWithFingerprintWhenOptedIn() {
        presenter.attach(view);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authentication.hasOptedIntoFingerprintAuth()).thenReturn(true);

        presenter.startAuth(false);

        verify(view).authenticateWithFingerprint();
    }

    @Test
    public void onSixDigits_MATCH() throws Exception {
        presenter.attach(view);

        int[] userPin = new int[]{6, 4, 0, 2, 3, 7};
        when(pinEntry.hasExistingPin()).thenReturn(true);
        when(pinEntry.comparePins(anyString(), anyString())).thenReturn(PinCompare.MATCH);


        presenter.verifyPin(userPin);
        verify(authentication).setAuthenticated();
        verify(view).userHasAuthenticated();
    }

    @Test
    public void onSixDigits_NON_MATCH() throws Exception {
        presenter.attach(view);

        int[] userPin = new int[]{6, 4, 0, 2, 3, 7};
        when(pinEntry.hasExistingPin()).thenReturn(true);
        when(pinEntry.comparePins(anyString(), anyString())).thenReturn(PinCompare.NON_MATCH);


        presenter.verifyPin(userPin);
        verify(authentication, never()).setAuthenticated();
        verify(view).onPinMismatch();
    }

    @Test
    public void setAuthenticatedWhenFingerprintAuthenticates() {
        presenter.attach(view);
        presenter.onFingerprintAuthenticated();

        verify(authentication, times(1)).setAuthenticated();
        verify(view, times(1)).userHasAuthenticated();
    }
}