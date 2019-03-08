package com.coinninja.coinkeeper.service.runner;

import android.content.Context;
import android.content.res.Resources;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.bitcoin.BroadcastTransactionHelper;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.UnspentTransactionHolder;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.BroadcastBtcInviteHelper;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.model.helpers.ExternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BroadcastBtcInviteRunnerTest {
    TransactionFee transactionFee = new TransactionFee(10, 20, 30);

    @Mock
    private HDWallet hdWallet;
    @Mock
    DaoSessionManager daoSessionManager;
    @Mock
    Context context;
    @Mock
    private Resources resources;
    @Mock
    private InviteTransactionSummary invite;
    @Mock
    private Analytics analytics;
    @Mock
    private PaymentHolder paymentHolder;
    @Mock
    private FundingUTXOs fundingUTXOs;
    @Mock
    private BroadcastBtcInviteHelper broadcastBtcInviteHelper;
    @Mock
    private FundingRunnable fundingRunner;
    @Mock
    private UnspentTransactionHolder unspentTransactionHolder;
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
    AccountManager accountManager;
    @Mock
    private TransactionBroadcastResult transactionBroadcastResult;
    @Mock
    private TransactionsInvitesSummary transactionsInvitesSummary;
    @Mock
    private PhoneNumberUtil phoneNumberUtil;
    @Mock
    private SyncWalletManager syncWalletManager;

    @InjectMocks
    private BroadcastBtcInviteRunner runner;

    long amountToSend = 100000L;
    long fee = 100L;
    String serverId = "--server-id--";
    String address = "--address--";
    String nameOfReceiver = "Joe Blow";


    @Before
    public void setUp() throws Exception {
        transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(context.getResources()).thenReturn(resources);
        when(invite.getTransactionsInvitesSummary()).thenReturn(transactionsInvitesSummary);
        when(invite.getServerId()).thenReturn(serverId);
        when(invite.getReceiverPhoneNumber()).thenReturn(new PhoneNumber("+12565245258"));
        when(invite.getInviteName()).thenReturn(nameOfReceiver);
        when(invite.getAddress()).thenReturn(address);
        when(invite.getValueFeesSatoshis()).thenReturn(fee);
        when(invite.getValueSatoshis()).thenReturn(amountToSend);
        when(accountManager.getNextChangeIndex()).thenReturn(3);
        USDCurrency evaluationCurrency = mock(USDCurrency.class);

        // Payment holder?
        when(paymentHolder.getTransactionFee()).thenReturn(transactionFee);
        when(paymentHolder.getEvaluationCurrency()).thenReturn(evaluationCurrency);
        when(paymentHolder.getBtcCurrency()).thenReturn(new BTCCurrency(amountToSend));
        when(transactionData.getPaymentAddress()).thenReturn(address);
        when(unspentTransactionHolder.toTransactionData()).thenReturn(transactionData);
        FundingRunnable.FundedHolder fundedHolder = new FundingRunnable.FundedHolder(unspentTransactionHolder, 30l);
        when(fundingRunner.fundRun(anyLong(), anyLong(), any())).thenReturn(fundingUTXOs);
        when(fundingRunner.evaluateFundingUTXOs(any())).thenReturn(fundedHolder);
        transactionBroadcastResult = mock(TransactionBroadcastResult.class);
        when(broadcastTransactionHelper.broadcast(any())).thenReturn(transactionBroadcastResult);
        when(transactionBroadcastResult.isSuccess()).thenReturn(true);
        runner.setInvite(invite);
    }

    @Test
    public void full_funding_run_successful_test() {
        runner.run();
        verify(fundingRunner).fundRun(invite.getValueSatoshis(), invite.getValueFeesSatoshis(), null);
        verify(fundingRunner).evaluateFundingUTXOs(fundingUTXOs);
        verify(inviteTransactionSummaryHelper).updateFulfilledInvite(transactionsInvitesSummary, transactionBroadcastResult);
        verify(transactionNotificationManager).notifyCnOfFundedInvite(invite);
        verify(syncWalletManager).syncNow();
        verify(analytics).trackEvent(Analytics.EVENT_DROPBIT_COMPLETED);
    }

    @Test
    public void broadcast_TX_To_Btc_Network_error_test() {
        TransactionBroadcastResult result = mock(TransactionBroadcastResult.class);
        when(result.isSuccess()).thenReturn(false);
        when(broadcastTransactionHelper.broadcast(transactionData)).thenReturn(result);

        runner.run();

        verify(result).getMessage();
    }

    @Test
    public void on_error_funding_notify_server_cancel_transaction_test() {
        FundingRunnable.FundedHolder fundedHolder = mock(FundingRunnable.FundedHolder.class);
        when(fundingRunner.evaluateFundingUTXOs(any())).thenReturn(fundedHolder);

        when(context.getString(anyInt(), anyString())).thenReturn("DropBit to 2565245258 has been canceled");

        when(fundedHolder.getUnspentTransactionHolder()).thenReturn(null);
        when(fundingRunner.evaluateFundingUTXOs(any())).thenReturn(fundedHolder);

        runner.run();


        verify(externalNotificationHelper).saveNotification(eq("DropBit to 2565245258 has been canceled"), eq(serverId));
        verify(walletHelper).updateBalances();
        verify(walletHelper).updateSpendableBalances();
    }

    @Test
    public void on_error_funding_notify_user_that_the_transaction_is_canceled_test() {
        String cancelMessage = "Invite to " + invite.getReceiverPhoneNumber() + " has be canceled";

        FundingRunnable.FundedHolder fundedHolder = mock(FundingRunnable.FundedHolder.class);
        when(fundingRunner.evaluateFundingUTXOs(any())).thenReturn(fundedHolder);

        when(context.getString(anyInt(), anyString())).thenReturn(cancelMessage);

        when(fundedHolder.getUnspentTransactionHolder()).thenReturn(null);
        when(fundingRunner.evaluateFundingUTXOs(any())).thenReturn(fundedHolder);

        runner.run();
        verify(transactionHelper).updateInviteAsCanceled(eq(invite.getServerId()));

    }
}