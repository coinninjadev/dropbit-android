package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.dropbit.DropBitCancellationManager;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NegativeBalanceRunnerTest {

    private WalletHelper mockWalletHelper;
    private NegativeBalanceRunner runner;
    private DropBitCancellationManager mockDropBitCancellationManager;

    @Before
    public void setUp() throws Exception {

        mockDropBitCancellationManager = mock(DropBitCancellationManager.class);
        mockWalletHelper = mock(WalletHelper.class);

        runner = new NegativeBalanceRunner(mockDropBitCancellationManager, mockWalletHelper);
    }


    @Test
    public void on_negative_cancel() {
        when(mockWalletHelper.buildBalances(false)).thenReturn(-847923L);

        runner.run();

        verify(mockDropBitCancellationManager).markUnfulfilledAsCanceled();
    }

    @Test
    public void not_negative_NOOP() {
        when(mockWalletHelper.buildBalances(false)).thenReturn(52L);

        runner.run();

        verify(mockDropBitCancellationManager, times(0)).markUnfulfilledAsCanceled();

    }

}