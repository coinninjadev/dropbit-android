package com.coinninja.coinkeeper.view.util;

import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;

import org.junit.Before;
import org.junit.Test;

import static com.coinninja.coinkeeper.view.util.TransactionUtil.FAILED_TO_BROADCAST;
import static com.coinninja.coinkeeper.view.util.TransactionUtil.IS_REPLACEABLE;
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
    public void isConfirmed_0() {
        when(mockTransaction.getNumConfirmations()).thenReturn(0);
        assertTrue(IS_REPLACEABLE(mockTransaction));
    }

    @Test
    public void isConfirmed_1() {
        when(mockTransaction.getNumConfirmations()).thenReturn(1);
        assertTrue(IS_REPLACEABLE(mockTransaction));
    }

    @Test
    public void isConfirmed_2() {
        when(mockTransaction.getNumConfirmations()).thenReturn(2);
        assertTrue(IS_REPLACEABLE(mockTransaction));
    }

    @Test
    public void isConfirmed_3() {
        when(mockTransaction.getNumConfirmations()).thenReturn(3);
        assertFalse(IS_REPLACEABLE(mockTransaction));
    }

    @Test
    public void isConfirmed_4() {
        when(mockTransaction.getNumConfirmations()).thenReturn(4);
        assertFalse(IS_REPLACEABLE(mockTransaction));
    }

    //TODO: can we only spend init or Acknowledge?
    @Test
    public void failedToBroadcast() {
        when(mockTransaction.getMemPoolState()).thenReturn(MemPoolState.FAILED_TO_BROADCAST);
        assertTrue(FAILED_TO_BROADCAST(mockTransaction));
    }

    @Test
    public void failedToBroadcast_false() {
        when(mockTransaction.getMemPoolState()).thenReturn(MemPoolState.ACKNOWLEDGE);
        assertFalse(FAILED_TO_BROADCAST(mockTransaction));
    }

}