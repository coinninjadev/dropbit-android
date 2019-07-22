package com.coinninja.coinkeeper.model.helpers;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.AddressDao;
import com.coinninja.coinkeeper.model.db.BroadcastBtcInviteDao;
import com.coinninja.coinkeeper.model.db.DaoSession;
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity;
import com.coinninja.coinkeeper.model.db.FundingStat;
import com.coinninja.coinkeeper.model.db.FundingStatDao;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummaryDao;
import com.coinninja.coinkeeper.model.db.UserIdentity;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.IdentityType;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.service.client.model.InviteMetadata;
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite;
import com.coinninja.coinkeeper.service.client.model.ScriptPubKey;
import com.coinninja.coinkeeper.service.client.model.SentInvite;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.service.client.model.VIn;
import com.coinninja.coinkeeper.service.client.model.VOut;
import com.coinninja.coinkeeper.util.DateUtil;

import org.greenrobot.greendao.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeast;

@RunWith(AndroidJUnit4.class)
public class TransactionHelperTest {

    private static final String SENDER_PHONE_STRING = "13305550000";
    private static final String RECEIVER_PHONE_STRING = "13305551111";

    @Mock
    private WalletHelper walletHelper;
    @Mock
    private DaoSessionManager daoSessionManager;
    @Mock
    private DateUtil dateUtil;
    @Mock
    private TransactionInviteSummaryHelper transactionInviteSummaryHelper;

    @InjectMocks
    private TransactionHelper helper;
    @Mock
    private TransactionSummaryDao transactionSummaryDao;
    @Mock
    private InviteTransactionSummaryDao inviteDao;
    @Mock
    private QueryBuilder tsQuery;
    @Mock
    private QueryBuilder tsInviteQuery;
    @Mock
    private QueryBuilder inviteQuery;
    @Mock
    private TransactionSummary transaction;
    @Mock
    private TransactionsInvitesSummaryDao transactionInviteSummaryDao;
    @Mock
    private TransactionsInvitesSummary transactionsInvitesSummary;
    @Mock
    private UserIdentityHelper userIdentityHelper;
    @Mock
    private DropbitAccountHelper dropbitAccountHelper;

