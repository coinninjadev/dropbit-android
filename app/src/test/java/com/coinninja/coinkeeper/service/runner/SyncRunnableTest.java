package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.service.client.model.TransactionStats;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.wallet.data.TestData;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyncRunnableTest {

    private Gson gson = new Gson();

    @Mock
    private Wallet wallet;

    @Mock
    private SharedMemoRetrievalRunner sharedMemoRetrievalRunner;

    @Mock
    private CNWalletManager cnWalletManager;

    @Mock
    private AccountManager accountManager;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private AddressHelper addressHelper;

    @Mock
    private HDWallet hdWallet;

    @Mock
    private AddressAPIUtil addressAPIUtil;

    @Mock
    private TransactionHelper transactionHelper;

    @Mock
    private TransactionAPIUtil transactionAPIUtil;

    @Mock
    private Analytics analytics;

    private List<GsonAddress> gsonAddresses = new ArrayList<>();
    private List<Address> addresses = new ArrayList<>();

    @InjectMocks
    private SyncRunnable syncRunner;

    @After
    public void tearDown() {
        gson = null;
        wallet = null;
        cnWalletManager = null;
        accountManager = null;
        walletHelper = null;
        addressHelper = null;
        analytics = null;
        transactionAPIUtil = null;
        gsonAddresses = null;
        addresses = null;
        sharedMemoRetrievalRunner = null;
    }

    @Before
    public void setUp() {
        gsonAddresses = new ArrayList<>();
        when(addressAPIUtil.fetchAddresses(any(HDWallet.class), anyInt(), anyInt(), anyInt())).thenReturn(gsonAddresses);
        when(addressHelper.addAddresses(any(), anyInt())).thenReturn(addresses);
        when(cnWalletManager.hasWallet()).thenReturn(true);
    }

    @Test
    public void returns_when_no_there_is_no_wallet() {
        when(cnWalletManager.hasWallet()).thenReturn(false);

        syncRunner.run();

        verify(cnWalletManager, times(0)).syncCompleted();
    }

    @Test
    public void updateSpentStatusForAddressesTargetStats() {
        syncRunner.run();

        verify(addressHelper).updateSpentTransactions();
    }

    @Test
    public void invokes_shared_memo_retrieval_runner() {
        syncRunner.run();

        verify(sharedMemoRetrievalRunner).run();
    }

    @Test
    public void sets_look_ahead_to_20_on_first_sync() {
        when(cnWalletManager.isFirstSync()).thenReturn(true);

        syncRunner.run();

        verify(addressAPIUtil).setLookAhead(20);
    }

    @Test
    public void sets_look_ahead_to_5_on_subsequent_sync() {
        when(cnWalletManager.isFirstSync()).thenReturn(false);

        syncRunner.run();

        verify(addressAPIUtil).setLookAhead(5);
    }

    @Test
    public void normalSyncUsesSmallerGap() {
        syncRunner.run();

        verify(addressAPIUtil).setLookAhead(AddressAPIUtil.LOOK_AHEAD);
    }

    @Test
    public void updatesWalletBalance() {
        syncRunner.run();

        verify(walletHelper).updateBalances();
    }

    @Test
    public void savesFeeData() {
        TransactionStats ts = new TransactionStats();
        ts.setFees(1905L);
        ts.setFeesRate(5107L);
        ts.setCoinBase(false);

        List<TransactionSummary> transactions = new ArrayList<>();
        TransactionSummary t1 = mock(TransactionSummary.class);
        transactions.add(t1);
        when(transactionHelper.getTransactionsWithoutFees()).thenReturn(transactions);
        when(transactionAPIUtil.fetchFeesFor(t1)).thenReturn(ts);

        syncRunner.run();

        verify(t1).setFee(1905L);
        verify(t1).update();
    }

    @Test
    public void fetchesHistoricPriceForTransactions() {
        List<TransactionSummary> transactions = new ArrayList<>();

        syncRunner.run();

        verify(transactionAPIUtil).updateHistoricPricingIfNecessary(transactions);
    }

    private List<TransactionSummary> mockPartialTransactions() {
        List<TransactionSummary> transactions = new ArrayList<>();
        when(transactionHelper.getIncompleteTransactions()).thenReturn(transactions);
        TransactionSummary t1 = new TransactionSummary();
        t1.setTxid("1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24");
        transactions.add(t1);

        TransactionSummary t2 = new TransactionSummary();
        t2.setTxid("9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57");
        transactions.add(t2);
        return transactions;
    }

    @Test
    public void updatesPartialTransactions() {
        when(walletHelper.getBlockTip()).thenReturn(562372);
        List<TransactionDetail> fetchedTransactions = new ArrayList<>();
        TransactionDetail t1 = gson.fromJson(TestData.TRANSACTIONS_ONE, new TypeToken<TransactionDetail>() {
        }.getType());
        TransactionDetail t2 = gson.fromJson(TestData.TRANSACTIONS_TWO, new TypeToken<TransactionDetail>() {
        }.getType());
        fetchedTransactions.add(t1);
        fetchedTransactions.add(t2);

        List<TransactionSummary> transactions = mockPartialTransactions();
        when(transactionAPIUtil.fetchPartialTransactions(transactions)).thenReturn(fetchedTransactions);

        syncRunner.run();

        verify(transactionHelper).updateTransactions(fetchedTransactions, 562372);
    }

    @Test
    public void fetchesIncompleteTransactions() {
        List<TransactionSummary> transactions = mockPartialTransactions();

        syncRunner.run();

        verify(transactionAPIUtil).fetchPartialTransactions(transactions);
    }

    @Test
    public void itSavesReferencesToTransactions() {
        Gson gson = new Gson();
        List<GsonAddress> responseAddresses = gson.fromJson(TestData.EXTERNAL_ADDRESS_RESPONSE_BLOCK_ONE, new TypeToken<List<GsonAddress>>() {
        }.getType());
        when(addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 1)).thenReturn(responseAddresses);

        syncRunner.run();

        verify(transactionHelper).initTransactions(responseAddresses);
    }

    @Test
    public void savesExternalAddresses() {
        Gson gson = new Gson();
        List<GsonAddress> responseAddresses = gson.fromJson(TestData.EXTERNAL_ADDRESS_RESPONSE_BLOCK_ONE, new TypeToken<List<GsonAddress>>() {
        }.getType());
        when(addressAPIUtil.fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 1)).thenReturn(responseAddresses);

        syncRunner.run();

        verify(addressHelper).addAddresses(responseAddresses, HDWallet.EXTERNAL);
    }

    @Test
    public void savesInternalAddresses() {
        Gson gson = new Gson();
        List<GsonAddress> responseAddresses = gson.fromJson(TestData.INTERNAL_ADDRESS_RESPONSE_BLOCK_ONE, new TypeToken<List<GsonAddress>>() {
        }.getType());
        when(addressAPIUtil.fetchAddresses(hdWallet, HDWallet.INTERNAL, 0, 1)).thenReturn(responseAddresses);

        syncRunner.run();

        verify(addressHelper).addAddresses(responseAddresses, HDWallet.INTERNAL);
    }

    @Test
    public void setsInternalAddressToCorrectIndex() {
        when(addressAPIUtil.getLargestIndexConsumed()).thenReturn(3);

        syncRunner.run();

        verify(accountManager).reportLargestChangeIndexConsumed(3);


        when(addressAPIUtil.getLargestIndexConsumed()).thenReturn(0);

        syncRunner.run();

        verify(accountManager).reportLargestChangeIndexConsumed(0);
    }

    @Test
    public void setsExternalAddressToCorrectPositionWhenAddressesAreFetched() {
        when(addressAPIUtil.getLargestIndexConsumed()).thenReturn(5);

        syncRunner.run();

        verify(accountManager).reportLargestReceiveIndexConsumed(5);
    }

    @Test
    public void doesNotSaveALowerExternalAddressForWallet() {
        when(addressAPIUtil.getLargestIndexConsumed()).thenReturn(1);

        syncRunner.run();

        verify(wallet, times(0)).setExternalIndex(anyInt());
    }

    @Test
    public void fetchesExternalAddresses() {
        when(accountManager.getLargestReportedReceiveAddress()).thenReturn(5);

        syncRunner.run();

        verify(addressAPIUtil).fetchAddresses(hdWallet, HDWallet.EXTERNAL, 0, 6);
    }

    @Test
    public void fetchesInternalAddresses() {
        when(accountManager.getLargestReportedChangeAddress()).thenReturn(2);

        syncRunner.run();

        verify(addressAPIUtil).fetchAddresses(hdWallet, HDWallet.INTERNAL, 0, 3);
    }

    @Test
    public void caches_addresses_once_sync_completed() {
        InOrder inOrder = inOrder(cnWalletManager, accountManager);
        syncRunner.run();

        inOrder.verify(cnWalletManager).syncCompleted();
        inOrder.verify(accountManager).cacheAddresses();
    }

    @Test
    public void updatesLastSyncOnWallet() {
        syncRunner.run();

        verify(cnWalletManager).syncCompleted();
    }

    @Test
    public void sets_has_received_as_user_property() {
        when(addressHelper.hasReceivedTransaction()).thenReturn(false).thenReturn(true);

        syncRunner.run();
        syncRunner.run();

        // only runs 1 time because the first time, no receives happened
        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_RECEIVED_ADDRESS, true);
    }

    @Test
    public void links_address_to_stats() {
        syncRunner.run();

        verify(walletHelper).linkStatsWithAddressBook();
    }
}