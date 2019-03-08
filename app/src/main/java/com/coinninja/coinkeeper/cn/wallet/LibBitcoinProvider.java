package com.coinninja.coinkeeper.cn.wallet;

import com.coinninja.bindings.Libbitcoin;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;

import javax.inject.Inject;

@CoinkeeperApplicationScope
public class LibBitcoinProvider {
    private WalletHelper walletHelper;

    @Inject
    LibBitcoinProvider(WalletHelper walletHelper) {
        this.walletHelper = walletHelper;
    }

    public Libbitcoin provide() {
        String[] seedWords = walletHelper.getSeedWords();
        if (seedWords == null || seedWords.length < 12) {
            return new Libbitcoin();
        }

        return new Libbitcoin(seedWords);
    }
}
