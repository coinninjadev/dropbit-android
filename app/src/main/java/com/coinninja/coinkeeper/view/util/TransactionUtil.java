package com.coinninja.coinkeeper.view.util;

import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;

public class TransactionUtil {
    public static final int SPENDABLE_COUNT = 1;


    public static boolean isTargetNotSpendable(TargetStat target) {
        int numConfirmations = target.getTransaction().getNumConfirmations();
        return tooFewConfirmations(target, numConfirmations) || failedToBroadcast(target.getTransaction());
    }

    public static boolean failedToBroadcast(TransactionSummary transactionSummary) {
        return transactionSummary.getMemPoolState() == MemPoolState.FAILED_TO_BROADCAST;
    }

    private static boolean tooFewConfirmations(TargetStat target, int numConfirmations) {
        return (numConfirmations < SPENDABLE_COUNT) && (target.getAddress().getChangeIndex() == 0);
    }

}
