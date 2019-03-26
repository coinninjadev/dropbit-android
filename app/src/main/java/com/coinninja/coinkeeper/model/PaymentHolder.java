package com.coinninja.coinkeeper.model;

import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

public class PaymentHolder {
    private Currency evaluationCurrency;
    private TransactionFee transactionFee;
    private BTCCurrency spendableBalance;
    private boolean isSharingMemo = true;
    private String publicKey = "";
    private String memo = "";
    private String paymentAddress = "";

    private DefaultCurrencies defaultCurrencies;

    @Inject
    public PaymentHolder() {
        this(new USDCurrency(0L), new TransactionFee(0, 0, 0));
    }

    public PaymentHolder(Currency evaluationCurrency, TransactionFee transactionFee) {
        this.evaluationCurrency = evaluationCurrency;
        this.transactionFee = transactionFee;
        spendableBalance = new BTCCurrency(0L);
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
        DefaultCurrencies newDefaults = new DefaultCurrencies(defaultCurrencies.getSecondaryCurrency(), defaultCurrencies.getPrimaryCurrency());
        defaultCurrencies = newDefaults;
    }

    public Currency getEvaluationCurrency() {
        return evaluationCurrency;
    }

    public void setEvaluationCurrency(Currency currency) {
        evaluationCurrency = currency;
    }

    public TransactionFee getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(TransactionFee transactionFee) {
        this.transactionFee = transactionFee;
    }

    public String getMemo() {
        return memo;
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
        return paymentAddress;
    }

    public void setPaymentAddress(String paymentAddress) {
        this.paymentAddress = paymentAddress;
    }

    public boolean hasPaymentAddress() {
        return !"".equals(paymentAddress);
    }

    public boolean hasPubKey() {
        return !"".equals(publicKey);
    }

    public void clearPayment() {
        publicKey = "";
        paymentAddress = "";
    }

    public void setMaxLimitForFiat() {
        if (evaluationCurrency != null)
            USDCurrency.SET_MAX_LIMIT((USDCurrency) evaluationCurrency);
    }

    public DefaultCurrencies getDefaultCurrencies() {
        return defaultCurrencies;
    }

    public void setDefaultCurrencies(DefaultCurrencies defaultCurrencies) {
        this.defaultCurrencies = defaultCurrencies;
    }
}
