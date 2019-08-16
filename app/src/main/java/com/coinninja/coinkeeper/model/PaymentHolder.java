package com.coinninja.coinkeeper.model;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

public class PaymentHolder {
    private Currency evaluationCurrency;
    private BTCCurrency spendableBalance;
    private boolean isSharingMemo = true;
    private String publicKey = "";
    private String memo = "";
    private TransactionData transactionData;

    private DefaultCurrencies defaultCurrencies;

    @Inject
    public PaymentHolder() {
        this(new USDCurrency(0L));
    }

    public PaymentHolder(Currency evaluationCurrency) {
        this.evaluationCurrency = evaluationCurrency;
        spendableBalance = new BTCCurrency(0L);
        clearTransactionData();
    }


    public Currency updateValue(Currency currency) {
        if (currency.isCrypto() != getPrimaryCurrency().isCrypto()) {
            toggleCurrencies();
        }

        String formattedAmount = currency.toFormattedString();
        Currency primaryCurrency = defaultCurrencies.getPrimaryCurrency();
        primaryCurrency.update(formattedAmount);
        Currency secondaryCurrency;

        if (primaryCurrency.isCrypto()) {
            secondaryCurrency = primaryCurrency.toUSD(evaluationCurrency);
        } else {
            secondaryCurrency = primaryCurrency.toBTC(evaluationCurrency);
        }

        defaultCurrencies.getSecondaryCurrency().update(secondaryCurrency.toFormattedString());
        return secondaryCurrency;
    }

    public BTCCurrency getBtcCurrency() {
        return (BTCCurrency) getCryptoCurrency();
    }

    public void toggleCurrencies() {
        defaultCurrencies = new DefaultCurrencies(defaultCurrencies.getSecondaryCurrency(), defaultCurrencies.getPrimaryCurrency());
    }

    public Currency getEvaluationCurrency() {
        return evaluationCurrency;
    }

    public void setEvaluationCurrency(Currency currency) {
        evaluationCurrency = currency;
    }

    public String getMemo() {
        return memo == null ? "" : memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public boolean getIsSharingMemo() {
        return isSharingMemo;
    }

    public void setIsSharingMemo(boolean sharingMemo) {
        isSharingMemo = sharingMemo;
    }

    public BTCCurrency getSpendableBalance() {
        return spendableBalance;
    }

    public void setSpendableBalance(BTCCurrency spendableBalance) {
        this.spendableBalance = spendableBalance;
    }

    public Currency getPrimaryCurrency() {
        return defaultCurrencies.getPrimaryCurrency();
    }

    public Currency getSecondaryCurrency() {
        return defaultCurrencies.getSecondaryCurrency();
    }

    public Currency getFiat() {
        return defaultCurrencies.getFiat();
    }

    public CryptoCurrency getCryptoCurrency() {
        return defaultCurrencies.getCrypto();
    }

    public String getPaymentAddress() {
        return transactionData.getPaymentAddress();
    }

    public void setPaymentAddress(String paymentAddress) {
        transactionData.setPaymentAddress(paymentAddress);
    }

    public boolean hasPaymentAddress() {
        return transactionData != null && transactionData.getPaymentAddress() != null && !"".equals(transactionData.getPaymentAddress());
    }

    public boolean hasPubKey() {
        return !"".equals(publicKey);
    }

    public void clearPayment() {
        publicKey = "";
        clearTransactionData();
        getPrimaryCurrency().zero();
    }

    public void setMaxLimitForFiat() {
        if (evaluationCurrency != null)
            USDCurrency.setMaxLimit((USDCurrency) evaluationCurrency);
    }

    public DefaultCurrencies getDefaultCurrencies() {
        return defaultCurrencies;
    }

    public void setDefaultCurrencies(DefaultCurrencies defaultCurrencies) {
        this.defaultCurrencies = defaultCurrencies;
    }

    public TransactionData getTransactionData() {
        return transactionData;
    }

    public void setTransactionData(TransactionData transactionData) {
        if (hasPaymentAddress())
            transactionData.setPaymentAddress(getPaymentAddress());
        this.transactionData = transactionData;
    }

    private void clearTransactionData() {
        transactionData = new TransactionData(new UnspentTransactionOutput[0], 0, 0,
                0, new DerivationPath(49, 0, 0, 1, 0), "");
    }
}
