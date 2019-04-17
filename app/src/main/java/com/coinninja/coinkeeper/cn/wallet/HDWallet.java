package com.coinninja.coinkeeper.cn.wallet;

import com.coinninja.bindings.DecryptionKeys;
import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.EncryptionKeys;
import com.coinninja.bindings.Libbitcoin;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;

import javax.inject.Inject;

@CoinkeeperApplicationScope
public class HDWallet {

    public static final int EXTERNAL = 0;
    public static final int INTERNAL = 1;

    private final LibBitcoinProvider libBitcoinProvider;

    @Inject
    public HDWallet(LibBitcoinProvider libBitcoinProvider) {
        this.libBitcoinProvider = libBitcoinProvider;
    }

    public String[] fillBlock(int type, int startingIndex, int bufferSize) {
        Libbitcoin libbitcoin = libBitcoinProvider.provide();
        String[] block = new String[bufferSize];
        if (type == EXTERNAL) {
            for (int i = 0; i < block.length; i++, startingIndex++) {
                block[i] = libbitcoin.getExternalChangeAddress(startingIndex);
            }
        } else if (type == INTERNAL) {
            for (int i = 0; i < block.length; i++, startingIndex++) {
                block[i] = libbitcoin.getInternalChangeAddress(startingIndex);
            }
        }

        return block;
    }

    public String getUncompressedPublicKey(DerivationPath path) {
        return libBitcoinProvider.provide().getUncompressedPublicKeyHex(path);
    }

    public long getFeeInSatoshis(TransactionFee transactionFee, int numInsOuts) {
        int currentTransactionByteSize = numInsOuts * 100;
        double fee = calcMinMinerFee(transactionFee) * currentTransactionByteSize;
        return Math.round(fee);
    }

    public String getExternalAddress(int index) {
        return libBitcoinProvider.provide().getExternalChangeAddress(index);
    }

    public String getAddress(DerivationPath derivationPath) {
        switch (derivationPath.getChange()) {
            case EXTERNAL:
                return libBitcoinProvider.provide().getExternalChangeAddress(derivationPath.getIndex());
            case INTERNAL:
                return libBitcoinProvider.provide().getInternalChangeAddress(derivationPath.getIndex());
        }

        throw new RuntimeException("Unknown Change path");
    }

    public boolean isBase58CheckEncoded(String address) {
        return libBitcoinProvider.provide().isBase58CheckEncoded(address);
    }

    public EncryptionKeys generateEncryptionKeys(String publicKey) {
        return libBitcoinProvider.provide().getEncryptionKeys(publicKey);
    }

    public DecryptionKeys generateDecryptionKeys(DerivationPath derivationPath, byte[] ephemeralPublicKey) {
        return libBitcoinProvider.provide().getDecryptionKeys(derivationPath, ephemeralPublicKey);
    }

    public double calcMinMinerFee(TransactionFee transactionFee) {
        return Math.max(transactionFee.getMin(), Intents.MIN_FEE_FLOOR);
    }

    private BTCCurrency satoshisToBTC(long satoshis) {
        return new BTCCurrency(satoshis);
    }
}
