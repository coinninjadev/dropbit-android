package com.coinninja.coinkeeper.presenter.activity;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.service.runner.BroadcastTransactionRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import junitx.util.PrivateAccessor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BroadcastTransactionPresenterTest {


    @Mock
    private BroadcastTransactionRunner broadcastTransactionRunner;
    @Mock
    private BroadcastTransactionPresenter.View view;

    @InjectMocks
    private BroadcastTransactionPresenter broadcastTransactionPresenter;

    @Before
    public void setUp() throws Exception {
        broadcastTransactionPresenter = new BroadcastTransactionPresenter(broadcastTransactionRunner);
        broadcastTransactionPresenter.attachView(view);
        when(broadcastTransactionRunner.clone()).thenReturn(broadcastTransactionRunner);
    }

    @Test
    public void broadcastTransaction() {
        TransactionData transactionData = mock(TransactionData.class);
        InOrder inOrder = inOrder(broadcastTransactionRunner);

        broadcastTransactionPresenter.broadcastTransaction(transactionData);

        inOrder.verify(broadcastTransactionRunner).setBroadcastListener(broadcastTransactionPresenter);
        inOrder.verify(broadcastTransactionRunner).clone();
        inOrder.verify(broadcastTransactionRunner).execute(transactionData);
    }


    @Test
    public void onBroadcastProgress() {
        int expectedProgress = 80;

        broadcastTransactionPresenter.onBroadcastProgress(expectedProgress);

        verify(view).showProgress(expectedProgress);
    }

    @Test
    public void onBroadcastSuccessful() {
        TransactionBroadcastResult mockResult = mock(TransactionBroadcastResult.class);

        broadcastTransactionPresenter.onBroadcastSuccessful(mockResult);

        verify(view).showBroadcastSuccessful(mockResult);
    }


    @Test
    public void onBroadcastError() {
        TransactionBroadcastResult mockResult = mock(TransactionBroadcastResult.class);

        broadcastTransactionPresenter.onBroadcastError(mockResult);

        verify(view).showBroadcastFail(mockResult);
    }

    @Test
    public void attachView() throws NoSuchFieldException {
        BroadcastTransactionPresenter.View expectedView = view;

        broadcastTransactionPresenter.attachView(view);
        BroadcastTransactionPresenter.View view = (BroadcastTransactionPresenter.View) PrivateAccessor.getField(broadcastTransactionPresenter, "view");

        assertThat(view, equalTo(expectedView));
    }
}