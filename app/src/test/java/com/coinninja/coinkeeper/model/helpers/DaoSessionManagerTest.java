package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.DaoMaster;
import com.coinninja.coinkeeper.model.db.DaoSession;
import com.coinninja.coinkeeper.model.db.User;
import com.coinninja.coinkeeper.model.db.UserDao;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.WalletDao;

import org.greenrobot.greendao.database.Database;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DaoSessionManagerTest {
    @Mock
    Database database;

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
        when(daoSession.getDatabase()).thenReturn(database);
        daoSessionManager.connect();
    }

    @Test
    public void creates_wallet_for_user() {
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        UserDao userDao = mock(UserDao.class);
        when(daoSession.getUserDao()).thenReturn(userDao);
        when(daoSession.getWalletDao()).thenReturn(walletDao);
        when(userDao.insert(any())).thenReturn(1L);

        daoSessionManager.createWallet();

        verify(walletDao).insert(walletCaptor.capture());
        Wallet wallet = walletCaptor.getValue();
        assertThat(wallet.getUserId(), equalTo(1L));
    }

}