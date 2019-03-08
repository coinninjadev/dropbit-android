package com.coinninja.coinkeeper.cn.wallet.service;

import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class CNWalletBinderTest {

    @Test
    public void binder_returns_correct_cn_wallet_service_object_test() {
        CNWalletServicesInterface mockCNWalletService = mock(CNWalletServicesInterface.class);
        CNWalletBinder cnWalletBinder = new CNWalletBinder(mockCNWalletService);

        assertThat(cnWalletBinder.getService(), equalTo(mockCNWalletService));
    }
}