    private long currentTimeInMillsec = 654654L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(transactionInviteSummaryHelper.getOrCreateTransactionInviteSummaryFor(transaction)).thenReturn(transactionsInvitesSummary);
        when(dateUtil.getCurrentTimeInMillis()).thenReturn(currentTimeInMillsec);
        when(daoSessionManager.getTransactionSummaryDao()).thenReturn(transactionSummaryDao);
        when(daoSessionManager.getTransactionsInvitesSummaryDao()).thenReturn(transactionInviteSummaryDao);
        when(daoSessionManager.getInviteTransactionSummaryDao()).thenReturn(inviteDao);
        when(transactionSummaryDao.queryBuilder()).thenReturn(tsQuery);
        when(transactionInviteSummaryDao.queryBuilder()).thenReturn(tsInviteQuery);
        when(inviteDao.queryBuilder()).thenReturn(inviteQuery);
    }

    @Test
    public void updates_invite_summary_with_pubkey() {
        long time = 1542311567000L;
        SentInvite sentInvite = mock(SentInvite.class);
        when(sentInvite.getCreated_at()).thenReturn(time);
        when(sentInvite.getId()).thenReturn("--server-id");
        when(sentInvite.getStatus()).thenReturn("new");
        when(sentInvite.getAddressPubKey()).thenReturn("--pub-key--");
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        when(inviteQuery.where(any())).thenReturn(inviteQuery);
        when(inviteQuery.limit(1)).thenReturn(inviteQuery);
        when(inviteQuery.unique()).thenReturn(invite);

        helper.updateInviteAddressTransaction(sentInvite);

        verify(invite).setBtcState(BTCState.UNFULFILLED);
        verify(invite).setPubkey("--pub-key--");
        verify(invite).update();
    }

    @Test
    public void updates_tx_time_for_canceled_dropbits() {
        long time = 1542311567000L;
        SentInvite sentInvite = mock(SentInvite.class);
        when(sentInvite.getCreated_at()).thenReturn(time);
        when(sentInvite.getId()).thenReturn("--server-id");
        when(sentInvite.getStatus()).thenReturn("canceled");
        InviteTransactionSummary inviteTransactionSummary = mock(InviteTransactionSummary.class);
        when(inviteQuery.where(any())).thenReturn(inviteQuery);
        when(inviteQuery.limit(1)).thenReturn(inviteQuery);
        when(inviteQuery.unique()).thenReturn(inviteTransactionSummary);
        when(inviteTransactionSummary.getSentDate()).thenReturn(time);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(tsInviteQuery.where(any())).thenReturn(tsInviteQuery);
        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesSummary);

        helper.updateInviteAddressTransaction(sentInvite);

        verify(transactionsInvitesSummary).setInviteTime(0);
        verify(transactionsInvitesSummary).setBtcTxTime(time);
        verify(transactionsInvitesSummary).update();
    }

    @Test
    public void updates_tx_time_for_expired_dropbits() {
        long time = 1542311567000L;
        SentInvite sentInvite = mock(SentInvite.class);
        when(sentInvite.getCreated_at()).thenReturn(time);
        when(sentInvite.getId()).thenReturn("--server-id");
        when(sentInvite.getStatus()).thenReturn("expired");
        InviteTransactionSummary inviteTransactionSummary = mock(InviteTransactionSummary.class);
        when(inviteQuery.where(any())).thenReturn(inviteQuery);
        when(inviteQuery.limit(1)).thenReturn(inviteQuery);
        when(inviteQuery.unique()).thenReturn(inviteTransactionSummary);
        when(inviteTransactionSummary.getSentDate()).thenReturn(time);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(tsInviteQuery.where(any())).thenReturn(tsInviteQuery);
        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesSummary);

        helper.updateInviteAddressTransaction(sentInvite);

        verify(transactionsInvitesSummary).setInviteTime(0);
        verify(transactionsInvitesSummary).setBtcTxTime(time);
        verify(transactionsInvitesSummary).update();
    }

    @Test
    public void merging_invite_to_transaction_invite_time_when_txtime_null_when_available() {
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        InviteTransactionSummary inviteTransactionSummary = mock(InviteTransactionSummary.class);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        TransactionsInvitesSummaryDao joinTableDao = mock(TransactionsInvitesSummaryDao.class);
        QueryBuilder<TransactionsInvitesSummary> joinQuery = mock(QueryBuilder.class);
        when(joinTableDao.queryBuilder()).thenReturn(joinQuery);
        when(joinQuery.where(any())).thenReturn(joinQuery);
        when(joinQuery.limit(1)).thenReturn(joinQuery);
        when(joinQuery.unique()).thenReturn(transactionsInvitesSummary).thenReturn(mock(TransactionsInvitesSummary.class));
        when(daoSessionManager.getTransactionsInvitesSummaryDao()).thenReturn(joinTableDao);
        when(daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionsInvitesSummary);
        when(inviteTransactionSummary.getSentDate()).thenReturn(100000L);
        when(transactionSummary.getTxTime()).thenReturn(0L);

        helper.joinInviteToTx(inviteTransactionSummary, transactionSummary);

        verify(transactionsInvitesSummary, times(0)).setBtcTxTime(anyLong());
        verify(transactionsInvitesSummary).setInviteTime(100000L);
    }

    @Test
    public void merging_invite_to_transaction_keeps_tx_time_when_available() {
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        InviteTransactionSummary inviteTransactionSummary = mock(InviteTransactionSummary.class);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        TransactionsInvitesSummaryDao joinTableDao = mock(TransactionsInvitesSummaryDao.class);
        QueryBuilder<TransactionsInvitesSummary> joinQuery = mock(QueryBuilder.class);
        when(joinTableDao.queryBuilder()).thenReturn(joinQuery);
        when(joinQuery.where(any())).thenReturn(joinQuery);
        when(joinQuery.limit(1)).thenReturn(joinQuery);
        when(joinQuery.unique()).thenReturn(transactionsInvitesSummary).thenReturn(mock(TransactionsInvitesSummary.class));
        when(daoSessionManager.getTransactionsInvitesSummaryDao()).thenReturn(joinTableDao);
        when(daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionsInvitesSummary);
        when(inviteTransactionSummary.getSentDate()).thenReturn(100000L);
        when(transactionSummary.getTxTime()).thenReturn(200000L);

        helper.joinInviteToTx(inviteTransactionSummary, transactionSummary);

        verify(transactionsInvitesSummary).setBtcTxTime(200000L);
        verify(transactionsInvitesSummary).setInviteTime(0);
    }

    @Test
    public void uses_invite_txtime_when_tx_time_is_unavailable() {
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        when(invite.getSentDate()).thenReturn(1000000000L);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(transactionsInvitesSummary.getBtcTxTime()).thenReturn(0L);
        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);
        when(tsInviteQuery.where(any())).thenReturn(tsInviteQuery);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesSummary);


        helper.addInviteToTransInvitesSummary(invite);

        verify(transactionsInvitesSummary).setInviteTime(1000000000L);
    }

    @Test
    public void uses_transactions_txtime_when_available() {
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        when(invite.getSentDate()).thenReturn(1000000000L);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(transactionsInvitesSummary.getBtcTxTime()).thenReturn(1000000L);
        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);
        when(tsInviteQuery.where(any())).thenReturn(tsInviteQuery);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesSummary);


        helper.addInviteToTransInvitesSummary(invite);

        verify(transactionsInvitesSummary).setInviteTime(0);
    }

    @Test
    public void does_not_update_transaction_time_when_not_avaialable() {
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(transaction.getTxTime()).thenReturn(0L);
        when(transaction.getId()).thenReturn(0L);
        when(transaction.getTxid()).thenReturn("--txid--");
        when(tsInviteQuery.whereOr(any(), any())).thenReturn(tsInviteQuery);
        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesSummary);

        helper.addTransactionToTransInvitesSummary(transaction);

        verify(transactionsInvitesSummary, times(0)).setInviteTime(anyLong());
        verify(transactionsInvitesSummary, times(0)).setBtcTxTime(anyLong());
    }

    @Test
    public void updates_transaction_time_removing_invite_time_from_summary() {
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(transaction.getTxTime()).thenReturn(100000L);
        when(transaction.getId()).thenReturn(0L);
        when(transaction.getTxid()).thenReturn("--txid--");
        when(tsInviteQuery.whereOr(any(), any())).thenReturn(tsInviteQuery);
        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesSummary);

        helper.addTransactionToTransInvitesSummary(transaction);

        verify(transactionsInvitesSummary).setInviteTime(0);
        verify(transactionsInvitesSummary).setBtcTxTime(100000L);
    }

    @Test
    public void saves_block_time_on_transaction() {
        long rTime = 10000000L;
        long txTime = 20000000L;
        long blockTime = 30000000L;
        TransactionDetail td = mock(TransactionDetail.class);
        when(td.getReceivedTime()).thenReturn(rTime);
        when(td.getTime()).thenReturn(txTime);
        when(td.getBlocktime()).thenReturn(blockTime);
        TransactionSummary ts = mock(TransactionSummary.class);

        helper.saveTransaction(ts, td);

        verify(ts).setTxTime(blockTime);
    }

    @Test
    public void saves_tx_time_on_transaction_when_block_time_absent() {
        long rTime = 10000000L;
        long txTime = 20000000L;
        TransactionDetail td = mock(TransactionDetail.class);
        when(td.getReceivedTime()).thenReturn(rTime);
        when(td.getTime()).thenReturn(txTime);
        TransactionSummary ts = mock(TransactionSummary.class);

        helper.saveTransaction(ts, td);

        verify(ts).setTxTime(txTime);
    }

    @Test
    public void saves_recieve_time_on_transaction_when_tx_time_and_block_time_absent() {
        long txTime = 10000000L;
        TransactionDetail td = mock(TransactionDetail.class);
        when(td.getReceivedTime()).thenReturn(txTime);
        TransactionSummary ts = mock(TransactionSummary.class);

        helper.saveTransaction(ts, td);

        verify(ts).setTxTime(txTime);
    }

    @Test
    public void join_invite_to_transaction_test() {
        TransactionsInvitesSummary transactionsInvitesOLD = mock(TransactionsInvitesSummary.class);
        TransactionsInvitesSummary transactionsInvitesNEW = mock(TransactionsInvitesSummary.class);
        String btcTxID = "BTC TX ID";

        when(tsQuery.limit(1)).thenReturn(tsQuery);
        when(tsQuery.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(tsQuery);
        when(tsQuery.unique()).thenReturn(transactionsInvitesNEW);
        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);
        when(tsInviteQuery.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(tsInviteQuery);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesNEW);

        TransactionsInvitesSummary newTable = helper.joinInviteToTransaction(transactionsInvitesOLD, btcTxID);

        verify(transactionInviteSummaryDao).delete(transactionsInvitesOLD);
        assertThat(newTable, equalTo(transactionsInvitesNEW));
    }

    @Test
    public void join_invite_to_transaction_do_nothing_already_have_both_invite_and_tx_test() {
        TransactionsInvitesSummary transactionsInvitesOLD = mock(TransactionsInvitesSummary.class);
        TransactionsInvitesSummary transactionsInvitesNEW = mock(TransactionsInvitesSummary.class);
        String btcTxID = "BTC TX ID";

        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        TransactionSummary transaction = mock(TransactionSummary.class);
        when(transactionsInvitesOLD.getInviteTransactionSummary()).thenReturn(invite);
        when(transactionsInvitesOLD.getTransactionSummary()).thenReturn(transaction);

        when(tsQuery.limit(1)).thenReturn(tsQuery);
        when(tsQuery.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(tsQuery);
        when(tsQuery.unique()).thenReturn(transactionsInvitesNEW);
        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);
        when(tsInviteQuery.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(tsInviteQuery);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesNEW);

        TransactionsInvitesSummary newTable = helper.joinInviteToTransaction(transactionsInvitesOLD, btcTxID);

        verify(transactionInviteSummaryDao, times(0)).delete(any());
        assertThat(newTable, equalTo(transactionsInvitesOLD));
    }

    @Test
    public void join_invite_to_transaction_do_nothing_if_null_test() {
        TransactionsInvitesSummary transactionsInvitesOLD = null;
        TransactionsInvitesSummary transactionsInvitesNEW = mock(TransactionsInvitesSummary.class);
        String btcTxID = "BTC TX ID";


        when(tsQuery.limit(1)).thenReturn(tsQuery);
        when(tsQuery.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(tsQuery);
        when(tsQuery.unique()).thenReturn(transactionsInvitesNEW);
        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);
        when(tsInviteQuery.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(tsInviteQuery);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesNEW);

        TransactionsInvitesSummary newTable = helper.joinInviteToTransaction(transactionsInvitesOLD, btcTxID);

        verify(transactionInviteSummaryDao, times(0)).delete(any());
        assertThat(newTable, nullValue());
    }

    @Test
    public void join_invite_to_transactions_clean_up_all_invites_test() {
        String btcTxID = "BTC TX ID";
        List<InviteTransactionSummary> invitesWithTXID = new ArrayList<>();
        InviteTransactionSummary tempInvite = mock(InviteTransactionSummary.class);
        invitesWithTXID.add(tempInvite);
        when(tempInvite.getBtcTransactionId()).thenReturn(btcTxID);
        when(tempInvite.getId()).thenReturn(6L);

        TransactionsInvitesSummary transactionsInvitesOLD = mock(TransactionsInvitesSummary.class);

        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);
        when(tsInviteQuery.where(any())).thenReturn(tsInviteQuery);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesOLD);
        when(inviteQuery.where(any(), any())).thenReturn(inviteQuery);
        when(inviteQuery.list()).thenReturn(invitesWithTXID);


        List<InviteTransactionSummary> invites = helper.getInvitesWithTxID();
        for (InviteTransactionSummary invite : invites) {
            helper.joinInviteToTx(invite, transaction);
        }

        verify(inviteDao, times(0)).delete(any());
    }

    @Test
    public void cancelPendingSentInvites_test() {

        when(inviteQuery.where(any())).thenReturn(inviteQuery);
        ArrayList<InviteTransactionSummary> summaries = new ArrayList();
        InviteTransactionSummary summary1 = mock(InviteTransactionSummary.class);
        summaries.add(summary1);
        InviteTransactionSummary summary2 = mock(InviteTransactionSummary.class);
        summaries.add(summary2);
        InviteTransactionSummary summary3 = mock(InviteTransactionSummary.class);
        summaries.add(summary3);

        when(summary1.getType()).thenReturn(Type.SENT);
        when(summary2.getType()).thenReturn(Type.RECEIVED);
        when(summary3.getType()).thenReturn(Type.SENT);

        when(inviteQuery.list()).thenReturn(summaries);

        helper.cancelPendingSentInvites();

        verify(summary1).setBtcState(BTCState.CANCELED);
        verify(summary1).update();
        verify(summary3).setBtcState(BTCState.CANCELED);
        verify(summary3).update();
        verify(summary2, times(0)).update();
    }

    @Test
    public void set_transaction_to_ACKNOWLEDGE_if_seen_by_coin_ninja_server_test() {
        TransactionSummary sampleTransaction = mock(TransactionSummary.class);
        String sampleTransactionID = "some tx id";
        setupTransactionSummaryDao(sampleTransaction);
        TransactionDetail transactionDetail = buildMockTransactionDetails(sampleTransactionID);
        List<TransactionDetail> fetchedTransactions = new ArrayList<>();
        fetchedTransactions.add(transactionDetail);


        helper.updateTransactions(fetchedTransactions, 562372);

        verify(sampleTransaction).setMemPoolState(MemPoolState.ACKNOWLEDGE);
        verify(sampleTransaction, atLeast(1)).refresh();
        verify(sampleTransaction, atLeast(1)).update();
    }

    @Test
    public void does_not_set_blockheight_when_not_mined() {
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        String sampleTransactionID = "some tx id";
        setupTransactionSummaryDao(transactionSummary);
        TransactionDetail transactionDetail = buildMockTransactionDetails(sampleTransactionID);
        when(transactionDetail.getBlockheight()).thenReturn(0);
        List<TransactionDetail> fetchedTransactions = new ArrayList<>();
        fetchedTransactions.add(transactionDetail);

        helper.updateTransactions(fetchedTransactions, 562372);

        verify(transactionSummary, times(0)).setNumConfirmations(anyInt());
    }

    @Test
    public void sets_confirmations_when_transaction_mined() {
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        String sampleTransactionID = "some tx id";
        setupTransactionSummaryDao(transactionSummary);
        TransactionDetail transactionDetail = buildMockTransactionDetails(sampleTransactionID);
        when(transactionDetail.getBlockheight()).thenReturn(562302);
        List<TransactionDetail> fetchedTransactions = new ArrayList<>();
        fetchedTransactions.add(transactionDetail);

        helper.updateTransactions(fetchedTransactions, 562372);

        verify(transactionSummary).setNumConfirmations(71);
    }

    @Test
    public void set_target_stat_state_toACKNOWLEDGE_base_on_TransactionMemPoolState_of_ACKNOWLEDGE_test() {
        MemPoolState sampleMemPoolState = MemPoolState.ACKNOWLEDGE;
        TargetStat sampleTargetStat = mock(TargetStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VOut out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveOut(sampleTransaction, out);

        verify(sampleTargetStat).setState(TargetStat.State.ACKNOWLEDGE);
    }

    @Test
    public void set_target_stat_state_toACKNOWLEDGE_base_on_TransactionMemPoolState_of_MINED_test() {
        MemPoolState sampleMemPoolState = MemPoolState.MINED;
        TargetStat sampleTargetStat = mock(TargetStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VOut out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveOut(sampleTransaction, out);

        verify(sampleTargetStat).setState(TargetStat.State.ACKNOWLEDGE);
    }

    @Test
    public void set_target_stat_state_toPENDING_base_on_TransactionMemPoolState_of_FAILED_TO_BROADCAST_test() {
        MemPoolState sampleMemPoolState = MemPoolState.FAILED_TO_BROADCAST;
        TargetStat sampleTargetStat = mock(TargetStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VOut out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveOut(sampleTransaction, out);

        verify(sampleTargetStat).setState(TargetStat.State.CANCELED);
    }

    @Test
    public void set_target_stat_state_toPENDING_base_on_TransactionMemPoolState_of_DOUBLE_SPEND_test() {
        MemPoolState sampleMemPoolState = MemPoolState.DOUBLE_SPEND;
        TargetStat sampleTargetStat = mock(TargetStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VOut out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveOut(sampleTransaction, out);

        verify(sampleTargetStat).setState(TargetStat.State.CANCELED);
    }

    @Test
    public void set_target_stat_state_toPENDING_base_on_TransactionMemPoolState_of_ORPHANED_test() {
        MemPoolState sampleMemPoolState = MemPoolState.ORPHANED;
        TargetStat sampleTargetStat = mock(TargetStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VOut out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveOut(sampleTransaction, out);

        verify(sampleTargetStat).setState(TargetStat.State.CANCELED);
    }

    @Test
    public void set_target_stat_state_toPENDING_base_on_TransactionMemPoolState_of_PENDING_test() {
        MemPoolState sampleMemPoolState = MemPoolState.PENDING;
        TargetStat sampleTargetStat = mock(TargetStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VOut out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveOut(sampleTransaction, out);

        verify(sampleTargetStat).setState(TargetStat.State.PENDING);
    }

    @Test
    public void set_funding_stat_state_toPENDING_base_on_TransactionMemPoolState_of_PENDING_test() {
        MemPoolState sampleMemPoolState = MemPoolState.PENDING;
        FundingStat sampleTargetStat = mock(FundingStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VIn in = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveIn(sampleTransaction, in);

        verify(sampleTargetStat).setState(FundingStat.State.PENDING);
    }

    @Test
    public void set_funding_stat_state_to_ACKNOWLEDGE_base_on_TransactionMemPoolState_of_ACKNOWLEDGE_test() {
        MemPoolState sampleMemPoolState = MemPoolState.ACKNOWLEDGE;
        FundingStat sampleTargetStat = mock(FundingStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VIn in = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveIn(sampleTransaction, in);

        verify(sampleTargetStat).setState(FundingStat.State.ACKNOWLEDGE);
    }

    @Test
    public void set_funding_stat_state_to_ACKNOWLEDGE_base_on_TransactionMemPoolState_of_MINED_test() {
        MemPoolState sampleMemPoolState = MemPoolState.MINED;
        FundingStat sampleTargetStat = mock(FundingStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VIn in = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveIn(sampleTransaction, in);

        verify(sampleTargetStat).setState(FundingStat.State.ACKNOWLEDGE);
    }

    @Test
    public void set_funding_stat_state_to_CANCELED_base_on_TransactionMemPoolState_of_DOUBLE_SPEND_test() {
        MemPoolState sampleMemPoolState = MemPoolState.DOUBLE_SPEND;
        FundingStat sampleTargetStat = mock(FundingStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VIn in = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveIn(sampleTransaction, in);

        verify(sampleTargetStat).setState(FundingStat.State.CANCELED);
    }

    @Test
    public void set_funding_stat_state_to_CANCELED_base_on_TransactionMemPoolState_of_FAILED_TO_BROADCAST_test() {
        MemPoolState sampleMemPoolState = MemPoolState.FAILED_TO_BROADCAST;
        FundingStat sampleTargetStat = mock(FundingStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VIn in = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveIn(sampleTransaction, in);

        verify(sampleTargetStat).setState(FundingStat.State.CANCELED);
    }

    @Test
    public void set_funding_stat_state_to_CANCELED_base_on_TransactionMemPoolState_of_ORPHANED_test() {
        MemPoolState sampleMemPoolState = MemPoolState.ORPHANED;
        FundingStat sampleTargetStat = mock(FundingStat.class);
        TransactionSummary sampleTransaction = buildSampleTransactionSummary();
        VIn in = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat);

        helper.saveIn(sampleTransaction, in);

        verify(sampleTargetStat).setState(FundingStat.State.CANCELED);
    }

    @Test
    public void if_transaction_is_in_a_block_set_its_TransactionMemPoolState_to_MINED_test() {
        TransactionDetail detail = mock(TransactionDetail.class);
        when(detail.getBlockhash()).thenReturn("Some block hash");

        helper.saveTransaction(transaction, detail);

        verify(transaction).setMemPoolState(MemPoolState.MINED);
    }

    @Test
    public void if_transaction_is_NOT_in_a_block_empty_do_not_touch_the_TransactionMemPoolState_test() {
        TransactionDetail detail = mock(TransactionDetail.class);
        when(detail.getBlockhash()).thenReturn("");

        helper.saveTransaction(transaction, detail);

        verify(transaction, times(0)).setMemPoolState(any());
    }

    @Test
    public void if_transaction_is_NOT_in_a_block_null_do_not_touch_the_TransactionMemPoolState_test() {
        TransactionDetail detail = mock(TransactionDetail.class);
        when(detail.getBlockhash()).thenReturn(null);

        helper.saveTransaction(transaction, detail);

        verify(transaction, times(0)).setMemPoolState(any());
    }

    @Test
    public void get_incomplete_transactions_test() {
        TransactionSummary sampleTransactionSummary = mock(TransactionSummary.class);
        List<TransactionSummary> sampleList = new ArrayList<>();
        sampleList.add(sampleTransactionSummary);

        when(tsQuery.whereOr(
                TransactionSummaryDao.Properties.MemPoolState.eq(MemPoolState.INIT.getId()),
                TransactionSummaryDao.Properties.MemPoolState.eq(MemPoolState.PENDING.getId()),
                TransactionSummaryDao.Properties.MemPoolState.eq(MemPoolState.ACKNOWLEDGE.getId())
        )).thenReturn(tsQuery);

        when(tsQuery.whereOr(
                TransactionSummaryDao.Properties.MemPoolState.eq(any()),
                TransactionSummaryDao.Properties.MemPoolState.eq(any()),
                TransactionSummaryDao.Properties.MemPoolState.eq(any())
        )).thenReturn(tsQuery);

        when(tsQuery.list()).thenReturn(sampleList);

        List<TransactionSummary> transactions = helper.getIncompleteTransactions();

        assertThat(transactions.size(), equalTo(1));
        assertThat(transactions.get(0), equalTo(sampleTransactionSummary));
    }

    @Test
    public void calculate_PastedTime_test() {
        long olderThanSeconds = 300;//5 minutes = 300 seconds

        long olderThanToMillis = helper.calculatePastTimeFromNow(olderThanSeconds);

        assertThat(olderThanToMillis, equalTo(354654L));
    }

    @Test
    public void rename_txid_when_marked_as_failed() {
        String exceptedNewTxId = "failedToBroadcast" + "_" + Long.toString(654654) + "_" + "some - txid";

        TransactionSummary transaction = mock(TransactionSummary.class);
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);

        when(daoSessionManager.getTransactionSummaryDao()).thenReturn(transactionSummaryDao);
        when(daoSessionManager.getTransactionsInvitesSummaryDao()).thenReturn(transactionInviteSummaryDao);
        when(daoSessionManager.getInviteTransactionSummaryDao()).thenReturn(inviteDao);

        when(transactionSummaryDao.queryBuilder()).thenReturn(tsQuery);
        when(transactionInviteSummaryDao.queryBuilder()).thenReturn(tsInviteQuery);
        when(inviteDao.queryBuilder()).thenReturn(inviteQuery);

        when(tsQuery.where(any())).thenReturn(tsQuery);
        when(inviteQuery.where(any())).thenReturn(inviteQuery);
        when(tsInviteQuery.where(any())).thenReturn(tsInviteQuery);

        when(tsQuery.limit(1)).thenReturn(tsQuery);
        when(inviteQuery.limit(1)).thenReturn(inviteQuery);
        when(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery);

        when(tsQuery.unique()).thenReturn(transaction);
        when(inviteQuery.unique()).thenReturn(invite);
        when(tsInviteQuery.unique()).thenReturn(transactionsInvitesSummary);


        String newTxid = helper.markTransactionSummaryAsFailedToBroadcast("some - txid");

        assertThat(newTxid, equalTo("failedToBroadcast_654654_some - txid"));
        verify(transaction).setTxid(exceptedNewTxId);
        verify(invite).setBtcTransactionId(exceptedNewTxId);
        verify(transactionsInvitesSummary).setInviteTxID(exceptedNewTxId);
    }

    @Test
    public void save_local_tx_test() {
        Wallet wallet = mock(Wallet.class);
        String txID = "--some tx id--";
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        when(daoSessionManager.getTransactionSummaryDao()).thenReturn(transactionSummaryDao);
        when(transactionSummaryDao.queryBuilder()).thenReturn(tsQuery);

        when(tsQuery.where(any())).thenReturn(tsQuery);
        when(tsQuery.limit(1)).thenReturn(tsQuery);
        when(tsQuery.unique()).thenReturn(null);


        long currentTimeInMillsec = 654654;
        when(dateUtil.getCurrentTimeInMillis()).thenReturn(currentTimeInMillsec);

        helper.saveLocalTransaction(wallet, txID, invite);

        ArgumentCaptor<TransactionSummary> argumentCaptor = ArgumentCaptor.forClass(TransactionSummary.class);
        verify(transactionSummaryDao).insert(argumentCaptor.capture());
        TransactionSummary transactionSummary = argumentCaptor.getValue();


        assertThat(transactionSummary.getTxTime(), equalTo(currentTimeInMillsec));
        assertThat(transactionSummary.getTxid(), equalTo(txID));
        assertThat(transactionSummary.getMemPoolState(), equalTo(MemPoolState.INIT));
        assertThat(transactionSummary.getWallet(), equalTo(wallet));

    }

    @Test
    public void saves_received_invite() {
        long when = System.currentTimeMillis() / 1000;
        ReceivedInvite receivedInvite = new ReceivedInvite();
        receivedInvite.setId("--server-id--");
        receivedInvite.setCreated_at(when);
        receivedInvite.setUpdated_at(when);
        receivedInvite.setAddress("--address--");
        receivedInvite.setRequest_ttl("1551463318");
        receivedInvite.setSender("--sender--");
        receivedInvite.setStatus("completed");
        receivedInvite.setTxid("--txid--");
        InviteMetadata inviteMetadata = new InviteMetadata();
        InviteMetadata.MetadataContact sender = new InviteMetadata.MetadataContact("phone", SENDER_PHONE_STRING);
        InviteMetadata.MetadataContact receiver = new InviteMetadata.MetadataContact("phone", RECEIVER_PHONE_STRING);
        inviteMetadata.setSender(sender);
        inviteMetadata.setReceiver(receiver);
        inviteMetadata.setAmount(new InviteMetadata.MetadataAmount(26236L, 100L));
        receivedInvite.setMetadata(inviteMetadata);

        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        Wallet wallet = mock(Wallet.class);
        when(daoSessionManager.getInviteTransactionSummaryDao()).thenReturn(inviteDao);
        when(inviteDao.queryBuilder()).thenReturn(inviteQuery);
        when(inviteQuery.where(any())).thenReturn(inviteQuery);
        when(inviteQuery.limit(1)).thenReturn(inviteQuery);
        when(inviteQuery.unique()).thenReturn(invite);
        when(wallet.getId()).thenReturn(1L);

        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(invite.getTransactionsInvitesSummary()).thenReturn(transactionsInvitesSummary);
        when(transactionsInvitesSummary.getTransactionSummary()).thenReturn(mock(TransactionSummary.class));

        UserIdentity fromUser = mock(UserIdentity.class);
        UserIdentity toUser = mock(UserIdentity.class);
        when(userIdentityHelper.updateFrom(sender)).thenReturn(fromUser);
        when(userIdentityHelper.updateFrom(receiver)).thenReturn(toUser);

        helper.saveReceivedInviteTransaction(wallet, receivedInvite);

        verify(invite).setHistoricValue(100L);
        verify(invite).setValueSatoshis(26236L);
        verify(invite).setValueFeesSatoshis(0L);
        verify(invite).setBtcState(BTCState.FULFILLED);
        verify(invite).setType(Type.RECEIVED);
        verify(invite).setWalletId(1L);
        verify(invite).setBtcTransactionId("--txid--");
        verify(invite).setAddress("--address--");
        verify(invite).setSentDate(when * 1000);
        verify(invite).setToUser(toUser);
        verify(invite).setFromUser(fromUser);
        verify(invite).update();

        verify(transactionsInvitesSummary).setFromUser(fromUser);
        verify(transactionsInvitesSummary).setToUser(toUser);
        verify(transactionsInvitesSummary).update();

    }

    @Test
    public void do_not_update_failed_to_broadcast_transactions_test() {
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        String btcTxID = "BTC TX ID";
        String inviteServerID = "some address";

        when(daoSessionManager.getInviteTransactionSummaryDao()).thenReturn(inviteDao);
        when(inviteDao.queryBuilder()).thenReturn(inviteQuery);
        when(inviteQuery.where(any())).thenReturn(inviteQuery);
        when(inviteQuery.limit(1)).thenReturn(inviteQuery);
        when(inviteQuery.unique()).thenReturn(invite);
        TransactionsInvitesSummary transactionSummary = mock(TransactionsInvitesSummary.class);
        when(invite.getTransactionsInvitesSummary()).thenReturn(transactionSummary);
        TransactionSummary tx = mock(TransactionSummary.class);
        when(transactionSummary.getTransactionSummary()).thenReturn(tx);
        when(tx.getMemPoolState()).thenReturn(MemPoolState.FAILED_TO_BROADCAST);

        Type type = Type.RECEIVED;
        long historicUSAValue = 150;
        Long sentDate = 54981324L;
        String inviteStatus = "complete";
        Long valueSatoshis = 8795L;
        Long fee = 50L;
        String address = "some address";
        long wallet = 1L;

        helper.saveInviteTransaction(
                wallet, inviteServerID, type, mock(UserIdentity.class), mock(UserIdentity.class),
                historicUSAValue, sentDate, inviteStatus, valueSatoshis, fee, address, btcTxID);


        verify(invite, times(0)).setAddress(any());
        verify(invite, times(0)).setValueFeesSatoshis(any());
        verify(invite, times(0)).setSentDate(any());
        verify(invite, times(0)).setBtcState(any());
        verify(invite, times(0)).setValueSatoshis(any());
        verify(invite, times(0)).setValueFeesSatoshis(fee);
        verify(invite, times(0)).setWalletId(any());
        verify(invite, times(0)).setAddress(any());
        verify(invite, times(0)).setBtcTransactionId(any());
        verify(invite, times(0)).setType(any());
        verify(invite, times(0)).setToUser(any());
        verify(invite, times(0)).setFromUser(any());

    }

    @Test
    public void do_NOT_update_FULFILLED_invites_test() {
        DaoSession daoSessionMock = mock(DaoSession.class);

        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        String inviteServerID = "some address";


        when(daoSessionMock.getInviteTransactionSummaryDao()).thenReturn(inviteDao);
        when(inviteDao.queryBuilder()).thenReturn(inviteQuery);
        when(inviteQuery.where(any())).thenReturn(inviteQuery);
        when(inviteQuery.unique()).thenReturn(invite);
        TransactionsInvitesSummary transactionSummary = mock(TransactionsInvitesSummary.class);
        when(invite.getTransactionsInvitesSummary()).thenReturn(transactionSummary);
        TransactionSummary tx = mock(TransactionSummary.class);
        when(transactionSummary.getTransactionSummary()).thenReturn(tx);
        when(tx.getMemPoolState()).thenReturn(MemPoolState.FAILED_TO_BROADCAST);
        when(invite.getBtcState()).thenReturn(BTCState.FULFILLED);


        Wallet wallet = mock(Wallet.class);
        String txID = "some tx iD";
        helper.updateInviteTxIDTransaction(wallet, inviteServerID, txID);


        verify(invite, times(0)).setAddress(any());
        verify(invite, times(0)).setValueFeesSatoshis(any());
        verify(invite, times(0)).setSentDate(any());
        verify(invite, times(0)).setBtcState(any());
        verify(invite, times(0)).setValueSatoshis(any());
        verify(invite, times(0)).setValueFeesSatoshis(any());
        verify(invite, times(0)).setWalletId(any());
        verify(invite, times(0)).setAddress(any());
        verify(invite, times(0)).setBtcTransactionId(any());
        verify(invite, times(0)).setType(any());
    }

    @Test
    public void creates_partial_transaction_for_sync_to_populate_when_fulfilling_an_invite() {
        String txid = "--txid--";
        Wallet wallet = mock(Wallet.class);
        when(walletHelper.getWallet()).thenReturn(wallet);
        when(daoSessionManager.newTransactionSummary()).thenReturn(transaction);
        when(daoSessionManager.insert(transaction)).thenReturn(1L);

        TransactionSummary summary = helper.createInitialTransaction(txid);

        verify(transaction).setWallet(wallet);
        verify(transaction).setTxid(txid);
        verify(transaction).setMemPoolState(MemPoolState.PENDING);
        verify(transaction).setNumConfirmations(0);
        verify(daoSessionManager).insert(transaction);

        assertThat(summary, equalTo(transaction));
    }

    @Test
    public void creates_partial_transaction_for_sync_to_populate_when_fulfilling_an_invite_no_change() {
        String txid = "--txid--";
        Wallet wallet = mock(Wallet.class);
        when(walletHelper.getWallet()).thenReturn(wallet);
        when(daoSessionManager.newTransactionSummary()).thenReturn(transaction);
        when(daoSessionManager.insert(transaction)).thenReturn(1L);

        TransactionSummary summary = helper.createInitialTransaction(txid);

        verify(transaction).setWallet(wallet);
        verify(transaction).setTxid(txid);
        verify(transaction).setMemPoolState(MemPoolState.PENDING);
        verify(transaction).setNumConfirmations(0);
        verify(daoSessionManager).insert(transaction);

        assertThat(summary, equalTo(transaction));
    }

    @Test
    public void records_contact_name_and_number_to_give_transaction() {
        long now = System.currentTimeMillis();
        when(dateUtil.getCurrentTimeInMillis()).thenReturn(now);
        when(transactionInviteSummaryHelper.getOrCreateTransactionInviteSummaryFor(transaction)).thenReturn(transactionsInvitesSummary);
        TransactionData transactionData = mock(TransactionData.class);
        String toName = "Joe Smoe";
        Identity identity = new Identity(IdentityType.PHONE, RECEIVER_PHONE_STRING, "--hash--", toName, "", false, null);
        String txid = "--txid--";
        CompletedBroadcastDTO completedBroadcastDTO = new CompletedBroadcastDTO(transactionData, txid, identity);
        Wallet wallet = mock(Wallet.class);
        when(walletHelper.getWallet()).thenReturn(wallet);
        when(daoSessionManager.newTransactionSummary()).thenReturn(transaction);
        when(daoSessionManager.insert(transaction)).thenReturn(1L);

        DropbitMeIdentity myIdentity = mock(DropbitMeIdentity.class);
        when(dropbitAccountHelper.identityForType(identity.getIdentityType())).thenReturn(myIdentity);
        UserIdentity toUser = mock(UserIdentity.class);
        when(userIdentityHelper.updateFrom(identity)).thenReturn(toUser);
        UserIdentity fromUser = mock(UserIdentity.class);
        when(userIdentityHelper.updateFrom(myIdentity)).thenReturn(fromUser);

        TransactionSummary summary = helper.createInitialTransactionForCompletedBroadcast(completedBroadcastDTO);

        when(summary.getTransactionsInvitesSummary()).thenReturn(transactionsInvitesSummary);

        verify(transaction).setWallet(wallet);
        verify(transaction).setTxid(txid);
        verify(transaction).setMemPoolState(MemPoolState.PENDING);
        verify(transaction).setNumConfirmations(0);
        verify(daoSessionManager).insert(transaction);
        verify(transactionsInvitesSummary).update();
        verify(transactionsInvitesSummary).setToUser(toUser);
        verify(transactionsInvitesSummary).setFromUser(fromUser);

        assertThat(summary, equalTo(transaction));

        helper.createInitialTransactionForCompletedBroadcast(completedBroadcastDTO);
    }

    private VIn setupFundingStatMempoolTestSuit(TransactionSummary sampleTransaction, MemPoolState sampleMemPoolState, FundingStat sampleFundingStat) {
        String sampleAddress = "some address";
        long sampleSatoshiValue = 500l;
        int index = 7;
        VIn in = buildSampleVIn(sampleAddress, sampleSatoshiValue, index);
        setupFundingStatDao(sampleFundingStat);
        setupAddressDao(null);
        when(sampleTransaction.getMemPoolState()).thenReturn(sampleMemPoolState);

        return in;
    }

    private VOut setupTargetStatMempoolTestSuit(TransactionSummary sampleTransaction, MemPoolState sampleMemPoolState, TargetStat sampleTargetStat) {
        String sampleAddress = "some address";
        long sampleSatoshiValue = 500l;
        int index = 7;
        VOut out = buildSampleVOut(sampleAddress, sampleSatoshiValue, index);
        setupTargetStatDao(sampleTargetStat);
        setupAddressDao(null);
        when(sampleTransaction.getMemPoolState()).thenReturn(sampleMemPoolState);

        return out;
    }

    private TransactionSummary buildSampleTransactionSummary() {
        long sampleTxTime = 654654654654l;
        TransactionSummary transaction = mock(TransactionSummary.class);
        when(transaction.getTxTime()).thenReturn(sampleTxTime);
        return transaction;
    }

    private VIn buildSampleVIn(String sampleAddress, long sampleSatoshiValue, int index) {

        VIn in = mock(VIn.class);
        VOut previous = buildSampleVOut(sampleAddress, sampleSatoshiValue, index);
        when(in.getPreviousOutput()).thenReturn(previous);

        return in;
    }

    private VOut buildSampleVOut(String sampleAddress, long sampleSatoshiValue, int index) {
        VOut out = mock(VOut.class);
        ScriptPubKey pubKey = mock(ScriptPubKey.class);

        String[] addresses = new String[]{sampleAddress};
        when(pubKey.getAddresses()).thenReturn(addresses);

        when(out.getScriptPubKey()).thenReturn(pubKey);
        when(out.getValue()).thenReturn(sampleSatoshiValue);
        when(out.getIndex()).thenReturn(index);

        return out;
    }

    private void setupAddressDao(Address sampledbAddress) {
        AddressDao dao = mock(AddressDao.class);

        QueryBuilder query = mock(QueryBuilder.class);
        when(daoSessionManager.getAddressDao()).thenReturn(dao);
        when(dao.queryBuilder()).thenReturn(query);
        when(query.where(any())).thenReturn(query);
        when(query.where(any(), any())).thenReturn(query);
        when(query.where(any(), any(), any())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.unique()).thenReturn(sampledbAddress);
    }

    private void setupFundingStatDao(FundingStat sampleFundingStat) {
        FundingStatDao dao = mock(FundingStatDao.class);

        QueryBuilder query = mock(QueryBuilder.class);
        when(daoSessionManager.getFundingStatDao()).thenReturn(dao);
        when(dao.queryBuilder()).thenReturn(query);
        when(query.where(any(), any(), any(), any(), any())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.unique()).thenReturn(sampleFundingStat);
    }

    private void setupTargetStatDao(TargetStat sampleTargetStat) {
        TargetStatDao dao = mock(TargetStatDao.class);

        QueryBuilder query = mock(QueryBuilder.class);
        when(daoSessionManager.getTargetStatDao()).thenReturn(dao);
        when(dao.queryBuilder()).thenReturn(query);
        when(query.where(any())).thenReturn(query);
        when(query.where(any(), any())).thenReturn(query);
        when(query.where(any(), any(), any(), any())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.unique()).thenReturn(sampleTargetStat);
    }

    private void setupTransactionSummaryDao(TransactionSummary sampleTransaction) {
        TransactionSummaryDao dao = mock(TransactionSummaryDao.class);

        QueryBuilder query = mock(QueryBuilder.class);
        when(daoSessionManager.getTransactionSummaryDao()).thenReturn(dao);
        when(dao.queryBuilder()).thenReturn(query);
        when(query.where(any())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.unique()).thenReturn(sampleTransaction);
    }

    private TransactionDetail buildMockTransactionDetails(String txID) {
        TransactionDetail transactionDetail = mock(TransactionDetail.class);
        when(transactionDetail.getTransactionId()).thenReturn(txID);
        return transactionDetail;
    }

}