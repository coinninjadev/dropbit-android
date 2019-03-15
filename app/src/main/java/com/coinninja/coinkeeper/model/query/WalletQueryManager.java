package com.coinninja.coinkeeper.model.query;

import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class WalletQueryManager {
    private final DaoSessionManager daoSessionManager;

    @Inject
    WalletQueryManager(DaoSessionManager daoSessionManager) {
        this.daoSessionManager = daoSessionManager;
    }

    @Nullable
    public Wallet getWallet() {
        return daoSessionManager.qeuryForWallet().orderAsc().limit(1).unique();
    }
}
