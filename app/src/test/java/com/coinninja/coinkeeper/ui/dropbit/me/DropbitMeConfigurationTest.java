package com.coinninja.coinkeeper.ui.dropbit.me;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class DropbitMeConfigurationTest {
    private Uri uri = Uri.parse("https://dropbit.me");

    @Mock
    private WalletHelper walletHelper;

    private DropbitMeConfiguration dropbitMeConfiguration;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        dropbitMeConfiguration = new DropbitMeConfiguration(uri, walletHelper);
    }

    @After
    public void tearDown() {
        walletHelper = null;
        dropbitMeConfiguration = null;
        uri = null;
    }

    @Test
    public void schedules_showing_because_of_newly_verified_account() {

        dropbitMeConfiguration.setNewlyVerified();

        assertTrue(dropbitMeConfiguration.shouldShowWhenPossible());
        assertTrue(dropbitMeConfiguration.isNewlyVerified());
    }

    @Test
    public void schedules_showing_when_when_available_from_other_sources() {
        dropbitMeConfiguration.showWhenPossible();

        assertTrue(dropbitMeConfiguration.shouldShowWhenPossible());
        assertFalse(dropbitMeConfiguration.isNewlyVerified());
    }

    @Test
    public void acknowledging_state_clears_state() {
        dropbitMeConfiguration.showWhenPossible();
        dropbitMeConfiguration.acknowledge();
        assertFalse(dropbitMeConfiguration.shouldShowWhenPossible());
        assertFalse(dropbitMeConfiguration.isNewlyVerified());

        dropbitMeConfiguration.setNewlyVerified();
        dropbitMeConfiguration.acknowledge();
        assertFalse(dropbitMeConfiguration.shouldShowWhenPossible());
        assertFalse(dropbitMeConfiguration.isNewlyVerified());

    }

    @Test
    public void proxies_account_verification_state() {
        when(walletHelper.hasVerifiedAccount()).thenReturn(false).thenReturn(true);

        assertFalse(dropbitMeConfiguration.hasVerifiedAccount());
        assertTrue(dropbitMeConfiguration.hasVerifiedAccount());
        verify(walletHelper, times(2)).hasVerifiedAccount();

    }

    @Test
    public void notifies_observer_that_showing_requested() {
        OnViewDropBitMeViewRequestedObserver observer = mock(OnViewDropBitMeViewRequestedObserver.class);

        dropbitMeConfiguration.setOnViewDropBitMeViewRequestedObserver(observer);
        dropbitMeConfiguration.showWhenPossible();

        verify(observer).onShowDropBitMeRequested();
    }

    @Test(expected = Test.None.class)
    public void does_not_notify_with_no_observer() {
        dropbitMeConfiguration.showWhenPossible();
    }

    @Test
    public void provides_access_to_dropbit_uri() {
        Account account = mock(Account.class);
        List<DropbitMeIdentity> identities = new ArrayList<>();
        DropbitMeIdentity phoneIdentity = mock(DropbitMeIdentity.class);
        identities.add(phoneIdentity);
        when(walletHelper.getUserAccount()).thenReturn(account);
        when(account.getIdentities()).thenReturn(identities);
        when(phoneIdentity.getHandle()).thenReturn("--handle--");

        assertThat(dropbitMeConfiguration.getShareUrl().toString(), equalTo("https://dropbit.me/--handle--"));
    }

    @Test
    public void true_when_disabled() {
        Account account = mock(Account.class);
        when(account.getIsPrivate()).thenReturn(true).thenReturn(false);
        when(walletHelper.getUserAccount()).thenReturn(account);

        assertTrue(dropbitMeConfiguration.isDisabled());
        assertFalse(dropbitMeConfiguration.isDisabled());
    }
}