package com.coinninja.coinkeeper.service.tasks;

import com.coinninja.coinkeeper.model.FundedCallback;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateFundingUTXOsTaskTest {

    @Mock
    private FundedCallback callback;

    @Mock
    private TargetStatHelper targetStatHelper;

    @Mock
    private PaymentHolder paymentHolder;

    @Mock
    private FundingUTXOs.Builder fundingUTXOsBuilder;

    @Mock
    private FundingUTXOs fundingUTXOs;

    private CreateFundingUTXOsTask task;
    private List<TargetStat> spendableTargets;

    @Before
    public void setUp() {
        spendableTargets = new ArrayList<>();

        task = new CreateFundingUTXOsTask(fundingUTXOsBuilder, targetStatHelper, paymentHolder, callback);
        when(targetStatHelper.getSpendableTargets()).thenReturn(spendableTargets);
        when(paymentHolder.getBtcCurrency()).thenReturn(new BTCCurrency(100L));
        when(fundingUTXOsBuilder.build()).thenReturn(fundingUTXOs);
    }

    @Test
    public void complete_forwards_funding_utxo() {
        task.onPostExecute(fundingUTXOs);

        verify(callback).onComplete(fundingUTXOs);
    }

    @Test
    public void returns_funding_utxo() {
        assertThat(task.doInBackground(), equalTo(fundingUTXOs));
    }

    @Test
    public void setsPaymentInSatoshis() {
        task.doInBackground();

        verify(fundingUTXOsBuilder).setSatoshisSpending(100L);
    }

    @Test
    public void setsFees() {
        task.doInBackground();

        verify(fundingUTXOsBuilder).setTransactionFee(paymentHolder.getTransactionFee());
    }

    @Test
    public void prepairs_builder_for_funding_check() {

        task.doInBackground();

        verify(fundingUTXOsBuilder).setUsableTargets(spendableTargets);
    }

    @Test
    public void can_get_task_statically() {
        assertThat(CreateFundingUTXOsTask.newInstance(fundingUTXOsBuilder, targetStatHelper, paymentHolder, callback),
                instanceOf(CreateFundingUTXOsTask.class));


    }
}