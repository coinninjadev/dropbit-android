package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.model.UnspentTransactionHolder;

public class UnspentTransactionExample {
    public static DerivationPath sampleDerivationPath1() {
        int purpose = 49;
        int coinType = 0;
        int account = 0;
        int change = 0;
        int index = 1;

        return new DerivationPath(purpose, coinType, account, change, index);
    }

    public static DerivationPath sampleDerivationPath2() {
        int purpose = 49;
        int coinType = 0;
        int account = 0;
        int change = 0;
        int index = 2;

        return new DerivationPath(purpose, coinType, account, change, index);
    }

    public static DerivationPath sampleChangeDerivationPath() {
        int purpose = 49;
        int coinType = 0;
        int account = 0;
        int change = 1;
        int index = 2;

        return new DerivationPath(purpose, coinType, account, change, index);
    }

    public static UnspentTransactionOutput sampleUnspentTransactionOutput1(DerivationPath derivationPath) {
        String txId = "--txid-funding-tx-sample-1";
        int index = 1;
        long amount = 2000L;

        return new UnspentTransactionOutput(txId, index, amount, derivationPath, false);
    }

    public static UnspentTransactionOutput sampleUnspentTransactionOutput2(DerivationPath derivationPath) {
        String txId = "--txid-funding-tx-sample-2";
        int index = 2;
        long amount = 1000L;

        return new UnspentTransactionOutput(txId, index, amount, derivationPath, true);
    }
    public static UnspentTransactionHolder build_holder() {
        DerivationPath sampleSpendableDerivationPath1 = sampleDerivationPath1();
        DerivationPath sampleSpendableDerivationPath2 = sampleDerivationPath2();
        UnspentTransactionOutput sampleUnspentTransactionOutput1 = sampleUnspentTransactionOutput1(sampleSpendableDerivationPath1);
        UnspentTransactionOutput sampleUnspentTransactionOutput2 = sampleUnspentTransactionOutput2(sampleSpendableDerivationPath2);

        long sampleSatoshisUnspentTotal = 3000L;
        UnspentTransactionOutput[] sampleUnspentTransOuts = new UnspentTransactionOutput[]{sampleUnspentTransactionOutput1, sampleUnspentTransactionOutput2};
        long sampleSatoshisRequestingToSpend = 2500L;
        long sampleSatoshisFeeAmount = 10L;
        long sampleSatoshisChangeAmount = 450L;
        DerivationPath sampleChangePath = sampleChangeDerivationPath();
        String samplePaymentAddress = "--send-to-address--";

        return new UnspentTransactionHolder(
                sampleSatoshisUnspentTotal,
                sampleUnspentTransOuts,
                sampleSatoshisRequestingToSpend,
                sampleSatoshisFeeAmount,
                sampleSatoshisChangeAmount,
                sampleChangePath,
                samplePaymentAddress);
    }

    public static UnspentTransactionHolder build_holder_no_change() {
        DerivationPath sampleSpendableDerivationPath1 = sampleDerivationPath1();
        DerivationPath sampleSpendableDerivationPath2 = sampleDerivationPath2();
        UnspentTransactionOutput sampleUnspentTransactionOutput1 = sampleUnspentTransactionOutput1(sampleSpendableDerivationPath1);
        UnspentTransactionOutput sampleUnspentTransactionOutput2 = sampleUnspentTransactionOutput2(sampleSpendableDerivationPath2);

        long sampleSatoshisUnspentTotal = 3000L;
        UnspentTransactionOutput[] sampleUnspentTransOuts = new UnspentTransactionOutput[]{sampleUnspentTransactionOutput1, sampleUnspentTransactionOutput2};
        long sampleSatoshisRequestingToSpend = 2990L;
        long sampleSatoshisFeeAmount = 10L;
        long sampleSatoshisChangeAmount = 0L;
        DerivationPath sampleChangePath = null;
        String samplePaymentAddress = null;

        return new UnspentTransactionHolder(
                sampleSatoshisUnspentTotal,
                sampleUnspentTransOuts,
                sampleSatoshisRequestingToSpend,
                sampleSatoshisFeeAmount,
                sampleSatoshisChangeAmount,
                sampleChangePath,
                samplePaymentAddress);
    }
}
