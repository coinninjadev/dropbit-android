package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.util.NotificationUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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
        walletCreationIntentService.notificationUtil = mock(NotificationUtil.class);

        String [] recoveryWords = new String[] {"some", "words"};
        when(cnWalletManager.generateRecoveryWords()).thenReturn(recoveryWords);

        walletCreationIntentService.onHandleIntent(null);

        verify(cnWalletManager).skipBackup(recoveryWords);
        verify(walletCreationIntentService.notificationUtil).dispatchInternalError(anyString(), any());
    }
}
