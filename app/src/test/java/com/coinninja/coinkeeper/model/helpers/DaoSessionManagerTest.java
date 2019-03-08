package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.DaoMaster;
import com.coinninja.coinkeeper.model.db.DaoSession;
import com.coinninja.coinkeeper.model.db.User;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.WalletDao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DaoSessionManagerTest {
    @Mock
    DaoMaster daoMaster;

    @Mock
    DaoSession daoSession;

    @Mock
    WalletDao walletDao;

    private DaoSessionManager daoSessionManager;

    @Before
    public void setUp() {
        daoSessionManager = new DaoSessionManager(daoMaster);
        when(daoMaster.newSession()).thenReturn(daoSession);
        daoSessionManager.connect();
    }

    @Test
    public void creates_wallet_for_user() {
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        when(daoSession.getWalletDao()).thenReturn(walletDao);

        User user = new User();
        user.setId(1L);
        daoSessionManager.createWallet(user);

        verify(walletDao).insert(walletCaptor.capture());
        Wallet wallet = walletCaptor.getValue();
        assertThat(wallet.getUserId(), equalTo(1L));
    }

}