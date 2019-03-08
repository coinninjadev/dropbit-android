package com.coinninja.coinkeeper.ui.transaction.details;

import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;

import dagger.Module;
import dagger.Provides;

@Module
public class TransactionDetailModule {

    @Provides
    TransactionDetailPageAdapter provideTransactionDetailPageAdapter(WalletHelper walletHelper, TransactionAdapterUtil transactionAdapterUtil) {
        return new TransactionDetailPageAdapter(walletHelper, transactionAdapterUtil);

    }
}
