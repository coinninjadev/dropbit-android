package com.coinninja.coinkeeper.view.adapter.util;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.FundingStat;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.util.DateFormatUtil;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.ConfirmationState;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.SendState;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(qualifiers = "en-rUS")
public class TransactionAdapterUtilTest {

    public static final String INVITE_SERVER_ID = "--invite-server-id";
    private TransactionAdapterUtil utility;
    private List<FundingStat> funders;
    private List<TargetStat> receivers;
    private USDCurrency btcValue;
    private TransactionsInvitesSummary transactionWrapper;
    private TransactionSummary transactionTx;
    private TransactionsInvitesSummary transactionsInvitesSummary;
    private DateFormatUtil dateFormatter;
    private InviteTransactionSummary invite;

    @Before
    public void setUp() {
        dateFormatter = mock(DateFormatUtil.class);
        transactionWrapper = mock(TransactionsInvitesSummary.class);
        transactionTx = mock(TransactionSummary.class);
        transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        utility = new TransactionAdapterUtil(dateFormatter, RuntimeEnvironment.application, new PhoneNumberUtil());
        receivers = new ArrayList<>();
        funders = new ArrayList<>();
        btcValue = new USDCurrency(1000.00D);
        when(dateFormatter.formatTime(1524547473000L)).thenReturn("April 24, 2018 01:24am");

        //DEFAULT TRANSACTION
        when(transactionWrapper.getTransactionSummary()).thenReturn(transactionTx);
        when(transactionTx.getTxTime()).thenReturn(1524547473000L);
        when(transactionTx.getTxid()).thenReturn("3480e31ea00efeb570472983ff914694f62804e768a6c6b4d1b6cd70a1cd3efa");
        when(transactionTx.getNumConfirmations()).thenReturn(1107);
        when(transactionTx.getFee()).thenReturn(2307L);
        when(transactionTx.getMemPoolState()).thenReturn(MemPoolState.MINED);
        when(transactionTx.getTransactionsInvitesSummary()).thenReturn(transactionsInvitesSummary);

        // DEFAULT STATS
        // ref txid: 3480e31ea00efeb570472983ff914694f62804e768a6c6b4d1b6cd70a1cd3efa
        FundingStat fundingStat = mock(FundingStat.class);
        when(fundingStat.getAddr()).thenReturn("1PyWmpjXkPftUGSbz9nrHbK4EoCPhKi6pg");
        when(fundingStat.getValue()).thenReturn(310064500L);
        Address address = mock(Address.class);
        when(fundingStat.getAddress()).thenReturn(address);
        funders.add(fundingStat);
        when(transactionTx.getFunder()).thenReturn(funders);

        TargetStat ts1 = mock(TargetStat.class);
        when(ts1.getAddr()).thenReturn("1CTgy2Xjk6S7fHvyqd9beXf5fMSM4Cm613");
        Address changeAddress = mock(Address.class);
        when(changeAddress.getChangeIndex()).thenReturn(HDWallet.INTERNAL);
        when(ts1.getAddress()).thenReturn(changeAddress);
        when(ts1.getValue()).thenReturn(309612300L);
        receivers.add(ts1);

        TargetStat ts2 = mock(TargetStat.class);
        when(ts2.getValue()).thenReturn(449893L);
        when(ts2.getAddr()).thenReturn("3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS");
        receivers.add(ts2);
        when(transactionTx.getReceiver()).thenReturn(receivers);
    }

    private void mockReceive() {
        funders.clear();
        receivers.clear();

        FundingStat fundingStat = mock(FundingStat.class);
        when(fundingStat.getAddr()).thenReturn("1PyWmpjXkPftUGSbz9nrHbK4EoCPhKi6pg");
        when(fundingStat.getValue()).thenReturn(310064500L);
        funders.add(fundingStat);
        when(transactionTx.getFunder()).thenReturn(funders);

        TargetStat ts1 = mock(TargetStat.class);
        when(ts1.getAddr()).thenReturn("1CTgy2Xjk6S7fHvyqd9beXf5fMSM4Cm613");
        when(ts1.getValue()).thenReturn(309612300L);
        receivers.add(ts1);

        TargetStat ts2 = mock(TargetStat.class);
        when(ts2.getValue()).thenReturn(449893L);
        when(ts2.getAddr()).thenReturn("3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS");
        Address address = mock(Address.class);
        when(address.getChangeIndex()).thenReturn(HDWallet.EXTERNAL);
        when(ts2.getAddress()).thenReturn(address);
        receivers.add(ts2);
        when(transactionTx.getReceiver()).thenReturn(receivers);

    }

