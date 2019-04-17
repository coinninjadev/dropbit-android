package com.coinninja.coinkeeper.service.runner;

import android.content.Context;
import android.content.res.Resources;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.bitcoin.BroadcastTransactionHelper;
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.BroadcastBtcInviteHelper;
import com.coinninja.coinkeeper.model.helpers.ExternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BroadcastBtcInviteRunnerTest {
    @Mock
    private Context context;
    @Mock
    private TransactionFundingManager transactionFundingManger;
    @Mock
    private Resources resources;
    @Mock
    private InviteTransactionSummary invite;
    @Mock
    private Analytics analytics;
    @Mock
    private PaymentHolder paymentHolder;
    @Mock
    private BroadcastBtcInviteHelper broadcastBtcInviteHelper;
    @Mock
    private TransactionData transactionData;
    @Mock
    private TransactionHelper transactionHelper;
    @Mock
    private ExternalNotificationHelper externalNotificationHelper;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    private BroadcastTransactionHelper broadcastTransactionHelper;
    @Mock
    private InviteTransactionSummaryHelper inviteTransactionSummaryHelper;
    @Mock
    private TransactionNotificationManager transactionNotificationManager;
    @Mock
    private TransactionBroadcastResult transactionBroadcastResult;
    @Mock
    private TransactionsInvitesSummary transactionsInvitesSummary;
    @Mock
    private SyncWalletManager syncWalletManager;
    private String serverId = "--server-id--";
    @Mock
    private PhoneNumber phoneNumber;

    @InjectMocks
    private BroadcastBtcInviteRunner runner;
    String address = "--address--";

    @After
    public void tearDown() {
        runner = null;
        address = null;
        context = null;
        resources = null;
        invite = null;
        analytics = null;
        paymentHolder = null;
        broadcastBtcInviteHelper = null;
        transactionData = null;
        transactionHelper = null;
        externalNotificationHelper = null;
        walletHelper = null;
        broadcastTransactionHelper = null;
        inviteTransactionSummaryHelper = null;
        transactionNotificationManager = null;
        transactionBroadcastResult = null;
        transactionsInvitesSummary = null;
        syncWalletManager = null;
        phoneNumber = null;
        serverId = null;
    }

    @Before
    public void setUp() throws Exception {
        transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(context.getResources()).thenReturn(resources);
        when(invite.getTransactionsInvitesSummary()).thenReturn(transactionsInvitesSummary);
        when(invite.getServerId()).thenReturn(serverId);
        when(invite.getReceiverPhoneNumber()).thenReturn(phoneNumber);
        String nameOfReceiver = "Joe Blow";
        when(invite.getInviteName()).thenReturn(nameOfReceiver);
        when(invite.getAddress()).thenReturn(address);
        long fee = 100L;
        when(invite.getValueFeesSatoshis()).thenReturn(fee);
        long amountToSend = 100000L;
        when(invite.getValueSatoshis()).thenReturn(amountToSend);
        USDCurrency evaluationCurrency = mock(USDCurrency.class);
        when(paymentHolder.getEvaluationCurrency()).thenReturn(evaluationCurrency);
        when(paymentHolder.getBtcCurrency()).thenReturn(new BTCCurrency(amountToSend));
        when(paymentHolder.getTransactionData()).thenReturn(transactionData);
        when(transactionData.getPaymentAddress()).thenReturn(address);
        transactionBroadcastResult = mock(TransactionBroadcastResult.class);
        when(broadcastTransactionHelper.broadcast(any())).thenReturn(transactionBroadcastResult);
        when(transactionBroadcastResult.isSuccess()).thenReturn(true);
        when(transactionFundingManger.buildFundedTransactionDataForDropBit(invite.getValueSatoshis(), invite.getValueFeesSatoshis())).thenReturn(transactionData);
        runner.setInvite(invite);
    }

    @Test
    public void full_funding_run_successful_test() {
        TransactionData transactionData = new TransactionData(new UnspentTransactionOutput[1],
                invite.getValueSatoshis(), invite.getValueFeesSatoshis(), 0, mock(DerivationPath.class), "");
        when(transactionFundingManger.buildFundedTransactionDataForDropBit(invite.getValueSatoshis(), invite.getValueFeesSatoshis())).thenReturn(transactionData);

        runner.run();

        assertThat(transactionData.getPaymentAddress(), equalTo(address));
        verify(broadcastTransactionHelper).broadcast(transactionData);
        verify(inviteTransactionSummaryHelper).updateFulfilledInvite(transactionsInvitesSummary, transactionBroadcastResult);
        verify(transactionNotificationManager).notifyCnOfFundedInvite(invite);
        verify(syncWalletManager).syncNow();
        verify(analytics).trackEvent(Analytics.EVENT_DROPBIT_COMPLETED);
    }

    @Test
    public void broadcast_TX_To_Btc_Network_error_test() {
        when(transactionData.getUtxos()).thenReturn(new UnspentTransactionOutput[1]);
        TransactionBroadcastResult result = mock(TransactionBroadcastResult.class);
        when(result.isSuccess()).thenReturn(false);
        when(broadcastTransactionHelper.broadcast(transactionData)).thenReturn(result);

        runner.run();

        verify(result).getMessage();
    }

    @Test
    public void on_error_funding_notify_server_cancel_transaction_test() {
        when(transactionData.getUtxos()).thenReturn(new UnspentTransactionOutput[0]);
        when(context.getString(anyInt(), anyString())).thenReturn("DropBit to 2565245258 has been canceled");

        runner.run();

        verify(externalNotificationHelper).saveNotification(eq("DropBit to 2565245258 has been canceled"), eq(serverId));
        verify(walletHelper).updateBalances();
        verify(walletHelper).updateSpendableBalances();
    }

    @Test
    public void on_error_funding_notify_user_that_the_transaction_is_canceled_test() {
        when(transactionData.getUtxos()).thenReturn(new UnspentTransactionOutput[0]);
        String cancelMessage = "Invite to " + invite.getReceiverPhoneNumber() + " has be canceled";

        when(context.getString(anyInt(), anyString())).thenReturn(cancelMessage);

        runner.run();
        verify(transactionHelper).updateInviteAsCanceled(eq(invite.getServerId()));

    }
}