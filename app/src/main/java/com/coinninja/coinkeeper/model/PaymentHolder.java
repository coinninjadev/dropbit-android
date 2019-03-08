package com.coinninja.coinkeeper.model;

import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

public class PaymentHolder {
    private Currency evaluationCurrency;
    private TransactionFee transactionFee;
    private PaymentMode paymentMode;

    private USDCurrency usdCurrency;
    private BTCCurrency btcCurrency;
    private BTCCurrency spendableBalance;
    private boolean isSharingMemo = true;
    private String publicKey = "";
    private String memo = "";
    private String paymentAddress = "";

    @Inject
    public PaymentHolder() {
        this(new USDCurrency(0L), new TransactionFee(0, 0, 0));
    }

    public PaymentHolder(Currency evaluationCurrency, TransactionFee transactionFee) {
        this.evaluationCurrency = evaluationCurrency;
        this.transactionFee = transactionFee;
        spendableBalance = new BTCCurrency(0L);
    }

    public Currency loadPaymentFrom(Currency currency) {
        if (currency instanceof BTCCurrency) {
            btcCurrency = (BTCCurrency) currency;
            paymentMode = PaymentMode.CRYPTO;
            usdCurrency = btcCurrency.toUSD(evaluationCurrency);
            return usdCurrency;
        } else {
            usdCurrency = (USDCurrency) currency;
            paymentMode = PaymentMode.FIAT;
            btcCurrency = usdCurrency.toBTC(evaluationCurrency);
            return btcCurrency;
        }
    }

    @Deprecated
    public USDCurrency getUsdCurrency() {
        return usdCurrency;
    }

    public BTCCurrency getBtcCurrency() {
        return btcCurrency;
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

    public void setIsSharingMemo(boolean sharingMemo) {
        isSharingMemo = sharingMemo;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public boolean getIsSharingMemo() {
        return isSharingMemo;
    }

    public BTCCurrency getSpendableBalance() {
        return spendableBalance;
    }

    public void setSpendableBalance(BTCCurrency spendableBalance) {
        this.spendableBalance = spendableBalance;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public Currency getPrimaryCurrency() {
        Currency currency;

        if (paymentMode == PaymentMode.CRYPTO) {
            currency = btcCurrency;
        } else {
            currency = usdCurrency;
        }

        return currency;
    }

    public Currency getSecondaryCurrency() {
        Currency currency;
        if (paymentMode == PaymentMode.CRYPTO) {
            currency = usdCurrency;
        } else {
            currency = btcCurrency;
        }
        return currency;
    }

    public Currency getCryptoCurrency() {
        return btcCurrency;
    }

    public void setPaymentAddress(String paymentAddress) {
        this.paymentAddress = paymentAddress;
    }

    public String getPaymentAddress() {
        return paymentAddress;
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

    public enum PaymentMode {
        CRYPTO, FIAT
    }
}
