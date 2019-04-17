package com.coinninja.coinkeeper.cn.wallet.tx;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.di.interfaces.TransactionDust;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class TransactionFundingManager {
    private final HDWallet hdWallet;
    private final long valueOfDust;
    private final AccountManager accountManager;
    private final TargetStatHelper targetStatHelper;
    private final InviteTransactionSummaryHelper inviteTransactionSummaryHelper;
    private List<UnspentTransactionOutput> unspentTransactionOutputs;

    @Inject
    TransactionFundingManager(HDWallet hdWallet, @TransactionDust long valueOfDust, AccountManager accountManager,
                              TargetStatHelper targetStatHelper, InviteTransactionSummaryHelper inviteTransactionSummaryHelper) {

        this.hdWallet = hdWallet;
        this.valueOfDust = valueOfDust;
        this.accountManager = accountManager;
        this.targetStatHelper = targetStatHelper;
        this.inviteTransactionSummaryHelper = inviteTransactionSummaryHelper;
    }

    public TransactionData buildFundedTransactionData(TransactionFee transactionFee) {
        TransactionData transactionData = createTransactionData();
        if (transactionFee == null) return transactionData;

        long fees = 0;
        long changeAmount = 0;
        long walletValue = calculateSpendableBalance();
        long amountSending = 0;

        if (walletValue > 0) {
            fees = buildInputsWithFees(transactionFee, walletValue);
            amountSending = walletValue - fees;
            changeAmount = calculateChange(walletValue);
        }

        if (amountSending > 0) {
            transactionData.setAmount(amountSending);
            transactionData.setFeeAmount(fees);
            transactionData.setChangeAmount(changeAmount);
            transactionData.setUtxos(getFundingInputs());
        }

        return transactionData;
    }

    public TransactionData buildFundedTransactionData(TransactionFee transactionFee, long amountToSend) {
        TransactionData transactionData = createTransactionData();
        if (transactionFee == null) return transactionData;

        long fees = 0;
        long changeAmount = 0;
        long walletValue = calculateSpendableBalance();

        if (walletValue > amountToSend) {
            fees = buildInputsWithFees(transactionFee, amountToSend);
            changeAmount = calculateChange(amountToSend + fees);
        }

        if (walletValue >= amountToSend + fees) {
            transactionData.setAmount(amountToSend);
            transactionData.setFeeAmount(fees);
            transactionData.setChangeAmount(changeAmount);
            transactionData.setUtxos(getFundingInputs());
        }

        return transactionData;
    }

    public TransactionData buildFundedTransactionDataForDropBit(long amountToSend, long explicitFee) {
        TransactionData transactionData = createTransactionData();
        long changeAmount = 0;
        long walletValue = calculateValueOfWallet();
        long totalTransactionCost = amountToSend + explicitFee;

        if (walletValue > 0) {
            changeAmount = calculateChange(totalTransactionCost);
        }

        if (walletValue >= totalTransactionCost) {
            transactionData.setAmount(amountToSend);
            transactionData.setFeeAmount(explicitFee);
            transactionData.setChangeAmount(changeAmount);
            transactionData.setUtxos(getFundingInputs());
        }

        return transactionData;
    }

    protected long calculateSpendableBalance() {
        return calculateValueOfWallet() + calculateValueOfPendingDropbits();
    }

    protected long buildInputsWithFees(TransactionFee transactionFee, long amountToSend) {
        long transactionFeePerInput = calculateFeePerInput(transactionFee);
        List<UnspentTransactionOutput> requiredInputs = new ArrayList<>();
        int inputCount = 0;
        int outputCount = 1;
        long inputsValue = 0L;
        long currentFee = 0L;
        long totalSendingValue;

        for (UnspentTransactionOutput unspentTransactionOutput : getFundingInputs()) {
            totalSendingValue = amountToSend + currentFee;

            if (totalSendingValue > inputsValue) {
                requiredInputs.add(unspentTransactionOutput);
                inputCount += 1;
                inputsValue += unspentTransactionOutput.getAmount();
                currentFee = transactionFeePerInput * (inputCount + outputCount);
                totalSendingValue = amountToSend + currentFee;

                long changeValue = inputsValue - totalSendingValue;
                if (changeValue > 0 && changeValue < transactionFeePerInput + getValueOfDust()) {
                    currentFee += changeValue;
                    break;
                } else if (changeValue > 0) {
                    currentFee += transactionFeePerInput;
                    break;
                }
            }
        }
        setRequiredInputs(requiredInputs);
        return currentFee;
    }

    protected UnspentTransactionOutput[] getFundingInputs() {
        UnspentTransactionOutput[] outputs = new UnspentTransactionOutput[unspentTransactionOutputs.size()];
        return unspentTransactionOutputs.toArray(outputs);
    }

    protected long calculateFeePerInput(TransactionFee transactionFee) {
        return hdWallet.getFeeInSatoshis(transactionFee, 1);
    }


    protected long calculateValueOfWallet() {
        long value = 0L;
        for (TargetStat stat : targetStatHelper.getSpendableTargets()) {
            value += stat.getValue();
            unspentTransactionOutputs.add(stat.toUnspentTranasactionOutput());
        }
        return value;
    }

    protected long calculateValueOfPendingDropbits() {
        long value = 0L;
        for (InviteTransactionSummary dropbit : inviteTransactionSummaryHelper.getUnfulfilledSentInvites()) {
            value -= dropbit.getValueFeesSatoshis();
            value -= dropbit.getValueSatoshis();
        }
        return value;
    }

    @NotNull
    protected TransactionData createTransactionData() {
        unspentTransactionOutputs = new ArrayList<>();
        return new TransactionData(new UnspentTransactionOutput[0], 0, 0, 0,
                nextChangePath(), null);
    }

    protected DerivationPath nextChangePath() {
        Address address = new Address();
        address.setIndex(accountManager.getNextChangeIndex());
        address.setChangeIndex(HDWallet.INTERNAL);
        return address.getDerivationPath();
    }

    protected long calculateChange(long totalTransactionCost) {
        long value = 0L;
        for (UnspentTransactionOutput unspentTransactionOutput : unspentTransactionOutputs) {
            value += unspentTransactionOutput.getAmount();
        }

        return value - totalTransactionCost;
    }

    protected long getValueOfDust() {
        return valueOfDust;
    }

    protected void setRequiredInputs(List<UnspentTransactionOutput> requiredInputs) {
        unspentTransactionOutputs.clear();
        unspentTransactionOutputs.addAll(requiredInputs);
    }
}
