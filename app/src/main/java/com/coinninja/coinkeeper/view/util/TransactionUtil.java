package com.coinninja.coinkeeper.view.util;

import com.coinninja.coinkeeper.BuildConfig;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;

public class TransactionUtil {
    public static final int SPENDABLE_COUNT = 1;
    public static final int REQUIRED_CONFIRMATIONS = 3;


    public static boolean IS_TARGETSTAT_NOT_SPENDABLE(TargetStat target) {
        int numConfirmations = target.getTransaction().getNumConfirmations();
        return tooFewConfirmations(target, numConfirmations) || FAILED_TO_BROADCAST(target.getTransaction());
    }

    public static boolean FAILED_TO_BROADCAST(TransactionSummary transactionSummary) {
        return transactionSummary.getMemPoolState() == MemPoolState.FAILED_TO_BROADCAST;
    }

    private static boolean tooFewConfirmations(TargetStat target, int numConfirmations) {
        return (numConfirmations < SPENDABLE_COUNT) && (target.getAddress().getChangeIndex() == 0);
    }

    public static boolean IS_REPLACEABLE(TransactionSummary transactionSummary) {
        return transactionSummary.getNumConfirmations() < REQUIRED_CONFIRMATIONS;
    }

    public static boolean IS_AMOUNT_DUSTY(long amount) {
        long DUST_MAX = BuildConfig.DUST_AMOUNT_SATOSHIS;
        return amount <= DUST_MAX;
    }
}
