package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionConfirmationUpdateRunnerTest {

    private static final int BLOCK_HEIGHT = 52126;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private TransactionHelper transactionHelper;

    @Mock
    private TransactionSummary t1;

    @Mock
    private TransactionSummary t2;

    @InjectMocks
    private TransactionConfirmationUpdateRunner runner;

    @Before
    public void setUp() {
        List<TransactionSummary> transactions = new ArrayList<>();
        transactions.add(t1);
        transactions.add(t2);
        when(transactionHelper.getPendingMindedTransactions()).thenReturn(transactions);
        when(walletHelper.getBlockTip()).thenReturn(BLOCK_HEIGHT);

    }

    @Test
    public void skips_transactions_that_are_not_in_a_block() {
        when(t1.getBlockhash()).thenReturn("");
        when(t2.getBlockhash()).thenReturn("----hash---");
        when(t2.getBlockheight()).thenReturn(52113);
        runner.run();

        verify(t1, times(0)).update();

        verify(t2).setNumConfirmations(14);
        verify(t2).update();
    }

    @Test
    public void updates_transaction_with_counts() {
        when(t1.getBlockhash()).thenReturn("----hash---");
        when(t1.getBlockheight()).thenReturn(52123);

        when(t2.getBlockhash()).thenReturn("----hash---");
        when(t2.getBlockheight()).thenReturn(52113);

        runner.run();

        verify(t1).setNumConfirmations(4);
        verify(t1).update();

        verify(t2).setNumConfirmations(14);
        verify(t2).update();
    }

}