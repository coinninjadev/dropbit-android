package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WalletCreationIntentServiceTest {

    @Mock
    CNWalletManager cnWalletManager;


    @Test
    public void creates_wallet(){
        WalletCreationIntentService walletCreationIntentService = new WalletCreationIntentService();
        walletCreationIntentService.cnWalletManageer = cnWalletManager;

        String [] recoveryWords = new String[] {"some", "words"};
        when(cnWalletManager.generateRecoveryWords()).thenReturn(recoveryWords);

        walletCreationIntentService.onHandleIntent(null);

        verify(cnWalletManager).skipBackup(recoveryWords);
    }
}
