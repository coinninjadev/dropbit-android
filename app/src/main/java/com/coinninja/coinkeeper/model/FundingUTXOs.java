package com.coinninja.coinkeeper.model;

import android.util.Log;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.coinninja.coinkeeper.view.util.TransactionUtil.IS_AMOUNT_DUSTY;
import static com.coinninja.coinkeeper.view.util.TransactionUtil.IS_REPLACEABLE;
import static com.coinninja.coinkeeper.view.util.TransactionUtil.IS_TARGETSTAT_NOT_SPENDABLE;

public class FundingUTXOs {
    private final ArrayList<UnspentTransactionOutput> usingUTXOs;
    private final long satoshisFundedTotal;
    private final long satoshisSpending;
    private final long satoshisFeesSpending;
    private final long satoshisDustGiveToMiner;


    private FundingUTXOs(ArrayList<UnspentTransactionOutput> usingUTXOs, long satoshisFundedTotal, long satoshisSpending, long satoshisFeesSpending, long satoshisDustGiveToMiner) {
        this.usingUTXOs = usingUTXOs;
        this.satoshisFundedTotal = satoshisFundedTotal;
        this.satoshisSpending = satoshisSpending;
        this.satoshisFeesSpending = satoshisFeesSpending;
        this.satoshisDustGiveToMiner = satoshisDustGiveToMiner;
    }

    public long getSatoshisFundedTotal() {
        return satoshisFundedTotal;
    }

    public long getSatoshisTotalSpending() {
        return getSatoshisSpending() + getSatoshisFeesSpending();
    }

    public ArrayList<UnspentTransactionOutput> getUsingUTXOs() {
        return usingUTXOs;
    }

    public long getSatoshisSpending() {
        return satoshisSpending;
    }

    public long getSatoshisFeesSpending() {
        return satoshisFeesSpending + satoshisDustGiveToMiner;
    }

    protected long getRAWSatoshisDustGiveToMiner() {
        return satoshisDustGiveToMiner;
    }

    protected long getRAWSatoshisFeesSpending() {
        return satoshisFeesSpending;
    }

    public static class Builder {
        private static final String TAG = Builder.class.getSimpleName();

        private List<TargetStat> usableTargets;
        private long satoshisRequestingSpend;
        private HDWallet hdWallet;
        private TransactionFee transactionFee;
        private ProgressListener progressListener;

        private int numOfInputsWantedToUsed;
        private int numOfOutputsWantedToUsed;
        private int totalWantedNumInOuts;

        private long satoshisFeeAmount;
        private long satoshisTotalSpendingAmount;
        private long satoshisDesiredFeeAmount;

        @Inject
        public Builder(HDWallet hdWallet) {
            this.hdWallet = hdWallet;
        }

        public Builder setUsableTargets(List<TargetStat> usableTargets) {
            this.usableTargets = usableTargets;
            return this;
        }

        public Builder setSatoshisSpending(long satoshisRequestingSpend) {
            this.satoshisRequestingSpend = satoshisRequestingSpend;
            return this;
        }

        public Builder setSatoshisDesiredFeeAmount(long satoshisDesiredFeeAmount) {
            this.satoshisDesiredFeeAmount = satoshisDesiredFeeAmount;
            return this;
        }

        public Builder setTransactionFee(TransactionFee transactionFee) {
            this.transactionFee = transactionFee;
            return this;
        }

        public Builder setProgressListener(ProgressListener progressListener) {
            this.progressListener = progressListener;
            return this;
        }

        public FundingUTXOs build() {
            return runFundingAlgorithm();
        }

        private FundingUTXOs runFundingAlgorithm() {

            long satoshisTotalUnspentInputs = 0l;
            int numOfGatheredInputs = 0;

            updateInOutFeeCount(1);


            logFeeInfo("first Estimate", numOfInputsWantedToUsed, numOfOutputsWantedToUsed, totalWantedNumInOuts, transactionFee == null ? satoshisDesiredFeeAmount : transactionFee.getMin(), satoshisFeeAmount, satoshisRequestingSpend, satoshisTotalSpendingAmount, satoshisTotalUnspentInputs);

            ArrayList<UnspentTransactionOutput> usingUTXOs = new ArrayList<>();
            satoshisTotalUnspentInputs = gatherUsableUTXO(satoshisTotalSpendingAmount, usableTargets, usingUTXOs);
            numOfGatheredInputs = usingUTXOs.size();

            while (numOfGatheredInputs > numOfInputsWantedToUsed) {
                updateInOutFeeCount(numOfGatheredInputs);

                usingUTXOs = new ArrayList<>();
                satoshisTotalUnspentInputs = gatherUsableUTXO(satoshisTotalSpendingAmount, usableTargets, usingUTXOs);
                numOfGatheredInputs = usingUTXOs.size();
            }


            int finalNumOfInputsUsed = usingUTXOs.size();
            int finalNumOfOutputsUsed = numOfOutputsWantedToUsed;
            int finalTotalNumInOutsUsed = finalNumOfInputsUsed + finalNumOfOutputsUsed;

            long finalSatoshisRequestingSpend = satoshisRequestingSpend;
            long finalSatoshisFee = satoshisFeeAmount;
            long finalSatoshisTotalRequestingSpend = finalSatoshisRequestingSpend + finalSatoshisFee;

            long finalSatoshisTotalAllInputs = satoshisTotalUnspentInputs;

            long possibleChangeAmount = finalSatoshisTotalAllInputs - finalSatoshisTotalRequestingSpend;
            boolean hasChange = possibleChangeAmount > 0;
            long finalChangeAmount = hasChange ? possibleChangeAmount : 0;

            long finalDustGiveToMiner = IS_AMOUNT_DUSTY(finalChangeAmount) ? finalChangeAmount : 0;

            logFeeInfo("final Estimate", finalNumOfInputsUsed, finalNumOfOutputsUsed, finalTotalNumInOutsUsed, transactionFee == null ? satoshisDesiredFeeAmount : transactionFee.getMin(), finalSatoshisFee, finalSatoshisRequestingSpend, finalSatoshisTotalRequestingSpend, finalSatoshisTotalAllInputs);

            return new FundingUTXOs(
                    usingUTXOs,
                    finalSatoshisTotalAllInputs,
                    finalSatoshisRequestingSpend,
                    finalSatoshisFee,
                    finalDustGiveToMiner);
        }

