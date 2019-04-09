package com.coinninja.coinkeeper.service.runner;

import android.content.Context;
import android.content.res.Resources;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FundingRunnableTest {
    @Mock
    private Context context;
    @Mock
    private Resources resources;
    @Mock
    private HDWallet hdWallet;
    @Mock
    private TransactionFee transactionFee;
    @Mock
    private TargetStatHelper targetStatHelper;

    private String fundingError = "funding error";

    private FundingRunnable runner;

    @Before
    public void setUp() throws Exception {
        when(context.getResources()).thenReturn(resources);
        when(resources.getString(R.string.pay_not_enough_funds_error)).thenReturn(fundingError);

        runner = new FundingRunnable(context, hdWallet, targetStatHelper);
        runner.setTransactionFee(transactionFee);

        BTCCurrency mockBtcFee = mock(BTCCurrency.class);
        when(hdWallet.getFeeForTransaction(anyObject(), anyInt())).thenReturn(mockBtcFee);
    }

    @After
    public void tearDown() {
        runner = null;
        fundingError = null;
        targetStatHelper = null;
        transactionFee = null;
        hdWallet = null;
        resources = null;
        context = null;
    }

    @Test
    public void execute_runner() {
        List<TargetStat> usableTargets = buildTargetStats();
        when(targetStatHelper.getSpendableTargets()).thenReturn(usableTargets);

        Long satoshisTotalSpend = 3982l;
        FundingUTXOs fundingUTXOs = runner.fundRun(satoshisTotalSpend, null);


        assertThat(fundingUTXOs.getSatoshisFundedTotal(), equalTo(500L));
        assertThat(fundingUTXOs.getUsingUTXOs().size(), equalTo(1));
        assertThat(fundingUTXOs.getUsingUTXOs().get(0).getAmount(), equalTo(500L));
        assertThat(fundingUTXOs.getUsingUTXOs().get(0).getIndex(), equalTo(12));
    }

    @Test
    public void post_execute() {
        FundingUTXOs fundingUTXOs = mock(FundingUTXOs.class);

        FundingRunnable.FundedHolder data = runner.evaluateFundingUTXOs(fundingUTXOs);

        assertThat(data.getUnspentTransactionHolder().satoshisUnspentTotal, equalTo(0l));
    }

    @Test
    public void post_execute_error() {
        FundingRunnable.FundedHolder data = runner.evaluateFundingUTXOs(null);

        assertThat(data.getErrorReason(), equalTo(fundingError));
        assertThat(data.getSatoshisFee(), equalTo(0l));
        verify(resources).getString(R.string.pay_not_enough_funds_error);

    }

    @Test
    public void evaluateFundingUTXOs_getSatoshisFundedTotal_negative_from_long_overflow() {
        FundingUTXOs mockFundingUtxos = mock(FundingUTXOs.class);
        when(mockFundingUtxos.getSatoshisFundedTotal()).thenReturn(-1L);

        FundingRunnable.FundedHolder result = runner.evaluateFundingUTXOs(mockFundingUtxos);

        assertThat(result.getErrorReason(), equalTo(fundingError));
        assertThat(result.getSatoshisFee(), equalTo(0l));
        verify(resources).getString(R.string.pay_not_enough_funds_error);
    }

    @Test
    public void evaluateFundingUTXOs_getSatoshisFeesSpending_negative_from_long_overflow() {
        FundingUTXOs mockFundingUtxos = mock(FundingUTXOs.class);
        when(mockFundingUtxos.getSatoshisFeesSpending()).thenReturn(-1L);

        FundingRunnable.FundedHolder result = runner.evaluateFundingUTXOs(mockFundingUtxos);

        assertThat(result.getErrorReason(), equalTo(fundingError));
        assertThat(result.getSatoshisFee(), equalTo(0l));
        verify(resources).getString(R.string.pay_not_enough_funds_error);
    }

    @Test
    public void evaluateFundingUTXOs_getSatoshisSpending_negative_from_long_overflow() {
        FundingUTXOs mockFundingUtxos = mock(FundingUTXOs.class);
        when(mockFundingUtxos.getSatoshisSpending()).thenReturn(-1L);

        FundingRunnable.FundedHolder result = runner.evaluateFundingUTXOs(mockFundingUtxos);

        assertThat(result.getErrorReason(), equalTo(fundingError));
        assertThat(result.getSatoshisFee(), equalTo(0l));
        verify(resources).getString(R.string.pay_not_enough_funds_error);
    }

    @Test
    public void evaluateFundingUTXOs_getSatoshisTotalSpending_negative_from_long_overflow() {
        FundingUTXOs mockFundingUtxos = mock(FundingUTXOs.class);
        when(mockFundingUtxos.getSatoshisTotalSpending()).thenReturn(-1L);

        FundingRunnable.FundedHolder result = runner.evaluateFundingUTXOs(mockFundingUtxos);

        assertThat(result.getErrorReason(), equalTo(fundingError));
        assertThat(result.getSatoshisFee(), equalTo(0l));
        verify(resources).getString(R.string.pay_not_enough_funds_error);
    }


    @Test
    public void evaluateFundingUTXOs_getSatoshisFundedTotal_MaxValue() {
        FundingUTXOs mockFundingUtxos = mock(FundingUTXOs.class);
        when(mockFundingUtxos.getSatoshisFundedTotal()).thenReturn(BTCCurrency.MAX_SATOSHI);

        FundingRunnable.FundedHolder result = runner.evaluateFundingUTXOs(mockFundingUtxos);

        assertThat(result.getErrorReason(), equalTo(fundingError));
        assertThat(result.getSatoshisFee(), equalTo(0l));
        verify(resources).getString(R.string.pay_not_enough_funds_error);
    }

    @Test
    public void evaluateFundingUTXOs_getSatoshisFeesSpending_MaxValue() {
        FundingUTXOs mockFundingUtxos = mock(FundingUTXOs.class);
        when(mockFundingUtxos.getSatoshisFeesSpending()).thenReturn(BTCCurrency.MAX_SATOSHI);

        FundingRunnable.FundedHolder result = runner.evaluateFundingUTXOs(mockFundingUtxos);

        assertThat(result.getErrorReason(), equalTo(fundingError));
        assertThat(result.getSatoshisFee(), equalTo(0l));
        verify(resources).getString(R.string.pay_not_enough_funds_error);
    }

    @Test
    public void evaluateFundingUTXOs_getSatoshisSpending_MaxValue() {
        FundingUTXOs mockFundingUtxos = mock(FundingUTXOs.class);
        when(mockFundingUtxos.getSatoshisSpending()).thenReturn(BTCCurrency.MAX_SATOSHI);

        FundingRunnable.FundedHolder result = runner.evaluateFundingUTXOs(mockFundingUtxos);

        assertThat(result.getErrorReason(), equalTo(fundingError));
        assertThat(result.getSatoshisFee(), equalTo(0l));
        verify(resources).getString(R.string.pay_not_enough_funds_error);
    }

    @Test
    public void evaluateFundingUTXOs_getSatoshisTotalSpending_MaxValue() {
        FundingUTXOs mockFundingUtxos = mock(FundingUTXOs.class);
        when(mockFundingUtxos.getSatoshisTotalSpending()).thenReturn(BTCCurrency.MAX_SATOSHI);

        FundingRunnable.FundedHolder result = runner.evaluateFundingUTXOs(mockFundingUtxos);

        assertThat(result.getErrorReason(), equalTo(fundingError));
        assertThat(result.getSatoshisFee(), equalTo(0l));
        verify(resources).getString(R.string.pay_not_enough_funds_error);
    }

    private List<TargetStat> buildTargetStats() {
        TargetStat stat1 = mock(TargetStat.class);
        Address address1 = mock(Address.class);
        TransactionSummary previousTransaction = mock(TransactionSummary.class);

        when(stat1.getAddress()).thenReturn(address1);
        when(stat1.getTransaction()).thenReturn(previousTransaction);

        when(stat1.getFundingId()).thenReturn(33L);
        when(stat1.getAddress()).thenReturn(address1);
        when(stat1.getAddr()).thenReturn("address1");
        when(stat1.getValue()).thenReturn(500L);
        when(address1.getChangeIndex()).thenReturn(0);
        when(address1.getIndex()).thenReturn(52);
        when(stat1.getPosition()).thenReturn(12);
        when(previousTransaction.getNumConfirmations()).thenReturn(3);


        List<TargetStat> list = new ArrayList<>();
        list.add(stat1);

        return list;
    }

}