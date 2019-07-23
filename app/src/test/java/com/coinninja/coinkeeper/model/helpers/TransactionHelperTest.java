package com.coinninja.coinkeeper.model.helpers;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.AddressDao;
import com.coinninja.coinkeeper.model.db.BroadcastBtcInviteDao;
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
import com.coinninja.coinkeeper.model.db.enums.IdentityType;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.model.query.TransactionQueryManager;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;
import com.coinninja.coinkeeper.service.client.model.ScriptPubKey;
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
import org.mockito.internal.verification.VerificationModeFactory;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
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
    private TransactionQueryManager transactionQueryManager;
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
        when(walletHelper.getWallet()).thenReturn(mock(Wallet.class));
    }

    @Test
    public void when_a_new_transaction_is_initialized_set_its_mempool_state_to_pending() {
        ArgumentCaptor<TransactionSummary> argumentCaptor = ArgumentCaptor.forClass(TransactionSummary.class);
        List<GsonAddress> addresses = new ArrayList<>();
        addresses.add(mock(GsonAddress.class));

        TransactionSummaryDao dao = setupTransactionSummaryDao(null);

        helper.initTransactions(addresses);
        verify(dao).insert(argumentCaptor.capture());


        TransactionSummary summary = argumentCaptor.getValue();
        assertNotNull(summary);
        assertThat(summary.getMemPoolState(), equalTo(MemPoolState.PENDING));
    }

    @Test
    public void do_not_create_transaction_when_it_already_exits() {
        TransactionSummary transaction = mock(TransactionSummary.class);
        List<GsonAddress> addresses = new ArrayList<>();
        addresses.add(mock(GsonAddress.class));
        TransactionSummaryDao dao = setupTransactionSummaryDao(transaction);

        helper.initTransactions(addresses);

        verify(dao, VerificationModeFactory.times(0)).insert(transaction);
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
        long rTime = 1;
        long txTime = 2;
        long blockTime = 3;
        TransactionDetail td = mock(TransactionDetail.class);
        when(td.getReceivedTimeMillis()).thenReturn(rTime);
        when(td.getTimeMillis()).thenReturn(txTime);
        when(td.getBlocktimeMillis()).thenReturn(blockTime);
        TransactionSummary ts = mock(TransactionSummary.class);

        helper.saveTransaction(ts, td);

        verify(ts).setTxTime(blockTime);
    }

    @Test
    public void saves_tx_time_on_transaction_when_block_time_absent() {
        long rTime = 1;
        long txTime = 2;
        TransactionDetail td = mock(TransactionDetail.class);
        when(td.getReceivedTimeMillis()).thenReturn(rTime);
        when(td.getTimeMillis()).thenReturn(txTime);
        TransactionSummary ts = mock(TransactionSummary.class);

        helper.saveTransaction(ts, td);

        verify(ts).setTxTime(txTime);
    }

    @Test
    public void saves_recieve_time_on_transaction_when_tx_time_and_block_time_absent() {
        long txTime = 1L;
        TransactionDetail td = mock(TransactionDetail.class);
        when(td.getReceivedTimeMillis()).thenReturn(txTime);
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
        when(detail.isInBlock()).thenCallRealMethod();

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
        when(transactionQueryManager.getIncompleteTransactions()).thenReturn(sampleList);

        List<TransactionSummary> transactions = helper.getIncompleteTransactions();

        assertThat(transactions, equalTo(sampleList));
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
    public void records_contact_name_and_number_to_give_transaction() {
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
        verify(transaction).setTxTime(currentTimeInMillsec);
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

    private TransactionSummaryDao setupTransactionSummaryDao(TransactionSummary sampleTransaction) {
        TransactionSummaryDao dao = mock(TransactionSummaryDao.class);

        QueryBuilder query = mock(QueryBuilder.class);
        when(daoSessionManager.getTransactionSummaryDao()).thenReturn(dao);
        when(dao.queryBuilder()).thenReturn(query);
        when(query.where(any())).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.unique()).thenReturn(sampleTransaction);
        return dao;
    }

    private TransactionDetail buildMockTransactionDetails(String txID) {
        TransactionDetail transactionDetail = mock(TransactionDetail.class);
        when(transactionDetail.getTransactionId()).thenReturn(txID);
        return transactionDetail;
    }

}