        private void updateInOutFeeCount(int numOfInputs) {
            numOfInputsWantedToUsed = numOfInputs;
            numOfOutputsWantedToUsed = 2;
            totalWantedNumInOuts = numOfInputsWantedToUsed + numOfOutputsWantedToUsed;

            satoshisFeeAmount = calculateFees(totalWantedNumInOuts);
            satoshisTotalSpendingAmount = satoshisRequestingSpend + satoshisFeeAmount;
        }

        private long calculateFees(int totalNumInOuts) {
            if (satoshisDesiredFeeAmount > 0) {
                return satoshisDesiredFeeAmount;
            }
            return hdWallet.getFeeForTransaction(transactionFee, totalNumInOuts).toSatoshis();
        }

        private long gatherUsableUTXO(long satoshisTotalRequestingSpend, List<TargetStat> usableTargets,
                                      ArrayList<UnspentTransactionOutput> transactionOutputs) {
            long satoshisUnspentTotal = 0L;

            for (int i = 0; i < usableTargets.size(); i++) {
                TargetStat targetStat = usableTargets.get(i);
                if (IS_TARGETSTAT_NOT_SPENDABLE(targetStat)) {
                    continue;
                }
                updatePublishProgress(i, usableTargets);

                UnspentTransactionOutput utxo = targetStatToUtxo(targetStat);
                long previousTransSatoshiAmount = targetStat.getValue();

                transactionOutputs.add(utxo);
                satoshisUnspentTotal += previousTransSatoshiAmount;

                if (satoshisUnspentTotal >= satoshisTotalRequestingSpend) {
                    break;
                }
            }

            return satoshisUnspentTotal;
        }

        private UnspentTransactionOutput targetStatToUtxo(TargetStat targetStat) {
            Address address = targetStat.getAddress();
            TransactionSummary previousTransaction = targetStat.getTransaction();

            int changeDerivationPath = address.getChangeIndex();
            int walletAddressIndex = address.getIndex();
            String previousTransactionHASH = previousTransaction.getTxid();
            int previousTransactionIndex = targetStat.getPosition();
            long previousTransactionSatoshiAmount = targetStat.getValue();
            DerivationPath inputPath = UnspentTransactionHolder.BUILD_DERIVATION(changeDerivationPath, walletAddressIndex);
            return new UnspentTransactionOutput(previousTransactionHASH, previousTransactionIndex,
                    previousTransactionSatoshiAmount, inputPath, IS_REPLACEABLE(previousTransaction));
        }

        private void updatePublishProgress(int currentPosition, List<TargetStat> usableTargets) {
            //Progress = % = Value / Total * 100
            int listTotal = usableTargets.size();
            int percentage = Math.round((currentPosition * 100) / listTotal);
            if (progressListener != null) {
                progressListener.onProgressUpdate(percentage);
            }
        }


        private void logFeeInfo(String msg,
                                int numOfIns,
                                int numOfOuts,
                                int totalNumInOuts,
                                double minFee,
                                long satoshisFee,
                                long satoshisRequestingSpend,
                                long satoshisTotalRequestingSpend,
                                long satoshisUnspentTotal) {

            StringBuilder outFeeMessage = new StringBuilder();

            outFeeMessage.append("funding runner logs:");
            outFeeMessage.append("\n");
            outFeeMessage.append("--- " + msg);
            outFeeMessage.append("\n");
            outFeeMessage.append("--- user requesting to spend = " + satoshisRequestingSpend);
            outFeeMessage.append("\n");
            outFeeMessage.append("--- Number of inputs = " + numOfIns);
            outFeeMessage.append("\n");
            outFeeMessage.append("--- Number of outputs = " + numOfOuts);
            outFeeMessage.append("\n");
            outFeeMessage.append("--- total in's and out's = " + totalNumInOuts);
            outFeeMessage.append("\n");
            outFeeMessage.append("--- 100 * in's and out's = " + (totalNumInOuts * 100));
            outFeeMessage.append("\n");
            outFeeMessage.append("--- using Spending satoshis Unspent Total = " + satoshisUnspentTotal);
            outFeeMessage.append("\n");
            outFeeMessage.append("--- fastest Fee = " + minFee);
            outFeeMessage.append("\n");
            outFeeMessage.append("--- Spending Fee = " + satoshisFee);
            outFeeMessage.append("\n");
            outFeeMessage.append("--- Spending Fee + requesting to spend = " + satoshisTotalRequestingSpend);
            outFeeMessage.append("\n");

            Log.i(TAG, outFeeMessage.toString());
        }
    }

    public interface ProgressListener {
        void onProgressUpdate(int progress);
    }
}
