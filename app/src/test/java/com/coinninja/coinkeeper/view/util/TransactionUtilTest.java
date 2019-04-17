package com.coinninja.coinkeeper.view.util;

import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionUtilTest {

    private TransactionSummary mockTransaction;

    @Before
    public void setUp() {
        mockTransaction = mock(TransactionSummary.class);
    }

    @Test
    public void failedToBroadcast() {
        when(mockTransaction.getMemPoolState()).thenReturn(MemPoolState.FAILED_TO_BROADCAST);
        assertTrue(TransactionUtil.failedToBroadcast(mockTransaction));
    }

    @Test
    public void failedToBroadcast_false() {
        when(mockTransaction.getMemPoolState()).thenReturn(MemPoolState.ACKNOWLEDGE);
        assertFalse(TransactionUtil.failedToBroadcast(mockTransaction));
    }

}