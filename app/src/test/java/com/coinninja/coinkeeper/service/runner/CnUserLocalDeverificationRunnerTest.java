package com.coinninja.coinkeeper.service.runner;

import android.content.Intent;

import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class CnUserLocalDeverificationRunnerTest {

    private WalletHelper walletHelper;
    private CnUserLocalDeverificationRunner localDeverificationRunner;
    private LocalBroadCastUtil localBroadCastUtil;

    @Before
    public void setUp() throws Exception {
        walletHelper = mock(WalletHelper.class);
        localBroadCastUtil = mock(LocalBroadCastUtil.class);

        localDeverificationRunner = new CnUserLocalDeverificationRunner(localBroadCastUtil, walletHelper);
    }

    @Test
    public void when_run_remove_account_data_from_dao_test() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);

        localDeverificationRunner.run();

        verify(walletHelper).removeCurrentCnRegistration();
        verify(localBroadCastUtil).sendBroadcast(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getAction(), equalTo(DropbitIntents.ACTION_CN_USER_ACCOUNT_UPDATED));
    }
}