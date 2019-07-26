package com.coinninja.coinkeeper.model.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class TransactionSummaryTest {

    @Test
    public void is_replaceable_with_2_confirmations() {
        TransactionSummary transactionSummary = new TransactionSummary();

        transactionSummary.setNumConfirmations(0);
        assertTrue(transactionSummary.isReplaceable());

        transactionSummary.setNumConfirmations(3);
        assertFalse(transactionSummary.isReplaceable());
    }

    @Test
    public void false_when_not_in_a_block() {
        TransactionSummary transactionSummary = new TransactionSummary();
        assertFalse(transactionSummary.isInBlock());

        transactionSummary.setBlockhash("--block-hash--");

        assertTrue(transactionSummary.isInBlock());


    }
}