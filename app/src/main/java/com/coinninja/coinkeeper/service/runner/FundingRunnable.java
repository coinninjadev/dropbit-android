package com.coinninja.coinkeeper.service.runner;

import android.content.Context;
import android.content.res.Resources;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.UnspentTransactionHolder;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FundingRunnable {
    private final HDWallet hDWallet;
    private final DaoSessionManager daoSessionManager;
    private final Resources resources;

    private String paymentAddress;
    private int currentChangeAddressIndex;
    private TransactionFee transactionFee;
    private Currency evaluationCurrency;


    @Inject
    public FundingRunnable(@ApplicationContext Context context, HDWallet hDWallet, DaoSessionManager daoSessionManager) {
        this.hDWallet = hDWallet;
        this.daoSessionManager = daoSessionManager;
        resources = context.getResources();
    }

    public FundingUTXOs fundRun(long satoshisRequestingToSpend, FundingUTXOs.ProgressListener progressListener) {
        return fundRun(satoshisRequestingToSpend, -1, progressListener);
    }

    public FundingUTXOs fundRun(long satoshisRequestingToSpend, long desiredFee, FundingUTXOs.ProgressListener progressListener) {
        List<TargetStat> usableTargets =
                daoSessionManager.getTargetStatDao().queryBuilder().where(
                        TargetStatDao.Properties.AddressId.isNotNull(),
                        TargetStatDao.Properties.FundingId.isNull()).orderAsc(TargetStatDao.Properties.TxTime).list();

        if (usableTargets == null) {
            return null;
        }


        FundingUTXOs fundingUTXOs = new FundingUTXOs.Builder(hDWallet)
                .setUsableTargets(usableTargets)
                .setSatoshisSpending(satoshisRequestingToSpend)
                .setTransactionFee(transactionFee)
                .setSatoshisDesiredFeeAmount(desiredFee)
                .setProgressListener(progressListener)
                .build();


        return fundingUTXOs;
    }


    public FundedHolder evaluateFundingUTXOs(FundingUTXOs fundingUTXOs) {
        // TODO: none of this validation should be used and rendered on payment confirmation screen
        if (fundingUTXOs == null) {
            return new FundedHolder(resources.getString(R.string.pay_not_enough_funds_error).toString(),
                    0l);
        }

        if (hasNegativeValue(fundingUTXOs) || hasTooLargeValue(fundingUTXOs)) {
            return new FundedHolder(resources.getString(R.string.pay_not_enough_funds_error).toString(),
                    0l);
        }

        long satoshisFundedTotal = fundingUTXOs.getSatoshisFundedTotal();
        long satoshisRequestingSpend = fundingUTXOs.getSatoshisSpending();
        long satoshisFeesSpend = fundingUTXOs.getSatoshisFeesSpending();
        long satoshisTotalSpending = fundingUTXOs.getSatoshisTotalSpending();//with fees
        ArrayList<UnspentTransactionOutput> usingUTXOs = fundingUTXOs.getUsingUTXOs();

        if (satoshisFundedTotal < satoshisTotalSpending) {
            BTCCurrency spendableBTC = new BTCCurrency(satoshisFundedTotal);

            StringBuilder error = new StringBuilder();

            error.append(resources.getString(R.string.pay_not_enough_funds_error));
            error.append("\n");
            error.append(resources.getString(R.string.pay_not_available_funds_error) + " " + spendableBTC.toFormattedCurrency());
            if (evaluationCurrency != null)
                error.append("  " + spendableBTC.toUSD(evaluationCurrency).toFormattedCurrency());


            return new FundedHolder(error.toString(), satoshisFeesSpend);
        }


        long satoshisChangeAmount = satoshisFundedTotal - satoshisTotalSpending;
        DerivationPath changePath = satoshisChangeAmount > 0 ?
                TransactionData.buildDerivationForChangeIndex(currentChangeAddressIndex) : null;

        UnspentTransactionOutput[] unspentTransactionOutput = usingUTXOs.toArray(new UnspentTransactionOutput[usingUTXOs.size()]);
        UnspentTransactionHolder spendableUTXOsUtxos = new UnspentTransactionHolder(satoshisFundedTotal, unspentTransactionOutput, satoshisRequestingSpend, satoshisFeesSpend, satoshisChangeAmount, changePath, paymentAddress);


        return new FundedHolder(spendableUTXOsUtxos, satoshisFeesSpend);
    }

    private boolean hasNegativeValue(FundingUTXOs fundingUTXOs) {
        return fundingUTXOs.getSatoshisFundedTotal() < 0 ||
                fundingUTXOs.getSatoshisFeesSpending() < 0 ||
                fundingUTXOs.getSatoshisSpending() < 0 ||
                fundingUTXOs.getSatoshisTotalSpending() < 0;
    }

    private boolean hasTooLargeValue(FundingUTXOs fundingUTXOs) {
        long max = BTCCurrency.MAX_SATOSHI;
        return fundingUTXOs.getSatoshisFundedTotal() >= max ||
                fundingUTXOs.getSatoshisFeesSpending() >= max ||
                fundingUTXOs.getSatoshisSpending() >= max ||
                fundingUTXOs.getSatoshisTotalSpending() >= max;
    }

    public void setPaymentAddress(String paymentAddress) {
        this.paymentAddress = paymentAddress;
    }

    public void setCurrentChangeAddressIndex(int currentChangeAddressIndex) {
        this.currentChangeAddressIndex = currentChangeAddressIndex;
    }

    public void setTransactionFee(TransactionFee transactionFee) {
        this.transactionFee = transactionFee;
    }

    public void setEvaluationCurrency(Currency evaluationCurrency) {
        this.evaluationCurrency = evaluationCurrency;
    }

    public static class FundedHolder {
        final private UnspentTransactionHolder unspentTransactionHolder;
        final private long satoshisFee;
        final private String errorReason;

        public FundedHolder(UnspentTransactionHolder unspentTransactionHolder, long satoshisFee) {
            this.unspentTransactionHolder = unspentTransactionHolder;
            this.satoshisFee = satoshisFee;
            errorReason = "";
        }

        public FundedHolder(String errorReason, long satoshisFee) {
            this.errorReason = errorReason;
            this.satoshisFee = satoshisFee;
            unspentTransactionHolder = null;
        }

        public UnspentTransactionHolder getUnspentTransactionHolder() {
            return unspentTransactionHolder;
        }

        public long getSatoshisFee() {
            return satoshisFee;
        }

        public String getErrorReason() {
            return errorReason;
        }
    }
}
