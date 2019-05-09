package com.coinninja.coinkeeper.model.helpers;


import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity;
import com.coinninja.coinkeeper.model.db.enums.IdentityType;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.model.CNUserPatch;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class DropbitAccountHelperTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private DaoSessionManager daoSessionManager;

    @InjectMocks
    private DropbitAccountHelper dropbitAccountHelper;


    @After
    public void tearDown() {
        walletHelper = null;
        dropbitAccountHelper = null;
    }

    @Test
    public void updates_user_account_from_patch() {
        Account account = mock(Account.class);
        when(walletHelper.getUserAccount()).thenReturn(account);

        dropbitAccountHelper.updateUserAccount(new CNUserPatch(true));

        verify(account).setIsPrivate(true);
        verify(account).update();
    }

    @Test(expected = Test.None.class)
    public void updates_user_account_from_patch__with_null_account() {
        when(walletHelper.getUserAccount()).thenReturn(null);

        dropbitAccountHelper.updateUserAccount(new CNUserPatch(true));
    }

    @Test
    public void updates_account_when_verified() {
        String hash = "0123456789abcdefghijklmnopqrstuvwxyz";
        PhoneNumber phoneNumber = new PhoneNumber("+13305551111");
        CNUserAccount cnUserAccount = new CNUserAccount();
        cnUserAccount.setIsPrivate(false);
        Account account = mock(Account.class);
        when(account.getPhoneNumberHash()).thenReturn(hash);
        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        when(walletHelper.getUserAccount()).thenReturn(account);
        DropbitMeIdentity dropbitMeIdentity = mock(DropbitMeIdentity.class);
        when(daoSessionManager.newDropbitMeIdentity()).thenReturn(dropbitMeIdentity);

        dropbitAccountHelper.updateVerifiedAccount(cnUserAccount);

        verify(account).setIsPrivate(false);
        verify(account).setStatus(Account.Status.VERIFIED);
        verify(account).update();

        verify(dropbitMeIdentity).setHash(hash);
        verify(dropbitMeIdentity).setIdentity(phoneNumber.toString());
        verify(dropbitMeIdentity).setType(IdentityType.PHONE);
        verify(dropbitMeIdentity).setAccount(account);
        verify(dropbitMeIdentity).setHandle("0123456789ab");
        verify(daoSessionManager).insert(dropbitMeIdentity);

    }
}

