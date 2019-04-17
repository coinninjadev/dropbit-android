package com.coinninja.coinkeeper.util;

import android.content.Context;

import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

public class PaymentUtil {

    private final Context context;
    private final BitcoinUtil bitcoinUtil;
    private final TransactionFundingManager transactionFundingManager;
    private TransactionFee transactionFee;
    private String address;
    private Contact contact;
    private PaymentMethod paymentMethod;
    private String errorMessage;
    private PaymentHolder paymentHolder;
    private boolean isFudningMax = false;

    @Inject
    public PaymentUtil(@ApplicationContext Context context, BitcoinUtil bitcoinUtil,
                       TransactionFundingManager transactionFundingManager) {
        this.context = context;
        this.transactionFundingManager = transactionFundingManager;
        paymentMethod = PaymentMethod.INVALID;
        this.bitcoinUtil = bitcoinUtil;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        errorMessage = "";
        contact = null;
        this.address = address == null || address.isEmpty() ? null : address;
        setPaymentMethod();
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        errorMessage = "";
        address = null;
        this.contact = contact;
        setPaymentMethod();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private void setErrorMessage(String message) {
        errorMessage = message;
    }

    public boolean fundMax() {
        isFudningMax = true;
        TransactionData transactionData = transactionFundingManager.buildFundedTransactionData(transactionFee);
        paymentHolder.setTransactionData(transactionData);
        return isFunded();
    }

    public void clearFunding() {
        isFudningMax = false;
        String address = paymentHolder.getPaymentAddress();
        paymentHolder.clearPayment();
        paymentHolder.setPaymentAddress(address);
    }

    public boolean isSendingMax() {
        return isFudningMax;
    }

    public PaymentHolder getPaymentHolder() {
        return paymentHolder;
    }

    public void setPaymentHolder(PaymentHolder paymentHolder) {
        this.paymentHolder = paymentHolder;
    }

    public boolean isValid() {
        return isValidPaymentMethod() && isValidPaymentAmount();
    }

    public boolean checkFunding() {
        if (!isSendingMax()) {
            TransactionData transactionData = transactionFundingManager
                    .buildFundedTransactionData(transactionFee,
                            paymentHolder.getCryptoCurrency().toLong());
            paymentHolder.setTransactionData(transactionData);
        }
        return isFunded();
    }

    public boolean isFunded() {
        BTCCurrency spendableBalance = paymentHolder.getSpendableBalance();
        TransactionData transactionData = paymentHolder.getTransactionData();
        boolean funded = isValidFunding() && transactionData.getUtxos().length > 0 && transactionData.getAmount() > 0;
        String total = paymentHolder.getBtcCurrency().toFormattedCurrency();

        if (!funded) {
            StringBuilder builder = new StringBuilder();
            builder.append(getString(R.string.pay_not_attempting_to_send));
            builder.append(" ");
            builder.append(total);
            builder.append(". ");
            builder.append(getString(R.string.pay_not_enough_funds_error));
            builder.append("\n");
            builder.append(getString(R.string.pay_not_available_funds_error));
            builder.append(" ");
            builder.append(spendableBalance.toFormattedCurrency());
            builder.append(" ");
            builder.append(spendableBalance.toUSD(paymentHolder.getEvaluationCurrency()).toFormattedCurrency());
            errorMessage = builder.toString();
        }

        return funded;
    }

    public void reset() {
        setAddress(null);
    }

    public void clearErrors() {
        errorMessage = "";
    }

    public boolean isVerifiedContact() {
        return getContact().isVerified();
    }

    public TransactionFee getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(TransactionFee transactionFee) {
        this.transactionFee = transactionFee;
    }

    protected boolean isValidPaymentMethod() {
        if (paymentMethod == PaymentMethod.ADDRESS) {
            validateAddress();
        }
        return paymentMethod != PaymentMethod.INVALID;
    }

    private void setPaymentMethod() {
        if (null != address) {
            paymentMethod = PaymentMethod.ADDRESS;
            paymentHolder.setPaymentAddress(address);
        } else if (null != contact && contact.isVerified()) {
            paymentMethod = PaymentMethod.VERIFIED_CONTACT;
        } else if (null != contact && !contact.isVerified()) {
            paymentMethod = PaymentMethod.INVITE;
        } else {
            errorMessage = getString(R.string.pay_error_add_valid_bitcoin_address);
            paymentMethod = PaymentMethod.INVALID;
        }
    }

    private void validateAddress() {
        String invalid_btc_address = context.getString(R.string.invalid_bitcoin_address_error);

        if (null == address) {
            setErrorMessage(invalid_btc_address);
            return;
        }

        if (!bitcoinUtil.isValidBTCAddress(address)) {
            BitcoinUtil.ADDRESS_INVALID_REASON invalidReason = bitcoinUtil.getInvalidReason();
            paymentMethod = PaymentMethod.INVALID;
            switch (invalidReason) {
                case NULL_ADDRESS:
                    setErrorMessage(invalid_btc_address);
                    break;
                case IS_BC1:
                    setErrorMessage(context.getString(R.string.bc1_error_message));
                    break;
                case NOT_BASE58:
                    setErrorMessage(context.getString(R.string.invalid_btc_adddress__base58));
                    break;
                case NOT_STANDARD_BTC_PATTERN:
                    setErrorMessage(invalid_btc_address);
                    break;
            }
        }
    }

    private boolean isValidPaymentAmount() {
        BTCCurrency btcCurrency = paymentHolder.getBtcCurrency();
        USDCurrency usdCurrency = (USDCurrency) paymentHolder.getFiat();

        boolean isValid = btcCurrency.isValid() && btcCurrency.toSatoshis() > 0;

        if (!isValid) {
            errorMessage = getString(R.string.pay_error_invalid_amount);
            return false;
        }

        long amountSpending = usdCurrency.toLong();
        isValid = isFudningMax && amountSpending > 0 || !isFudningMax && amountSpending > 99;
        if (!isValid) {
            errorMessage = getString(R.string.pay_error_too_little_transaction);
            return false;
        }

        isValid = !((paymentMethod == PaymentMethod.INVITE) &&
                paymentHolder.getFiat().toLong() > Intents.MAX_DOLLARS_SENT_THROUGH_CONTACTS);

        if (!isValid) {
            errorMessage = getString(R.string.payment_error_too_much_sent_to_contact);
        }

        return isValid;
    }

    private boolean isValidFunding() {
        return !(hasNegativeValue() || hasTooLargeValue());
    }

    private boolean hasNegativeValue() {
        TransactionData transactionData = paymentHolder.getTransactionData();
        return transactionData.getUtxos().length <= 0 ||
                transactionData.getAmount() < 0 ||
                transactionData.getFeeAmount() < 0 ||
                transactionData.getChangeAmount() < 0;
    }

    private boolean hasTooLargeValue() {
        TransactionData transactionData = paymentHolder.getTransactionData();
        long max = BTCCurrency.MAX_SATOSHI;
        return transactionData.getAmount() >= max ||
                transactionData.getChangeAmount() >= max ||
                transactionData.getFeeAmount() >= max;
    }

    private String getString(int res_id) {
        return context.getString(res_id);
    }

    public enum PaymentMethod {
        INVALID, ADDRESS, VERIFIED_CONTACT, INVITE
    }
}
