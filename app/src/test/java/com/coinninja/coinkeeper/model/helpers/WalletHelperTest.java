package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.AccountDao;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.FundingStat;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummaryDao;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.query.WalletQueryManager;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;

import org.greenrobot.greendao.query.LazyList;
import org.greenrobot.greendao.query.QueryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import app.dropbit.commons.currency.USDCurrency;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class WalletHelperTest {

    @Mock
    WalletQueryManager walletQueryManager;
    @Mock
    private DaoSessionManager daoSessionManager;
    @Mock
    private Wallet wallet;
    @Mock
    private WordHelper wordHelper;
    @Mock
    private PhoneNumber phoneNumber;

    @InjectMocks
    private WalletHelper walletHelper;

    @Before
    public void setUp() {
        when(wallet.getId()).thenReturn(1L);
        when(walletQueryManager.getPrimaryWallet()).thenReturn(wallet);
    }

    @After
    public void tearDown() {
        daoSessionManager = null;
        walletHelper = null;
        wordHelper = null;
        phoneNumber = null;
        walletQueryManager = null;
    }

    @Test
    public void provides_value_of_wallet_on_blockchain() {
        when(wallet.getBalance()).thenReturn(100000000L);
        when(wallet.getLastUSDPrice()).thenReturn(10000L);

        assertThat(walletHelper.btcChainWorth().toFormattedCurrency(), equalTo("$100.00"));
    }

    @Test
    public void caches_price() {
        walletHelper.setLatestPrice(new USDCurrency(500.00d));
        verify(wallet).setLastUSDPrice(50000L);
        verify(wallet).update();

        walletHelper.setLatestPrice(new USDCurrency(0L));
        verifyNoMoreInteractions(wallet);
    }

    @Test
    public void itSavesRecoveryWordsToWallet() {
        String[] recoveryWords = {"word1", "word2", "word3", "word4", "word5", "word6",
                "word7", "word8", "word9", "word10", "word11", "word12"};

        walletHelper.saveWords(recoveryWords);

        verify(wordHelper, times(1)).saveWord(1L, "word1", 0);
        verify(wordHelper, times(1)).saveWord(1L, "word2", 1);
        verify(wordHelper, times(1)).saveWord(1L, "word3", 2);
        verify(wordHelper, times(1)).saveWord(1L, "word4", 3);
        verify(wordHelper, times(1)).saveWord(1L, "word5", 4);
        verify(wordHelper, times(1)).saveWord(1L, "word6", 5);
        verify(wordHelper, times(1)).saveWord(1L, "word7", 6);
        verify(wordHelper, times(1)).saveWord(1L, "word8", 7);
        verify(wordHelper, times(1)).saveWord(1L, "word9", 8);
        verify(wordHelper, times(1)).saveWord(1L, "word10", 9);
        verify(wordHelper, times(1)).saveWord(1L, "word11", 10);
        verify(wordHelper, times(1)).saveWord(1L, "word12", 11);
    }

    @Test
    public void no_exception_when_words_or_addresses_are_null() {
        String[] recoveryWords = {"word1", "word2", "word3", "word4", "word5", "word6",
                "word7", "word8", "word9", "word10", "word11", "word12"};

        walletHelper.saveWords(recoveryWords);
    }

    @Test
    public void fetchesAllTranasctionsFromWalletLazely() {
        LazyList<TransactionsInvitesSummary> transactions = mock(LazyList.class);
        QueryBuilder mockQb = mock(QueryBuilder.class);
        TransactionsInvitesSummaryDao dao = mock(TransactionsInvitesSummaryDao.class);
        when(daoSessionManager.getTransactionsInvitesSummaryDao()).thenReturn(dao);
        when(dao.queryBuilder()).thenReturn(mockQb);
        when(mockQb.orderDesc(any(), any())).thenReturn(mockQb);
        when(mockQb.listLazyUncached()).thenReturn(transactions);

        assertThat(walletHelper.getTransactionsLazily(), equalTo(transactions));
    }

    @Test
    public void update_spendable_balances_test() {
        long expectedBalance = 15;

        List<TargetStat> targetStats = buildSampleTargetStats();
        List<FundingStat> fundingStats = buildSampleFundingStats();
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();

        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);

        walletHelper.updateBalances(wallet);

        verify(wallet).setBalance(expectedBalance);
    }

    @Test
    public void update_spendable_balances_never_include_any_received_invites_value_test() {
        //3 targetStats : ts1=5BTC ts2=10BTC ts3=5BTC_unconfirmed
        List<TargetStat> targetStats = buildSampleTargetStats();

        //2 fundingStats : fs1=3BTC fs2=2BTC
        List<FundingStat> fundingStats = buildSampleFundingStats();

        //2 invites : inv1=5BTC_fee=2BTC_sent inv2=5BTC_fee=null_received inv3=2BTC_fee=1BTC_sent
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();

        long expectedBalance = 15;

        InviteTransactionSummary invite = new InviteTransactionSummary();
        invite.setType(Type.BLOCKCHAIN_RECEIVED);
        invite.setBtcState(BTCState.UNFULFILLED);
        invite.setValueSatoshis(500l);
        invite.setValueFeesSatoshis(500l);

        invites.add(invite);

        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);

        walletHelper.updateBalances(wallet);

        verify(wallet).setBalance(expectedBalance);
    }

    @Test
    public void update_spendable_balances_dont_add_in_FULFILLED_invites_test() {
        //3 targetStats : ts1=5BTC ts2=10BTC ts3=5BTC_unconfirmed
        List<TargetStat> targetStats = buildSampleTargetStats();

        //2 fundingStats : fs1=3BTC fs2=2BTC
        List<FundingStat> fundingStats = buildSampleFundingStats();

        //2 invites : inv1=5BTC_fee=2BTC_sent inv2=5BTC_fee=null_received inv3=2BTC_fee=1BTC_sent
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();

        InviteTransactionSummary invite = new InviteTransactionSummary();
        invite.setType(Type.BLOCKCHAIN_SENT);
        invite.setBtcState(BTCState.FULFILLED);
        invite.setValueSatoshis(500l);
        invite.setValueFeesSatoshis(500l);

        invites.add(invite);

        long expectedBalance = 15;


        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);

        walletHelper.updateBalances(wallet);

        verify(wallet).setBalance(expectedBalance);
    }

    @Test
    public void update_spendable_balances_dont_add_in_CANCELED_invites_test() {
        //3 targetStats : ts1=5BTC ts2=10BTC ts3=5BTC_unconfirmed
        List<TargetStat> targetStats = buildSampleTargetStats();

        //2 fundingStats : fs1=3BTC fs2=2BTC
        List<FundingStat> fundingStats = buildSampleFundingStats();

        //2 invites : inv1=5BTC_fee=2BTC_sent inv2=5BTC_fee=null_received inv3=2BTC_fee=1BTC_sent
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();

        InviteTransactionSummary invite = new InviteTransactionSummary();
        invite.setType(Type.BLOCKCHAIN_SENT);
        invite.setBtcState(BTCState.CANCELED);
        invite.setValueSatoshis(200l);
        invite.setValueFeesSatoshis(620l);

        invites.add(invite);

        long expectedBalance = 15;


        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);

        walletHelper.updateBalances(wallet);

        verify(wallet).setBalance(expectedBalance);
    }

    @Test
    public void update_spendable_balances_dont_add_in_EXPIRED_invites_test() {
        //3 targetStats : ts1=5BTC ts2=10BTC ts3=5BTC_unconfirmed
        List<TargetStat> targetStats = buildSampleTargetStats();

        //2 fundingStats : fs1=3BTC fs2=2BTC
        List<FundingStat> fundingStats = buildSampleFundingStats();

        //2 invites : inv1=5BTC_fee=2BTC_sent inv2=5BTC_fee=null_received inv3=2BTC_fee=1BTC_sent
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();

        InviteTransactionSummary invite = new InviteTransactionSummary();
        invite.setType(Type.BLOCKCHAIN_SENT);
        invite.setBtcState(BTCState.EXPIRED);
        invite.setValueSatoshis(200l);
        invite.setValueFeesSatoshis(620l);

        invites.add(invite);

        long expectedBalance = 15;


        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);

        walletHelper.updateBalances(wallet);

        verify(wallet).setBalance(expectedBalance);
    }


    @Test
    public void update_un_spendable_balances_test() {
        long expectedBalance = 10;

        List<TargetStat> targetStats = buildSampleTargetStats();
        List<FundingStat> fundingStats = buildSampleFundingStats();
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();

        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);

        walletHelper.updateSpendableBalances(wallet);

        verify(wallet).setSpendableBalance(expectedBalance);
    }

    @Test
    public void calculate_invites_balance_sent_value_test() {
        InviteTransactionSummary invite = buildInvite(Type.BLOCKCHAIN_SENT, null);

        long value = walletHelper.calculateInviteValue(invite);

        assertThat(value, equalTo(600500l));
    }

    @Test
    public void calculate_invites_balance_received_value_test() {
        InviteTransactionSummary invite = buildInvite(Type.BLOCKCHAIN_RECEIVED, "");

        long value = walletHelper.calculateInviteValue(invite);

        assertThat(value, equalTo(600000l));
    }

    @Test
    public void calculate_invites_balance_sent_value_fulfilled_invites_test() {
        InviteTransactionSummary invite = buildInvite(Type.BLOCKCHAIN_SENT, "some - tx id");

        long value = walletHelper.calculateInviteValue(invite);

        assertThat(value, equalTo(0l));
    }

    @Test
    public void balances_never_negative_no_matter_what_test() {
        long expectedBalance = 0;

        List<TargetStat> targetStats = buildSampleTargetStats();
        List<FundingStat> fundingStats = buildSampleFundingStats();
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();

        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);

        InviteTransactionSummary invite = new InviteTransactionSummary();
        invite.setType(Type.BLOCKCHAIN_SENT);
        invite.setBtcState(BTCState.UNFULFILLED);
        invite.setValueSatoshis(30l);
        invite.setValueFeesSatoshis(5l);
        invites.add(invite);

        walletHelper.updateBalances(wallet);//with the above data the user balance = -20//but we should be protected from - balances

        verify(wallet).setBalance(expectedBalance);
    }

    @Test
    public void prove_the_balance_calculation_can_go_negative_test() {
        List<TargetStat> targetStats = buildSampleTargetStats();
        List<FundingStat> fundingStats = buildSampleFundingStats();
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();
        InviteTransactionSummary invite = new InviteTransactionSummary();
        invite.setType(Type.BLOCKCHAIN_SENT);
        invite.setBtcState(BTCState.UNFULFILLED);
        invite.setValueSatoshis(500l);
        invite.setValueFeesSatoshis(500l);
        invites.add(invite);
        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);


        long balanceCalculation = walletHelper.buildBalances(wallet, true);


        assertThat(balanceCalculation, equalTo(-985l));
    }

    @Test
    public void prove_the_balance_never_is_set_negative_test() {
        List<TargetStat> targetStats = buildSampleTargetStats();
        List<FundingStat> fundingStats = buildSampleFundingStats();
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();
        InviteTransactionSummary invite = new InviteTransactionSummary();
        invite.setType(Type.BLOCKCHAIN_SENT);
        invite.setBtcState(BTCState.UNFULFILLED);
        invite.setValueSatoshis(500l);
        invite.setValueFeesSatoshis(500l);
        invites.add(invite);
        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);


        walletHelper.updateBalances(wallet);

        verify(wallet).setBalance(0);//the real value of this wallet is -985l but we only show 0
    }

    @Test
    public void prove_the_spendable_balance_never_is_set_negative_test() {
        List<TargetStat> targetStats = buildSampleTargetStats();
        List<FundingStat> fundingStats = buildSampleFundingStats();
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();
        InviteTransactionSummary invite = new InviteTransactionSummary();
        invite.setType(Type.BLOCKCHAIN_SENT);
        invite.setBtcState(BTCState.UNFULFILLED);
        invite.setValueSatoshis(500l);
        invite.setValueFeesSatoshis(500l);
        invites.add(invite);
        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);


        walletHelper.updateSpendableBalances(wallet);

        verify(wallet).setSpendableBalance(0);//the real spendable value of this wallet is -985l but we only show 0
    }

    @Test
    public void do_not_use_TargetStat_who_state_is_canceled() {
        List<TargetStat> targetStats = buildSampleTargetStats();
        List<FundingStat> fundingStats = buildSampleFundingStats();
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();

        TransactionSummary tx_10_confirmations = mock(TransactionSummary.class);
        when(tx_10_confirmations.getNumConfirmations()).thenReturn(10);
        TargetStat canceledTargetStat = mock(TargetStat.class);
        when(canceledTargetStat.getValue()).thenReturn(10l);
        when(canceledTargetStat.getTransaction()).thenReturn(tx_10_confirmations);
        targetStats.add(canceledTargetStat);

        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);


        when(canceledTargetStat.getState()).thenReturn(TargetStat.State.ACKNOWLEDGE);
        long balanceCalculation = walletHelper.buildBalances(wallet, true);
        assertThat(balanceCalculation, equalTo(25L));//with the canceledTargetStat added in the balances is 25 satoshis


        when(canceledTargetStat.getState()).thenReturn(TargetStat.State.CANCELED);
        long newBalanceCalculation = walletHelper.buildBalances(wallet, true);
        assertThat(newBalanceCalculation, equalTo(15L));//without the canceledTargetStat added in the balances is 15 satoshis
    }

    @Test
    public void do_not_use_Fundingstat_who_state_is_canceled() {
        List<TargetStat> targetStats = buildSampleTargetStats();
        List<FundingStat> fundingStats = buildSampleFundingStats();
        List<InviteTransactionSummary> invites = buildSampleInviteTransactionSummarys();

        FundingStat canceledFundingStat = mock(FundingStat.class);
        when(canceledFundingStat.getValue()).thenReturn(10l);
        fundingStats.add(canceledFundingStat);

        when(wallet.getTargetStats()).thenReturn(targetStats);
        when(wallet.getFundingStats()).thenReturn(fundingStats);
        when(wallet.getInviteTransactionSummaries()).thenReturn(invites);


        when(canceledFundingStat.getState()).thenReturn(FundingStat.State.ACKNOWLEDGE);

        long balanceCalculation = walletHelper.buildBalances(wallet, true);

        assertThat(balanceCalculation, equalTo(5L));//with the canceledFundingStat added in the balances is 5 satoshis


        when(canceledFundingStat.getState()).thenReturn(FundingStat.State.CANCELED);

        long newBalanceCalculation = walletHelper.buildBalances(wallet, true);

        assertThat(newBalanceCalculation, equalTo(15L));//without the canceledFundingStat added in the balances is 15 satoshis
    }

    @Test
    public void saveAccountRegistration() {
        CNUserAccount cnUserAccount = mock(CNUserAccount.class);
        CNPhoneNumber cnPhoneNumber = mock(CNPhoneNumber.class);
        String cnUserId = "---some id ---";
        String phoneNumberHash = "---phoneNumberHash ---";
        when(cnPhoneNumber.toPhoneNumber()).thenReturn(phoneNumber);
        when(cnUserAccount.getId()).thenReturn(cnUserId);
        String verificationStatus = "pending-verification";
        when(cnUserAccount.getStatus()).thenReturn(verificationStatus);
        when(cnUserAccount.getPhoneNumberHash()).thenReturn(phoneNumberHash);

        Account account = mock(Account.class);
        AccountDao dao = mock(AccountDao.class);
        QueryBuilder mockQb = mock(QueryBuilder.class);
        when(daoSessionManager.getAccountDao()).thenReturn(dao);
        when(dao.queryBuilder()).thenReturn(mockQb);
        when(mockQb.where(any())).thenReturn(mockQb);
        when(mockQb.limit(1)).thenReturn(mockQb);
        when(mockQb.unique()).thenReturn(account);
        when(account.getCnWalletId()).thenReturn("wallet id");

        walletHelper.saveAccountRegistration(cnUserAccount, cnPhoneNumber);

        verify(account).setPhoneNumber(phoneNumber);
        verify(account).setPhoneNumberHash(phoneNumberHash);
        verify(account).setCnUserId(cnUserId);
        verify(account).populateStatus(verificationStatus);
    }

    @Test
    public void assert_that_getting_null_wallet_does_not_cause_null_pointer_exception() {
        when(walletQueryManager.getPrimaryWallet()).thenReturn(null);
        walletHelper.setLatestFee(new TransactionFee(20.0, 20.0, 20.0));
    }

    @Test
    public void assert_that_setting_negative_to_tx_fees_does_not_overwrite() {
        walletHelper.setLatestFee(new TransactionFee(-1.0, -1.0, -1.0));

        verifyZeroInteractions(wallet);
    }

    @Test
    public void assert_that_setting_0_to_tx_fees_does_not_overwrite() {
        walletHelper.setLatestFee(new TransactionFee(0.0, 0.0, 0.0));

        verifyZeroInteractions(wallet);
    }

    @Test
    public void assert_that_setting_null_to_tx_fees_does_not_overwrite() {
        walletHelper.setLatestFee(null);

        verifyZeroInteractions(wallet);
    }

    @Test
    public void saveAccountRegistration_noop_if_have_no_account() {
        Account account = mock(Account.class);
        AccountDao dao = mock(AccountDao.class);
        QueryBuilder mockQb = mock(QueryBuilder.class);
        when(daoSessionManager.getAccountDao()).thenReturn(dao);
        when(daoSessionManager.getAccountDao()).thenReturn(dao);
        when(dao.queryBuilder()).thenReturn(mockQb);
        when(mockQb.where(any())).thenReturn(mockQb);
        when(mockQb.limit(1)).thenReturn(mockQb);
        when(mockQb.unique()).thenReturn(account);

        when(account.getCnWalletId()).thenReturn(null);

        walletHelper.saveAccountRegistration(null, null);
        // no null pointer
    }

    private InviteTransactionSummary buildInvite(Type type, String txID) {
        InviteTransactionSummary invite = new InviteTransactionSummary();

        invite.setBtcTransactionId(txID);
        invite.setType(type);
        invite.setValueSatoshis(600000l);
        invite.setValueFeesSatoshis(500l);
        return invite;
    }

    private List<TargetStat> buildSampleTargetStats() {
        Address address = mock(Address.class);
        when(address.getChangeIndex()).thenReturn(0);

        TransactionSummary tx_10_confirmations = mock(TransactionSummary.class);
        when(tx_10_confirmations.getNumConfirmations()).thenReturn(10);

        TransactionSummary tx_zero_confirmation = mock(TransactionSummary.class);
        when(tx_zero_confirmation.getNumConfirmations()).thenReturn(0);

        List<TargetStat> targetStats = new ArrayList<>();

        TargetStat targetStats1 = mock(TargetStat.class);
        when(targetStats1.getValue()).thenReturn(10l);
        when(targetStats1.getTransaction()).thenReturn(tx_10_confirmations);


        TargetStat targetStats2 = mock(TargetStat.class);
        when(targetStats2.getValue()).thenReturn(5l);
        when(targetStats2.getTransaction()).thenReturn(tx_10_confirmations);

        TargetStat targetStats3 = mock(TargetStat.class);
        when(targetStats3.getValue()).thenReturn(5l);
        when(targetStats3.getTransaction()).thenReturn(tx_zero_confirmation);
        when(targetStats3.getAddress()).thenReturn(address);


        targetStats.add(targetStats1);
        targetStats.add(targetStats2);
        targetStats.add(targetStats3);
        return targetStats;
    }

    private List<FundingStat> buildSampleFundingStats() {
        List<FundingStat> fundingStats = new ArrayList<>();

        FundingStat fundingStat1 = new FundingStat();
        fundingStat1.setValue(3);

        FundingStat fundingStat2 = new FundingStat();
        fundingStat2.setValue(2);

        fundingStats.add(fundingStat1);
        fundingStats.add(fundingStat2);
        return fundingStats;
    }

    private List<InviteTransactionSummary> buildSampleInviteTransactionSummarys() {
        List<InviteTransactionSummary> inviteTransactionSummaries = new ArrayList<>();

        InviteTransactionSummary invite1 = new InviteTransactionSummary();
        invite1.setType(Type.BLOCKCHAIN_SENT);
        invite1.setBtcState(BTCState.FULFILLED);
        invite1.setValueSatoshis(5l);
        invite1.setValueFeesSatoshis(2l);

        InviteTransactionSummary invite2 = new InviteTransactionSummary();
        invite2.setType(Type.BLOCKCHAIN_RECEIVED);
        invite2.setValueSatoshis(5l);
        invite2.setValueFeesSatoshis(null);

        InviteTransactionSummary invite3 = new InviteTransactionSummary();
        invite3.setType(Type.BLOCKCHAIN_SENT);
        invite3.setBtcState(BTCState.UNFULFILLED);
        invite3.setBtcTransactionId("some - tx -id");
        invite3.setValueSatoshis(2l);
        invite3.setValueFeesSatoshis(1l);


        inviteTransactionSummaries.add(invite1);
        inviteTransactionSummaries.add(invite2);
        inviteTransactionSummaries.add(invite3);
        return inviteTransactionSummaries;
    }


}