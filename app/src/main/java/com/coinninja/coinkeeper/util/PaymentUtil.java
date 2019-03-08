package com.coinninja.coinkeeper.util;

import android.content.Context;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.FundedCallback;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.tasks.CreateFundingUTXOsTask;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

public class PaymentUtil implements FundedCallback {

    private final Context context;
    private final BitcoinUtil bitcoinUtil;
    private String address;
    private Contact contact;
    private PaymentMethod paymentMethod;
    private String errorMessage;
    private PaymentHolder paymentHolder;
    private final TargetStatHelper targetStatHelper;
    private final FundingUTXOs.Builder fundingBuilder;
    private FundedCallback fundedCallback;
    FundingUTXOs fundingUTXOs = null;

    @Inject
    public PaymentUtil(@ApplicationContext Context context, BitcoinUtil bitcoinUtil,
                       TargetStatHelper targetStatHelper, FundingUTXOs.Builder fundingBuilder) {
        this.context = context;
        this.targetStatHelper = targetStatHelper;
        this.fundingBuilder = fundingBuilder;
        paymentMethod = PaymentMethod.INVALID;
        this.bitcoinUtil = bitcoinUtil;
    }

    public void setPaymentHolder(PaymentHolder paymentHolder) {
        this.paymentHolder = paymentHolder;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setAddress(String address) {
        errorMessage = "";
        contact = null;
        this.address = address == null || address.isEmpty() ? null : address;
        setPaymentMethod();
    }

    public String getAddress() {
        return address;
    }

    public void setContact(Contact contact) {
        errorMessage = "";
        address = null;
        this.contact = contact;
        setPaymentMethod();
    }

    public Contact getContact() {
        return contact;
    }

    private void setPaymentMethod() {
        if (null != address) {
            paymentMethod = PaymentMethod.ADDRESS;
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

    private void setErrorMessage(String message) {
        errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public PaymentHolder getPaymentHolder() {
        return paymentHolder;
    }

    public BTCCurrency setUSDPayment(double paymentAmmount) {
        fundingUTXOs = null;
        return (BTCCurrency) paymentHolder.loadPaymentFrom(new USDCurrency(paymentAmmount));
    }

    public boolean isValid() {
        return isValidPaymentMethod() && isValidPaymentAmount() && isFunded();
    }

    public void checkFunding(FundedCallback fundedCallback) {
        this.fundedCallback = fundedCallback;
        CreateFundingUTXOsTask.newInstance(fundingBuilder, targetStatHelper, paymentHolder, this).execute();
    }

    private boolean isValidPaymentAmount() {
        BTCCurrency btcCurrency = paymentHolder.getBtcCurrency();
        USDCurrency usdCurrency = paymentHolder.getUsdCurrency();

        boolean isValid = btcCurrency.isValid() && btcCurrency.toSatoshis() > 0;

        if (!isValid) {
            errorMessage = getString(R.string.pay_error_invalid_amount);
            return false;
        }

        isValid = usdCurrency.toLong() > 99L;
        if (!isValid) {
            errorMessage = getString(R.string.pay_error_too_little_transaction);
            return false;
        }

        isValid = !((paymentMethod == PaymentMethod.INVITE) &&
                paymentHolder.getUsdCurrency().toLong() > Intents.MAX_DOLLARS_SENT_THROUGH_CONTACTS);

        if (!isValid) {
            errorMessage = getString(R.string.payment_error_too_much_sent_to_contact);
        }

        return isValid;
    }

    protected boolean isValidPaymentMethod() {
        if (paymentMethod == PaymentMethod.ADDRESS) {
            validateAddress();
        }
        return paymentMethod != PaymentMethod.INVALID;
    }

    public boolean isFunded() {
        BTCCurrency spendableBalance = paymentHolder.getSpendableBalance();
        boolean funded = true;
        String total = "";
        BTCCurrency availableBTC;

        if (null == fundingUTXOs) {
            funded = paymentHolder.getBtcCurrency().toSatoshis() <= spendableBalance.toSatoshis();
            total = paymentHolder.getBtcCurrency().toFormattedCurrency();
            availableBTC = spendableBalance;
        } else {
            funded = isValidFundingUtxos() && fundingUTXOs.getSatoshisTotalSpending() <= fundingUTXOs.getSatoshisFundedTotal();
            total = new BTCCurrency(fundingUTXOs.getSatoshisTotalSpending()).toFormattedCurrency();
            availableBTC = new BTCCurrency(fundingUTXOs.getSatoshisFundedTotal());
        }

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
            builder.append(availableBTC.toFormattedCurrency());
            builder.append(" ");
            builder.append(availableBTC.toUSD(paymentHolder.getEvaluationCurrency()).toFormattedCurrency());
            errorMessage = builder.toString();
        }

        return funded;
    }


    private boolean isValidFundingUtxos() {
        return !(hasNegativeValue(fundingUTXOs) || hasTooLargeValue(fundingUTXOs));
    }

    private boolean hasNegativeValue(FundingUTXOs fundingUTXOs) {
        return fundingUTXOs.getSatoshisFundedTotal() < 0 ||
                fundingUTXOs.getSatoshisFeesSpending() < 0 ||
                fundingUTXOs.getSatoshisSpending() < 0 ||
                fundingUTXOs.getSatoshisTotalSpending() < 0;
    }

    private boolean hasTooLargeValue(FundingUTXOs fundingUTXOs) {
        long max = BTCCurrency.MAX_SATOSHI;
        return fundingUTXOs.getSatoshisFundedTotal() >= max ||
                fundingUTXOs.getSatoshisFeesSpending() >= max ||
                fundingUTXOs.getSatoshisSpending() >= max ||
                fundingUTXOs.getSatoshisTotalSpending() >= max;
    }

    private String getString(int res_id) {
        return context.getString(res_id);
    }

    @Override
    public void onComplete(FundingUTXOs fundingUTXOs) {
        this.fundingUTXOs = fundingUTXOs;
        fundedCallback.onComplete(fundingUTXOs);
    }

    public void setFundingUTXOs(FundingUTXOs fundingUTXOs) {
        this.fundingUTXOs = fundingUTXOs;
    }

    public void reset() {
        fundingUTXOs = null;
        setAddress(null);
    }

    public void clearErrors() {
        fundingUTXOs = null;
        errorMessage = "";
    }

    public boolean isVerifiedContact() {
        return getContact().isVerified();
    }

    public enum PaymentMethod {
        INVALID, ADDRESS, VERIFIED_CONTACT, INVITE
    }
}
