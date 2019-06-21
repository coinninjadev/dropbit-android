package com.coinninja.coinkeeper.cn.wallet.tx;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionFundingManagerTest {
    private final int index = 25;
    private final long transactionDust = 500;

    @Mock
    private AccountManager accountManager;
    @Mock
    private TargetStatHelper targetStatHelper;
    @Mock
    private InviteTransactionSummaryHelper inviteTransactionSummaryHelper;
    @Mock
    private HDWallet hdWallet;
    private List<TargetStat> targetStats = new ArrayList<>();
    private List<InviteTransactionSummary> inviteTransactionSummaries = new ArrayList<>();
    private List<UnspentTransactionOutput> unspentTransactionOutputs = new ArrayList<>();
    private DerivationPath expectedChangePath = new DerivationPath(49, 0, 0, HDWallet.INTERNAL, index);
    private TransactionFee transactionFees = new TransactionFee(5, 10, 15);
    private TransactionFundingManager transactionFundingManager;

    @Before
    public void setUp() {
        transactionFundingManager = new TransactionFundingManager(hdWallet, transactionDust, accountManager, targetStatHelper, inviteTransactionSummaryHelper);
        when(targetStatHelper.getSpendableTargets()).thenReturn(targetStats);
        when(inviteTransactionSummaryHelper.getUnfulfilledSentInvites()).thenReturn(inviteTransactionSummaries);
        when(hdWallet.calcMinMinerFee(any())).thenCallRealMethod();
        when(hdWallet.getFeeInSatoshis(eq(transactionFees), anyInt())).thenCallRealMethod();
        when(accountManager.getNextChangeIndex()).thenReturn(index);
    }

    @After
    public void tearDown() {
        targetStats.clear();
        accountManager = null;
        expectedChangePath = null;
        inviteTransactionSummaries.clear();
        unspentTransactionOutputs.clear();
        unspentTransactionOutputs = null;
        inviteTransactionSummaries = null;
        targetStats = null;
        targetStatHelper = null;
        hdWallet = null;
        transactionFees = null;
        inviteTransactionSummaryHelper = null;
    }

    @Test
    public void creates_exact_with_fee_and_amount_provided() {
        mockTargets(9999L, 50000L, 100000L);

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionDataForDropBit(90000L, 500L);

        assertThat(transactionData.getAmount(), equalTo(90000L));
        assertThat(transactionData.getFeeAmount(), equalTo(500L));
        assertThat(transactionData.getChangeAmount(), equalTo(69499L));
        assertThat(transactionData.getUtxos(), equalTo(unspentTransactionOutputs.toArray()));
    }

    @Test
    public void uses_wallet_balance__send_max() {
        mockTargets(9999L, 50000L, 100000L);
        long expectedFee = hdWallet.getFeeInSatoshis(transactionFees, 4);
        long amountToSend = 9999 + 50000 + 100000 - expectedFee;

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees);

        assertThat(transactionData.getAmount(), equalTo(amountToSend));
        assertThat(transactionData.getFeeAmount(), equalTo(expectedFee));
        assertThat(transactionData.getChangeAmount(), equalTo(0L));
        assertThat(transactionData.getUtxos(), equalTo(unspentTransactionOutputs.toArray()));
    }

    @Test
    public void sending_max_reserves_prior_commitments() {
        mockPendingDropbit(10000, 500);
        mockTargets(9999L, 50000L, 100000L);
        long expectedFee = hdWallet.getFeeInSatoshis(transactionFees, 5);
        long expectedChange = 10000 + 500;
        long amountToSend = 9999 + 50000 + 100000 - expectedFee - expectedChange;

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees);

        assertThat(transactionData.getAmount(), equalTo(amountToSend));
        assertThat(transactionData.getFeeAmount(), equalTo(expectedFee));
        assertThat(transactionData.getChangeAmount(), equalTo(expectedChange));
        assertThat(transactionData.getUtxos(), equalTo(unspentTransactionOutputs.toArray()));
    }

    @Test
    public void sets_zero_for_sending_amounts_when_requested_amount_exceeds_available_balance() {
        mockTargets(9999L, 50000L, 100000L);

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees, 1600000L);

        assertThat(transactionData.getAmount(), equalTo(0L));
        assertThat(transactionData.getChangeAmount(), equalTo(0L));
        assertThat(transactionData.getFeeAmount(), equalTo(0L));
        assertThat(transactionData.getChangePath(), equalTo(expectedChangePath));
        assertThat(transactionData.getUtxos(), equalTo(new UnspentTransactionOutput[0]));
    }

    @Test
    public void sets_zero_for_sending_amounts_when_requested_amount_exceeds_available_balance__fees_push_it_over() {
        long input1 = 9000;
        long input2 = 19000;
        long input3 = 500;

        long expectedFee = hdWallet.getFeeInSatoshis(transactionFees, 4);
        long amountToSend = input1 + input2 + input3 - expectedFee + 100L;
        mockTargets(input1, input2, input3);

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees, amountToSend);

        assertThat(transactionData.getAmount(), equalTo(0L));
        assertThat(transactionData.getChangeAmount(), equalTo(0L));
        assertThat(transactionData.getFeeAmount(), equalTo(0L));
        assertThat(transactionData.getChangePath(), equalTo(expectedChangePath));
        assertThat(transactionData.getUtxos(), equalTo(new UnspentTransactionOutput[0]));
    }

    @Test
    public void pending_dropbits_factor_into_spendable_of_funds() {
        long input1 = 9000;
        long input2 = 19000;
        long input3 = 1000;

        long expectedFee = hdWallet.getFeeInSatoshis(transactionFees, 4);
        long amountToSend = input1 + input2 - expectedFee;
        mockTargets(input1, input2, input3);
        mockPendingDropbit(input3, 100);

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees, amountToSend);

        assertThat(transactionData.getAmount(), equalTo(0L));
        assertThat(transactionData.getChangeAmount(), equalTo(0L));
        assertThat(transactionData.getFeeAmount(), equalTo(0L));
        assertThat(transactionData.getChangePath(), equalTo(expectedChangePath));
        assertThat(transactionData.getUtxos(), equalTo(new UnspentTransactionOutput[0]));
    }

    @Test
    public void sets_amount_for_sending_amount_when_requested_amount_exceeds_available_balance() {
        mockTargets(9999L, 50000L, 100000L);

        long amountToSend = 55000L;
        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees, amountToSend);

        assertThat(transactionData.getAmount(), equalTo(amountToSend));
    }

    @Test
    public void calculates_fees_when_generating_inputs() {
        mockTargets(9999L, 50000L, 100000L);
        unspentTransactionOutputs.remove(unspentTransactionOutputs.get(2));

        long amountToSend = 55000L;
        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees, amountToSend);

        long feeInSatoshis = hdWallet.getFeeInSatoshis(transactionFees, 4);
        assertThat(transactionData.getAmount(), equalTo(amountToSend));
        assertThat(transactionData.getFeeAmount(), equalTo(feeInSatoshis));
        assertThat(transactionData.getChangeAmount(), equalTo(9999L + 50000L - feeInSatoshis - amountToSend));
        assertThat(transactionData.getUtxos(), equalTo(unspentTransactionOutputs.toArray()));
    }

    @Test
    public void calculates_fees_when_generating_inputs__with_dust_to_minor() {
        long valueOfFirstInput = 9000;
        long dust = 100;
        long expectedFee = hdWallet.getFeeInSatoshis(transactionFees, 2);
        long amountToSend = valueOfFirstInput - dust - expectedFee;
        mockTargets(valueOfFirstInput, 50000L, 100000L);
        unspentTransactionOutputs.remove(unspentTransactionOutputs.get(2));
        unspentTransactionOutputs.remove(unspentTransactionOutputs.get(1));

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees, amountToSend);

        assertThat(transactionData.getAmount(), equalTo(amountToSend));
        assertThat(transactionData.getFeeAmount(), equalTo(expectedFee + dust));
        assertThat(transactionData.getChangeAmount(), equalTo(0L));
        assertThat(transactionData.getUtxos(), equalTo(unspentTransactionOutputs.toArray()));
    }

    @Test
    public void calculates_fees_when_generating_inputs__two_inputs_with_dust_to_minor() {
        long valueOfFirstInput = 9000;
        long valueOfSecondInput = 50000L;
        long dust = 100;
        long expectedFee = hdWallet.getFeeInSatoshis(transactionFees, 3);
        long amountToSend = valueOfFirstInput + valueOfSecondInput - dust - expectedFee;
        mockTargets(valueOfFirstInput, valueOfSecondInput, 100000L);
        unspentTransactionOutputs.remove(unspentTransactionOutputs.get(2));

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees, amountToSend);

        assertThat(transactionData.getAmount(), equalTo(amountToSend));
        assertThat(transactionData.getFeeAmount(), equalTo(expectedFee + dust));
        assertThat(transactionData.getChangeAmount(), equalTo(0L));
        assertThat(transactionData.getUtxos(), equalTo(unspentTransactionOutputs.toArray()));
    }

    @Test
    public void calculates_fees_when_generating_inputs__with_one_input_exact_funds() {
        long valueOfFirstInput = 9000;
        long expectedFee = hdWallet.getFeeInSatoshis(transactionFees, 2);
        long amountToSend = valueOfFirstInput - expectedFee;
        mockTargets(valueOfFirstInput, 50000L, 100000L);
        unspentTransactionOutputs.remove(unspentTransactionOutputs.get(2));
        unspentTransactionOutputs.remove(unspentTransactionOutputs.get(1));

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees, amountToSend);

        assertThat(transactionData.getAmount(), equalTo(amountToSend));
        assertThat(transactionData.getFeeAmount(), equalTo(expectedFee));
        assertThat(transactionData.getChangeAmount(), equalTo(0L));
        assertThat(transactionData.getUtxos(), equalTo(unspentTransactionOutputs.toArray()));
    }

    @Test
    public void calculates_fees_when_generating_inputs__with_two_input_exact_funds() {
        long valueOfFirstInput = 9000;
        long valueOfSecondInput = 19000;
        long expectedFee = hdWallet.getFeeInSatoshis(transactionFees, 3);
        long amountToSend = valueOfFirstInput + valueOfSecondInput - expectedFee;
        mockTargets(valueOfFirstInput, valueOfSecondInput, 100000L);
        unspentTransactionOutputs.remove(unspentTransactionOutputs.get(2));

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees, amountToSend);

        assertThat(transactionData.getAmount(), equalTo(amountToSend));
        assertThat(transactionData.getFeeAmount(), equalTo(expectedFee));
        assertThat(transactionData.getChangeAmount(), equalTo(0L));
        assertThat(transactionData.getUtxos(), equalTo(unspentTransactionOutputs.toArray()));
    }

    @Test
    public void gives_change_to_miner_when_cost_to_keep_change_is_not_equitable() {
        long valueOfFirstInput = 2900;
        long valueOfSecondInput = 19000;
        long expectedFee = 1000;
        long amountToSend = 1000;
        mockTargets(valueOfFirstInput, valueOfSecondInput, 100000L);
        unspentTransactionOutputs.remove(unspentTransactionOutputs.get(2));
        unspentTransactionOutputs.remove(unspentTransactionOutputs.get(1));

        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFees, amountToSend);

        assertThat(transactionData.getAmount(), equalTo(amountToSend));
        assertThat(transactionData.getFeeAmount(), equalTo(expectedFee + 900));
        assertThat(transactionData.getChangeAmount(), equalTo(0L));
        assertThat(transactionData.getUtxos(), equalTo(unspentTransactionOutputs.toArray()));
    }

    @Test
    public void no_fees_generate_no_transaction() {
        verifyEmpty(transactionFundingManager.buildFundedTransactionData(null));
        verifyEmpty(transactionFundingManager.buildFundedTransactionData(null, 100L));
    }

    private void verifyEmpty(TransactionData data) {
        assertThat(data.getAmount(), equalTo(0L));
        assertThat(data.getFeeAmount(), equalTo(0L));
        assertThat(data.getChangeAmount(), equalTo(0L));
        assertThat(data.getUtxos().length, equalTo(0));
    }

    private void mockPendingDropbit(long value, long fee) {
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        when(invite.getValueSatoshis()).thenReturn(value);
        when(invite.getValueFeesSatoshis()).thenReturn(fee);
        inviteTransactionSummaries.add(invite);
    }

    private void mockTargets(long... values) {
        for (long value : values) {
            TargetStat stat = mock(TargetStat.class);
            UnspentTransactionOutput unspentTransactionOutput = mock(UnspentTransactionOutput.class);
            when(unspentTransactionOutput.getAmount()).thenReturn(value);
            when(stat.getValue()).thenReturn(value);
            when(stat.toUnspentTranasactionOutput()).thenReturn(unspentTransactionOutput);
            targetStats.add(stat);
            unspentTransactionOutputs.add(unspentTransactionOutput);
        }
    }

}