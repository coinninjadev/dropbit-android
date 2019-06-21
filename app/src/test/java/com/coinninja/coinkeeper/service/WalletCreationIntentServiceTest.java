package com.coinninja.coinkeeper.service;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class WalletCreationIntentServiceTest {

    @Test
    public void creates_wallet() {
        WalletCreationIntentService service = Robolectric.setupService(WalletCreationIntentService.class);

        String[] recoveryWords = new String[]{"some", "words"};
        when(service.cnWalletManager.generateRecoveryWords()).thenReturn(recoveryWords);

        service.onHandleIntent(null);

        verify(service.cnWalletManager).skipBackup(recoveryWords);
        verify(service.notificationUtil).dispatchInternalError(anyString(), any());
    }
}
