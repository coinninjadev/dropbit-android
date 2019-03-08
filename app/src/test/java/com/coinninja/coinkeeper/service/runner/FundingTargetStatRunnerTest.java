package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.UnspentTransactionHolder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(FundingUTXOs.Builder.class)
@RunWith(PowerMockRunner.class)
public class FundingTargetStatRunnerTest {
    private FundingRunnable fundingRunnable;

    private FundingTargetStatRunner fundingTargetStatRunner;
    private FundingTargetStatRunner.FundingTargetStatListener mockListener;


    @Before
    public void setUp() throws Exception {
        fundingRunnable = mock(FundingRunnable.class);
        mockListener = mock(FundingTargetStatRunner.FundingTargetStatListener.class);


        fundingTargetStatRunner = new FundingTargetStatRunner(fundingRunnable);
        fundingTargetStatRunner.setListener(mockListener);
    }

    @Test
    public void funding_async_execute_test() {
        Long satoshisTotalSpend = 4378390843l;
        FundingUTXOs fundingUTXOsMock = mock(FundingUTXOs.class);
        when(fundingRunnable.fundRun(satoshisTotalSpend, fundingTargetStatRunner)).thenReturn(fundingUTXOsMock);

        FundingUTXOs fundingUTXOs = fundingTargetStatRunner.doInBackground(satoshisTotalSpend);

        assertThat(fundingUTXOs, equalTo(fundingUTXOsMock));
    }

    @Test
    public void funding_async_execute_bad_data_test() {
        Long satoshisTotalSpend = 4378390843l;
        when(fundingRunnable.fundRun(satoshisTotalSpend, fundingTargetStatRunner)).thenReturn(null);

        FundingUTXOs fundingUTXOs = fundingTargetStatRunner.doInBackground(satoshisTotalSpend);

        assertThat(fundingUTXOs, nullValue());
    }

    @Test
    public void funding_async_evaluate_funding_good_data_test() {
        UnspentTransactionHolder unspent = mock(UnspentTransactionHolder.class);
        long fee = 500l;
        FundingUTXOs fundingUTXOs = mock(FundingUTXOs.class);
        when(fundingRunnable.evaluateFundingUTXOs(fundingUTXOs)).thenReturn(new FundingRunnable.FundedHolder(unspent, fee));


        fundingTargetStatRunner.onPostExecute(fundingUTXOs);


        verify(mockListener).onFundingSuccessful(unspent, fee);
    }

    @Test
    public void funding_async_evaluate_funding_bad_data_test() {
        FundingUTXOs fundingUTXOs = mock(FundingUTXOs.class);
        when(fundingRunnable.evaluateFundingUTXOs(fundingUTXOs)).thenReturn(new FundingRunnable.FundedHolder("SOME - ERORR", 0l));


        fundingTargetStatRunner.onPostExecute(fundingUTXOs);


        verify(mockListener).onFundingError(eq("SOME - ERORR"), eq(0l));
    }

}