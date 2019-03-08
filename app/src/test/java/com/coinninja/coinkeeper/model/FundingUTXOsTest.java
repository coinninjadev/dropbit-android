package com.coinninja.coinkeeper.model;

import android.content.Context;
import android.content.res.Resources;

import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.service.runner.FundingRunnable;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;

import org.greenrobot.greendao.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FundingUTXOsTest {

    @Mock
    private Context context;
    @Mock
    private DaoSessionManager daoSessionManager;
    @Mock
    private TargetStatDao targetStatDao;
    @Mock
    private Resources resources;

    private int changeIndex = 22;
    private String paymentAddress = "--address--";
    private long satoshisSpending = 1000;
    private long satoshisFee = 500;

    @Mock
    private HDWallet hdWallet;
    private TransactionFee fee = new TransactionFee(10, 20, 30);

    @InjectMocks
    private FundingRunnable FundingRunnable;

    @Before
    public void setUp() throws Exception {
        when(context.getResources()).thenReturn(resources);
        when(daoSessionManager.getTargetStatDao()).thenReturn(targetStatDao);

        FundingRunnable.setCurrentChangeAddressIndex(changeIndex);
        FundingRunnable.setPaymentAddress(paymentAddress);
        FundingRunnable.setTransactionFee(fee);

        BTCCurrency mockBtcFee = mock(BTCCurrency.class);
        when(mockBtcFee.toSatoshis()).thenReturn(satoshisFee);

        when(hdWallet.getFeeForTransaction(anyObject(), anyInt())).thenReturn(mockBtcFee);


    }

    @Test
    public void build_runner() {
        long expectedFundedTotal = 5000;
        long expectedFee = 500;
        long expectedSpending = 1000;
        long expectedTotalSpending = expectedFee + expectedSpending;
        long expectedChange = 3500;

        List<TargetStat> usableTargets = buildTargetStats();

        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        when(targetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any())).thenReturn(qb);
        when(qb.orderDesc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(usableTargets);

        FundingUTXOs fundingUTXOs = new FundingUTXOs.Builder(hdWallet)
                .setUsableTargets(usableTargets)
                .setSatoshisSpending(satoshisSpending)
                .setTransactionFee(fee)
                .build();

        assertThat(fundingUTXOs.getSatoshisFundedTotal(), equalTo(expectedFundedTotal));
        assertThat(fundingUTXOs.getSatoshisFeesSpending(), equalTo(expectedFee));
        assertThat(fundingUTXOs.getSatoshisSpending(), equalTo(expectedSpending));
        assertThat(fundingUTXOs.getSatoshisTotalSpending(), equalTo(expectedTotalSpending));
        long currentChangeAmount = fundingUTXOs.getSatoshisFundedTotal() - fundingUTXOs.getSatoshisTotalSpending();
        assertThat(currentChangeAmount, equalTo(expectedChange));
        assertThat(fundingUTXOs.getUsingUTXOs().size(), equalTo(1));
        assertThat(fundingUTXOs.getUsingUTXOs().get(0).getAmount(), equalTo(5000L));
        assertThat(fundingUTXOs.getUsingUTXOs().get(0).getIndex(), equalTo(12));
    }


    @Test
    public void build_unconfirmed_are_replaceable() {
        long expectedFundedTotal = 5000;
        long expectedFee = 500;
        long expectedSpending = 1000;
        long expectedTotalSpending = expectedFee + expectedSpending;
        long expectedChange = 3500;

        //TODO: If this was set as change address, we could use 0 or 1 and the test would work
        List<TargetStat> usableTargets = buildTargetStatsWithConfirmationCount(2);

        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        when(targetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any())).thenReturn(qb);
        when(qb.orderDesc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(usableTargets);

        FundingUTXOs fundingUTXOs = new FundingUTXOs.Builder(hdWallet)
                .setUsableTargets(usableTargets)
                .setSatoshisSpending(satoshisSpending)
                .setTransactionFee(fee)
                .build();

        assertThat(fundingUTXOs.getUsingUTXOs().size(), equalTo(1));
        UnspentTransactionOutput utxo = fundingUTXOs.getUsingUTXOs().get(0);
        assertThat(utxo.getAmount(), equalTo(5000L));
        assertThat(utxo.getIndex(), equalTo(12));
        assertTrue(utxo.isReplaceable());
    }

    @Test
    public void build_confirmed_are_not_replaceable() {
        long expectedFundedTotal = 5000;
        long expectedFee = 500;
        long expectedSpending = 1000;
        long expectedTotalSpending = expectedFee + expectedSpending;
        long expectedChange = 3500;

        List<TargetStat> usableTargets = buildTargetStatsWithConfirmationCount(4);

        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        when(targetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any())).thenReturn(qb);
        when(qb.orderDesc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(usableTargets);

        FundingUTXOs fundingUTXOs = new FundingUTXOs.Builder(hdWallet)
                .setUsableTargets(usableTargets)
                .setSatoshisSpending(satoshisSpending)
                .setTransactionFee(fee)
                .build();

        assertThat(fundingUTXOs.getUsingUTXOs().size(), equalTo(1));
        UnspentTransactionOutput utxo = fundingUTXOs.getUsingUTXOs().get(0);
        assertThat(utxo.getAmount(), equalTo(5000L));
        assertThat(utxo.getIndex(), equalTo(12));
        assertFalse(utxo.isReplaceable());
    }

    @Test
    public void dusty_change_give_to_miner() {
        long expectedFundedTotal = 5000;
        long expectedRAWFee = 500;
        long expectedRAWDust = 400;
        long expectedSpending = 4100;
        long expectedTotalSpending = expectedRAWDust + expectedRAWFee + expectedSpending;
        long expectedChange = 0;//change would be 400 but we will give that to the miner

        List<TargetStat> usableTargets = buildTargetStats();

        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        when(targetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any())).thenReturn(qb);
        when(qb.orderDesc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(usableTargets);

        FundingUTXOs fundingUTXOs = new FundingUTXOs.Builder(hdWallet)
                .setUsableTargets(usableTargets)
                .setSatoshisSpending(4100)
                .setTransactionFee(fee)
                .build();

        assertThat(fundingUTXOs.getSatoshisFundedTotal(), equalTo(expectedFundedTotal));
        assertThat(fundingUTXOs.getRAWSatoshisFeesSpending(), equalTo(expectedRAWFee));
        assertThat(fundingUTXOs.getRAWSatoshisDustGiveToMiner(), equalTo(expectedRAWDust));
        assertThat(fundingUTXOs.getSatoshisSpending(), equalTo(expectedSpending));
        assertThat(fundingUTXOs.getSatoshisTotalSpending(), equalTo(expectedTotalSpending));
        long currentChangeAmount = fundingUTXOs.getSatoshisFundedTotal() - fundingUTXOs.getSatoshisTotalSpending();
        assertThat(currentChangeAmount, equalTo(expectedChange));
    }

    @Test
    public void fees_if_specified_not_calculated_test() {
        long expectedFundedTotal = 5000;
        long expectedFee = 700;
        long expectedSpending = 1000;
        long expectedTotalSpending = expectedFee + expectedSpending;
        long expectedChange = 3500;

        List<TargetStat> usableTargets = buildTargetStats();

        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        when(targetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any())).thenReturn(qb);
        when(qb.orderDesc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(usableTargets);

        FundingUTXOs fundingUTXOs = new FundingUTXOs.Builder(hdWallet)
                .setUsableTargets(usableTargets)
                .setSatoshisSpending(satoshisSpending)
                .setTransactionFee(null)
                .setSatoshisDesiredFeeAmount(700)
                .build();

        assertThat(fundingUTXOs.getSatoshisFundedTotal(), equalTo(expectedFundedTotal));
        assertThat(fundingUTXOs.getSatoshisFeesSpending(), equalTo(expectedFee));
    }

    @Test
    public void no_dust_borderline_test() {
        long expectedFundedTotal = 5000;
        long expectedRAWFee = 500;
        long expectedRAWDust = 0;
        long expectedSpending = 3999;
        long expectedTotalSpending = expectedRAWDust + expectedRAWFee + expectedSpending;
        long expectedChange = 501;//change would be 400 but we will give that to the miner

        List<TargetStat> usableTargets = buildTargetStats();

        QueryBuilder<TargetStat> qb = mock(QueryBuilder.class);
        when(targetStatDao.queryBuilder()).thenReturn(qb);
        when(qb.where(any(), any())).thenReturn(qb);
        when(qb.orderDesc(any())).thenReturn(qb);
        when(qb.list()).thenReturn(usableTargets);

        FundingUTXOs fundingUTXOs = new FundingUTXOs.Builder(hdWallet)
                .setUsableTargets(usableTargets)
                .setSatoshisSpending(3999)
                .setTransactionFee(fee)
                .build();

        assertThat(fundingUTXOs.getSatoshisFundedTotal(), equalTo(expectedFundedTotal));
        assertThat(fundingUTXOs.getRAWSatoshisFeesSpending(), equalTo(expectedRAWFee));
        assertThat(fundingUTXOs.getRAWSatoshisDustGiveToMiner(), equalTo(expectedRAWDust));
        assertThat(fundingUTXOs.getSatoshisSpending(), equalTo(expectedSpending));
        assertThat(fundingUTXOs.getSatoshisTotalSpending(), equalTo(expectedTotalSpending));
        long currentChangeAmount = fundingUTXOs.getSatoshisFundedTotal() - fundingUTXOs.getSatoshisTotalSpending();
        assertThat(currentChangeAmount, equalTo(expectedChange));
    }

    private List<TargetStat> buildTargetStats() {
        return buildTargetStatsWithConfirmationCount(55);
    }

    private List<TargetStat> buildTargetStatsWithConfirmationCount(int confirmationCount) {
        TargetStat stat1 = mock(TargetStat.class);
        Address address1 = mock(Address.class);
        TransactionSummary previousTransaction = mock(TransactionSummary.class);

        when(stat1.getAddress()).thenReturn(address1);
        when(stat1.getTransaction()).thenReturn(previousTransaction);
        when(previousTransaction.getNumConfirmations()).thenReturn(confirmationCount);

        when(stat1.getFundingId()).thenReturn(33L);
        when(stat1.getAddress()).thenReturn(address1);
        when(stat1.getAddr()).thenReturn("address1");
        when(stat1.getValue()).thenReturn(5000L);
        when(address1.getChangeIndex()).thenReturn(0);
        when(address1.getIndex()).thenReturn(52);
        when(stat1.getPosition()).thenReturn(12);


        List<TargetStat> list = new ArrayList<>();
        list.add(stat1);

        return list;
    }
}