    private void mockSendToSelfAsSend() {
        mockSendToSelfAsReceive();

    }

    private void mockSendToSelfAsReceive() {
        funders.clear();
        receivers.clear();

        FundingStat fundingStat = mock(FundingStat.class);
        when(fundingStat.getAddr()).thenReturn("1PyWmpjXkPftUGSbz9nrHbK4EoCPhKi6pg");
        when(fundingStat.getValue()).thenReturn(310064500L);
        when(fundingStat.getAddress()).thenReturn(mock(Address.class));
        funders.add(fundingStat);
        when(transactionTx.getFunder()).thenReturn(funders);

        TargetStat ts1 = mock(TargetStat.class);
        when(ts1.getAddr()).thenReturn("1CTgy2Xjk6S7fHvyqd9beXf5fMSM4Cm613");
        when(ts1.getValue()).thenReturn(310062193L);
        when(ts1.getAddress()).thenReturn(mock(Address.class));
        receivers.add(ts1);
    }

    private void mockSentInvite() {
        invite = mock(InviteTransactionSummary.class);
        when(transactionWrapper.getTransactionSummary()).thenReturn(null);
        when(transactionWrapper.getInviteTransactionSummary()).thenReturn(invite);
        when(invite.getBtcState()).thenReturn(BTCState.UNFULFILLED);
        when(invite.getType()).thenReturn(Type.SENT);
        PhoneNumberUtil util = new PhoneNumberUtil();
        PhoneNumber senderPhoneNumber = new PhoneNumber("+13305550000");
        PhoneNumber receiverPhoneNumber = new PhoneNumber("+13305551111");
        when(invite.getSenderPhoneNumber()).thenReturn(senderPhoneNumber);
        when(invite.getReceiverPhoneNumber()).thenReturn(receiverPhoneNumber);
        when(invite.getInviteName()).thenReturn("Joe Blow");
        when(invite.getSentDate()).thenReturn(1524547473000L);
        when(invite.getValueSatoshis()).thenReturn(100000L);
        when(invite.getValueFeesSatoshis()).thenReturn(10L);
        when(invite.getServerId()).thenReturn(INVITE_SERVER_ID);
    }

    private void mockReceiveInvite() {
        mockSentInvite();
        when(invite.getType()).thenReturn(Type.RECEIVED);
    }

    // Invites
    @Test
    public void empty_target_address_when_invite_tx_not_fully_propagated() {
        //Send
        mockSentInvite();
        when(transactionWrapper.getTransactionSummary()).thenReturn(transactionTx);
        receivers.clear();
        funders.clear();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);
        assertThat(bindableTransaction.getTargetAddress(), equalTo(""));
        assertThat(bindableTransaction.getFundingAddress(), equalTo(""));

        //Receive
        mockReceiveInvite();
        when(transactionWrapper.getTransactionSummary()).thenReturn(transactionTx);
        receivers.clear();
        funders.clear();

        bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTargetAddress(), equalTo(""));
        assertThat(bindableTransaction.getFundingAddress(), equalTo(""));
    }

    @Test
    public void sets_invite_server_id_null() {
        mockSendToSelfAsReceive();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getServerInviteId(), equalTo(""));
    }

    @Test
    public void setsContactNumber__invite__send() {
        mockSentInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getContactPhoneNumber(), equalTo("(330) 555-1111"));
    }


    @Test
    public void setsContactNumber__invite__receive() {
        mockReceiveInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getContactPhoneNumber(), equalTo("(330) 555-0000"));
    }

    @Test
    public void sets_invite_server_id() {
        mockSentInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getServerInviteId(), equalTo(INVITE_SERVER_ID));
    }

    @Test
    public void sets_type_of_invite() {
        mockSentInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getSendState(), equalTo(SendState.SEND));
    }

    @Test
    public void provides_usd_value_at_time_of_transaction() {
        mockSentInvite();
        when(transactionWrapper.getTransactionSummary()).thenReturn(transactionTx);
        when(transactionTx.getHistoricPrice()).thenReturn(10000L);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getHistoricalTransactionUSDValue(), equalTo(10000L));
    }

    @Test
    public void sets_name_of_invite() {
        mockSentInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getContactName(), equalTo("Joe Blow"));
    }

    @Test
    public void sets_fee_for_invite() {
        mockSentInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getFee(), equalTo(10L));
    }

    @Test
    public void sets_receive_value_of_invite() {
        mockReceiveInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getValue(), equalTo(100000L));
    }

    @Test
    public void sets_transaction_id_when_completed() {
        mockSentInvite();
        when(transactionWrapper.getTransactionSummary()).thenReturn(transactionTx);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTxID(), equalTo("3480e31ea00efeb570472983ff914694f62804e768a6c6b4d1b6cd70a1cd3efa"));

    }

    @Test
    public void sets_sent_value() {
        mockSentInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getValue(), equalTo(100000L));
    }

    @Test
    public void sent_date_through_invite() {
        mockSentInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTxTime(), equalTo("April 24, 2018 01:24am"));
    }

    @Test
    public void does_not_set_invite_state_if_null_invite() {
        invite = null;

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertNull(bindableTransaction.getInviteState());
    }

    @Test
    public void receive_sets_invite_state_to_address_provided_when_address_exists_with_invite() {
        mockReceiveInvite();
        when(invite.getAddress()).thenReturn("--- address ---");

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getInviteState(), equalTo(BindableTransaction.InviteState.RECEIVED_ADDRESS_PROVIDED));
    }

    @Test
    public void send_sets_invite_state_to_address_provided_when_address_exists_with_invite() {
        mockSentInvite();
        when(invite.getAddress()).thenReturn("--- address ---");

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getInviteState(), equalTo(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED));
    }

    @Test
    public void updates_invite_state_to_canceled_for_canceled_invites() {
        mockSentInvite();
        when(invite.getBtcState()).thenReturn(BTCState.CANCELED);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getInviteState(), equalTo(BindableTransaction.InviteState.CANCELED));
        assertThat(bindableTransaction.getSendState(), equalTo(SendState.SEND_CANCELED));
    }

    @Test
    public void updates_invite_state_to_expired_for_expired_invtes() {
        mockSentInvite();
        when(invite.getBtcState()).thenReturn(BTCState.EXPIRED);
        when(invite.getType()).thenReturn(Type.RECEIVED);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getInviteState(), equalTo(BindableTransaction.InviteState.EXPIRED));
        assertThat(bindableTransaction.getSendState(), equalTo(SendState.RECEIVE_CANCELED));
    }

    @Test
    public void updates_sent_invite_state_to_sent_pending() {
        mockSentInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getInviteState(), equalTo(BindableTransaction.InviteState.SENT_PENDING));
    }

    @Test
    public void updates_received_invite_state_to_receive_pending() {
        mockReceiveInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getInviteState(), equalTo(BindableTransaction.InviteState.RECEIVED_PENDING));
    }

    @Test
    public void updates_canceled_received_invite_to_canceled() {
        mockReceiveInvite();
        when(invite.getBtcState()).thenReturn(BTCState.CANCELED);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getInviteState(), equalTo(BindableTransaction.InviteState.CANCELED));
        assertThat(bindableTransaction.getSendState(), equalTo(SendState.RECEIVE_CANCELED));
    }

    @Test
    public void updates_expired_received_invite_to_expired() {
        mockReceiveInvite();
        when(invite.getBtcState()).thenReturn(BTCState.EXPIRED);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getInviteState(), equalTo(BindableTransaction.InviteState.EXPIRED));
        assertThat(bindableTransaction.getSendState(), equalTo(SendState.RECEIVE_CANCELED));
    }

    @Test
    public void both_transaction_record_and_invite_calculates_confirmations() {
        mockSentInvite();
        when(transactionWrapper.getTransactionSummary()).thenReturn(transactionTx);
        when(transactionTx.getNumConfirmations()).thenReturn(1);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getConfirmationState(),
                equalTo(ConfirmationState.CONFIRMED));
        assertThat(bindableTransaction.getInviteState(),
                equalTo(BindableTransaction.InviteState.CONFIRMED));
    }

    // Transactions w/ contacts

    @Test
    public void setsContactNumber() {
        PhoneNumber phoneNumber = new PhoneNumber("+13305551111");
        when(transactionTx.getTransactionsInvitesSummary().getToPhoneNumber()).thenReturn(phoneNumber);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getContactPhoneNumber(), equalTo("(330) 555-1111"));
    }

    @Test
    public void setsContactName() {
        String name = "Joe Blow";
        when(transactionTx.getTransactionsInvitesSummary().getToName()).thenReturn(name);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getContactName(), equalTo(name));
    }

    // USE CASE -- SEND TO SELF
    @Test
    public void sets_send_to_self_state_send() {
        mockSendToSelfAsSend();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getSendState(), equalTo(SendState.TRANSFER));
    }

    @Test
    public void sets_send_to_self_state_receive() {
        mockSendToSelfAsReceive();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getSendState(), equalTo(SendState.TRANSFER));
    }

    @Test
    public void total_transaction_value_is_zero() {
        mockSendToSelfAsReceive();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getValue(), equalTo(0L));
    }

    @Test
    public void sets_senders_address_for_receive_from_self() {
        mockSendToSelfAsReceive();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getFundingAddress(), equalTo("1PyWmpjXkPftUGSbz9nrHbK4EoCPhKi6pg"));
    }

    @Test
    public void sets_target_address_for_receive_from_self() {
        mockSendToSelfAsReceive();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTargetAddress(), equalTo("1CTgy2Xjk6S7fHvyqd9beXf5fMSM4Cm613"));
    }

    // USE CASE -- RECEIVE
    @Test
    public void sets_receive_state() {
        mockReceive();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getSendState(), equalTo(SendState.RECEIVE));
    }

    @Test
    public void calulates_total_value_receivd() {
        mockReceive();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getValue(), equalTo(449893L));
    }

    @Test
    public void sets_senders_address_for_receive() {
        mockReceive();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getFundingAddress(), equalTo("1PyWmpjXkPftUGSbz9nrHbK4EoCPhKi6pg"));
    }

    @Test
    public void sets_target_address_for_receive() {
        mockReceive();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTargetAddress(), equalTo("3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS"));
    }

    // USE CASE -- SEND

    @Test
    public void sets_number_of_confirmations() {
        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getConfirmationCount(), equalTo(1107));
    }

    @Test
    public void sets_send_state() {
        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getSendState(), equalTo(SendState.SEND));
    }

    @Test
    public void calculates_total_value_sent() {
        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getValue(), equalTo(449893L));
    }

    @Test
    public void sets_senders_address_for_send() {
        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getFundingAddress(), equalTo("1PyWmpjXkPftUGSbz9nrHbK4EoCPhKi6pg"));
    }

    @Test
    public void sets_target_address_for_send() {
        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTargetAddress(), equalTo("3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS"));
    }

    // USE CASE -- GENERIC
    @Test
    public void send_sets_transaction_state_to_failed_to_broadcast() {
        when(transactionTx.getMemPoolState()).thenReturn(MemPoolState.FAILED_TO_BROADCAST);
        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getSendState(), equalTo(SendState.FAILED_TO_BROADCAST_SEND));
    }

    @Test
    public void receive_sets_transaction_state_to_failed_to_broadcast() {
        mockReceive();
        when(transactionTx.getMemPoolState()).thenReturn(MemPoolState.FAILED_TO_BROADCAST);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getSendState(), equalTo(SendState.FAILED_TO_BROADCAST_RECEIVE));
    }

    @Test
    public void sets_transaction_id() {
        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTxID(), equalTo("3480e31ea00efeb570472983ff914694f62804e768a6c6b4d1b6cd70a1cd3efa"));
    }

    @Test
    public void determines_the_total_amount_of_the_transaction() {
        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTotalTransactionCost(), equalTo(452200L));
    }

    @Test
    public void sets_fee_value() {
        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getFee(), equalTo(2307L));
        assertThat(bindableTransaction.getFeeCurrency().toLong(), equalTo(2307L));
    }

    // DATE -- Transaction Time
    @Test
    public void converts_transaction_time() {
        when(dateFormatter.formatTime(1524547473000L)).thenReturn("April 24, 2018 01:24am");

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTxTime(), equalTo("April 24, 2018 01:24am"));
    }

    @Test
    public void converts_ZERO_transaction_time_is_empty() {
        when(transactionTx.getTxTime()).thenReturn(0L);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTxTime(), equalTo(""));
    }

    // # Confirmations
    @Test
    public void binds_number_of_confirmations___4_or_more_is_confirmed() {
        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getConfirmationState(), equalTo(ConfirmationState.CONFIRMED));
    }

    @Test
    public void binds_number_of_confirmations___2_or_more_is_3_confirmations() {
        when(transactionTx.getNumConfirmations()).thenReturn(2);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getConfirmationState(), equalTo(ConfirmationState.CONFIRMED));
    }

    @Test
    public void binds_number_of_confirmations___1_or_more_is_1_confirmation() {
        when(transactionTx.getNumConfirmations()).thenReturn(1);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getConfirmationState(), equalTo(ConfirmationState.CONFIRMED));
    }

    @Test
    public void binds_number_of_confirmations___0_or_more_is_unconfirmed() {
        when(transactionTx.getNumConfirmations()).thenReturn(0);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getConfirmationState(), equalTo(ConfirmationState.UNCONFIRMED));
    }

    @Test
    public void render_a_TransactionsInvitesSummary_that_has_a_valid_InviteSummary_but_a_INIT_TransactionSummary() {
        when(transactionTx.getTxid()).thenReturn("---txid");
        when(transactionTx.getMemPoolState()).thenReturn(MemPoolState.INIT);
        when(transactionWrapper.getTransactionSummary()).thenReturn(transactionTx);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getFundingAddress(), equalTo(""));
    }

    @Test
    public void render_a_TransactionsInvitesSummary_that_has_a_valid_InviteSummary_but_a_FAILED_TO_BROADCAST_TransactionSummary() {
        when(transactionTx.getTxid()).thenReturn("---txid");
        when(transactionTx.getTransactionsInvitesSummary()).thenReturn(transactionWrapper);
        when(transactionTx.getMemPoolState()).thenReturn(MemPoolState.FAILED_TO_BROADCAST);
        when(transactionWrapper.getTransactionSummary()).thenReturn(transactionTx);
        when(transactionTx.getFunder()).thenReturn(new ArrayList());

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getFundingAddress(), equalTo(""));
    }

    @Test
    public void binds_a_ACKNOWLEDGE_normal_regular_btc_transaction_NotADropBit_test() {
        long time = 1524547473000L;
        long fee = 400L;
        TransactionSummary tx = buildMockTx("some tx id", time, fee, MemPoolState.ACKNOWLEDGE);
        InviteTransactionSummary invite = null;
        when(transactionWrapper.getTransactionSummary()).thenReturn(tx);
        when(transactionWrapper.getInviteTransactionSummary()).thenReturn(invite);


        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getSendState(), equalTo(SendState.SEND));
        assertThat(bindableTransaction.getFee(), equalTo(400L));
        assertThat(bindableTransaction.getValue(), equalTo(449893L));
        assertThat(bindableTransaction.getTotalTransactionCost(), equalTo(450293L));
        assertThat(bindableTransaction.getTxTime(), equalTo("April 24, 2018 01:24am"));
        assertThat(bindableTransaction.getTargetAddress(), equalTo("3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS"));
    }

    @Test
    public void binds_a_failed_to_broadcast_normal_regular_btc_transaction_NotADropBit_test() {
        when(transactionTx.getMemPoolState()).thenReturn(MemPoolState.FAILED_TO_BROADCAST);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getSendState(), equalTo(SendState.FAILED_TO_BROADCAST_SEND));
        assertThat(bindableTransaction.getTargetAddress(), equalTo("1CTgy2Xjk6S7fHvyqd9beXf5fMSM4Cm613"));
        assertThat(bindableTransaction.getFee(), equalTo(2307L));
        assertThat(bindableTransaction.getValue(), equalTo(0L));
        assertThat(bindableTransaction.getTotalTransactionCostCurrency().toLong(), equalTo(2307L));
        assertThat(bindableTransaction.getTxTime(), equalTo("April 24, 2018 01:24am"));
    }

    @Test
    public void binds_a_failed_to_broadcast_transfer_normal_regular_btc_transaction_NotADropBit_test() {
        mockSendToSelfAsSend();
        when(transactionTx.getMemPoolState()).thenReturn(MemPoolState.FAILED_TO_BROADCAST);


        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTargetAddress(), equalTo("1CTgy2Xjk6S7fHvyqd9beXf5fMSM4Cm613"));
        assertThat(bindableTransaction.getSendState(), equalTo(SendState.FAILED_TO_BROADCAST_TRANSFER));
        assertThat(bindableTransaction.getFee(), equalTo(2307L));
        assertThat(bindableTransaction.getValue(), equalTo(0L));
        assertThat(bindableTransaction.getTxTime(), equalTo("April 24, 2018 01:24am"));
    }

    @Test
    public void binds_a_failed_to_broadcast_transfer_from_a_received_invite_test() {
        long time = 1524547473000L;
        long fee = 400L;
        mockReceiveInvite();
        TransactionSummary tx = buildMockTx("some tx id", time, fee, MemPoolState.FAILED_TO_BROADCAST);
        when(tx.getFunder().get(0).getAddress()).thenReturn(null);
        //when(receivers.get(1).getValue()).thenReturn(0L);
        when(transactionWrapper.getTransactionSummary()).thenReturn(tx);
        when(transactionWrapper.getInviteTransactionSummary()).thenReturn(invite);
        when(invite.getBtcState()).thenReturn(BTCState.FULFILLED);


        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTargetAddress(), equalTo("1CTgy2Xjk6S7fHvyqd9beXf5fMSM4Cm613"));
        assertThat(bindableTransaction.getSendState(), equalTo(SendState.FAILED_TO_BROADCAST_RECEIVE));
        assertThat(bindableTransaction.getFee(), equalTo(400L));
        assertThat(bindableTransaction.getValue(), equalTo(100000L));
        assertThat(bindableTransaction.getTxTime(), equalTo("April 24, 2018 01:24am"));
    }

    @Test
    public void supplies_memo_from_invite() {
        mockSentInvite();
        String memo_from_invite = "memo from invite";
        TransactionNotification transactionNotification = mock(TransactionNotification.class);
        when(invite.getTransactionNotification()).thenReturn(transactionNotification);
        when(transactionNotification.getMemo()).thenReturn(memo_from_invite);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getMemo(), equalTo(memo_from_invite));
        assertThat(bindableTransaction.getIsSharedMemo(), equalTo(false));
    }

    @Test
    public void supplies_memo_from_transaction() {
        String memo_from_send = "memo from send";
        TransactionNotification transactionNotification = mock(TransactionNotification.class);
        when(transactionTx.getTransactionNotification()).thenReturn(transactionNotification);
        when(transactionNotification.getMemo()).thenReturn(memo_from_send);
        when(transactionNotification.getIsShared()).thenReturn(true);

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getMemo(), equalTo(memo_from_send));
        assertThat(bindableTransaction.getIsSharedMemo(), equalTo(true));
    }

    @Test
    public void sets_invite_id_on_bindable_transaction() {
        mockReceiveInvite();

        BindableTransaction bindableTransaction = utility.translateTransaction(transactionWrapper);

        assertThat(bindableTransaction.getTxTime(), equalTo("April 24, 2018 01:24am"));
    }


    private TransactionSummary buildMockTx(String some_tx_id, long time, long fee, MemPoolState state) {
        TransactionSummary tx = mock(TransactionSummary.class);
        TransactionsInvitesSummary summary = mock(TransactionsInvitesSummary.class);

        when(tx.getTxid()).thenReturn(some_tx_id);
        when(tx.getTxTime()).thenReturn(time);
        when(tx.getTransactionsInvitesSummary()).thenReturn(summary);
        when(tx.getTransactionsInvitesSummary().getToPhoneNumber()).thenReturn(new PhoneNumber());
        when(tx.getTransactionsInvitesSummary().getToName()).thenReturn("Carl Simmens");
        when(tx.getFunder()).thenReturn(funders);
        when(tx.getReceiver()).thenReturn(receivers);
        when(tx.getNumOutputs()).thenReturn(receivers.size());
        when(tx.getNumInputs()).thenReturn(funders.size());
        when(tx.getBlockhash()).thenReturn(null);
        when(tx.getMemPoolState()).thenReturn(state);
        when(tx.getFee()).thenReturn(fee);

        return tx;
    }

}