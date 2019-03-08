package com.coinninja.coinkeeper.cn.wallet.service;

import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CNServiceConnectionTest {

    private CNWalletService mockCNWalletService;
    private LocalBroadCastUtil mockLocalBroadCastUtil;
    private CNServiceConnection cnServiceConnection;

    @Before
    public void setUp() throws Exception {

        mockCNWalletService = mock(CNWalletService.class);
        mockLocalBroadCastUtil = mock(LocalBroadCastUtil.class);

        cnServiceConnection = new CNServiceConnection(mockLocalBroadCastUtil);
    }

    @Test
    public void when_service_connected_mark_self_as_bonded_test() {
        CNWalletBinder binder = mock(CNWalletBinder.class);
        when(binder.getService()).thenReturn(mockCNWalletService);

        cnServiceConnection.onServiceConnected(null, binder);

        assertTrue(cnServiceConnection.isBounded());
    }


    @Test
    public void when_service_connected_set_CNWalletService_object_test() {
        CNWalletBinder binder = mock(CNWalletBinder.class);
        when(binder.getService()).thenReturn(mockCNWalletService);

        cnServiceConnection.onServiceConnected(null, binder);
        CNWalletServicesInterface currentService = cnServiceConnection.getCNWalletServicesInterface();

        assertThat(currentService, equalTo(mockCNWalletService));
    }

    @Test
    public void when_service_connected_broadcast_local_message_test() {
        CNWalletBinder binder = mock(CNWalletBinder.class);
        when(binder.getService()).thenReturn(mockCNWalletService);

        cnServiceConnection.onServiceConnected(null, binder);

        verify(mockLocalBroadCastUtil).sendBroadcast(Intents.ACTION_ON_SERVICE_CONNECTION_BOUNDED);
    }
}