package com.coinninja.coinkeeper.view.adapter.util;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.FiatCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

public class BindableTransaction {
    private final WalletHelper walletHelper;
    private String txTime;
    private ConfirmationState confirmationState;
    private InviteState inviteState;
    private SendState sendState;
    private long value;
    private long fee;
    private String fundingAddress;
    private String targetAddress;
    private String txID;
    private String contactName;
    private String contactPhoneNumber;
    private Long historicalInviteUSDValue;
    private String serverInviteId;
    private int numConfirmations;
    private long historicalTransactionUSDValue;
    private boolean isSharedMemo;
    private String memo;

    @Inject
    public BindableTransaction(WalletHelper walletHelper) {
        this.walletHelper = walletHelper;
        reset();
    }

    public void reset() {
        txTime = "";
        confirmationState = null;
        inviteState = null;
        sendState = null;
        value = 0L;
        fee = 0L;
        fundingAddress = "";
        targetAddress = "";
        txID = "";
        contactName = "";
        contactPhoneNumber = "";
        historicalInviteUSDValue = 0L;
        serverInviteId = "";
        numConfirmations = 0;
        historicalTransactionUSDValue = 0L;
        isSharedMemo = false;
        memo = "";
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getTxTime() {
        return txTime;
    }

    public void setTxTime(String txTime) {
        this.txTime = txTime;
    }

    public ConfirmationState getConfirmationState() {
        return confirmationState;
    }

    public void setConfirmationState(ConfirmationState confirmationState) {
        this.confirmationState = confirmationState;
    }

    public SendState getSendState() {
        return sendState;
    }

    public void setSendState(SendState sendState) {
        this.sendState = sendState;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public CryptoCurrency getValueCurrency() {
        return new BTCCurrency(getValue());
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public CryptoCurrency getFeeCurrency() {
        return new BTCCurrency(getFee());
    }

    public long getTotalTransactionCost() {
        return getFee() + getValue();
    }

    public CryptoCurrency getTotalTransactionCostCurrency() {
        return new BTCCurrency(getTotalTransactionCost());
    }

    public String getFundingAddress() {
        return fundingAddress;
    }

    public void setFundingAddress(String fundingAddress) {
        this.fundingAddress = fundingAddress;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }

    public String getTxID() {
        return txID;
    }

    public void setTxID(String txID) {
        this.txID = txID;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public InviteState getInviteState() {
        return inviteState;
    }

    public void setInviteState(InviteState inviteState) {
        this.inviteState = inviteState;
    }

    public Long getHistoricalInviteUSDValue() {
        return historicalInviteUSDValue;
    }

    public void setHistoricalInviteUSDValue(Long historicalUSDValue) {
        historicalInviteUSDValue = historicalUSDValue;
    }

    public long getHistoricalTransactionUSDValue() {
        return historicalTransactionUSDValue;
    }

    public void setHistoricalTransactionUSDValue(long historicalTransactionUSDValue) {
        this.historicalTransactionUSDValue = historicalTransactionUSDValue;
    }

    public String getServerInviteId() {
        return serverInviteId;
    }

    public void setServerInviteId(String serverInviteId) {
        this.serverInviteId = serverInviteId;
    }

    public int getConfirmationCount() {
        return numConfirmations;
    }

    public void setConfirmationCount(int numConfirmations) {
        this.numConfirmations = numConfirmations;
    }

    public boolean getIsSharedMemo() {
        return isSharedMemo;
    }

    public void setIsSharedMemo(boolean isSharedMemo) {
        this.isSharedMemo = isSharedMemo;
    }

    public String getContactOrPhoneNumber() {
        return "".equals(contactName) ? contactPhoneNumber : contactName;
    }

    public String getIdentifiableTarget() {
        if (contactName != null && !contactName.isEmpty())
            return contactName;
        if (contactPhoneNumber != null && !contactPhoneNumber.isEmpty()) {
            return new PhoneNumber(contactPhoneNumber).displayTextForLocale();
        }
        return targetAddress;
    }

    public SendState getBasicDirection() {
        switch (sendState) {
            case FAILED_TO_BROADCAST_SEND:
            case SEND_CANCELED:
            case SEND:
                return SendState.SEND;
            case FAILED_TO_BROADCAST_TRANSFER:
            case TRANSFER:
                return SendState.TRANSFER;
            case FAILED_TO_BROADCAST_RECEIVE:
            case RECEIVE_CANCELED:
            case RECEIVE:
            default:
                return SendState.RECEIVE;
        }

    }

    public CryptoCurrency totalCryptoForSendState() {
        switch (getBasicDirection()) {
            case SEND:
                return getTotalTransactionCostCurrency();
            case TRANSFER:
                return getFeeCurrency();
            case RECEIVE:
            default:
                return getValueCurrency();
        }
    }

    public FiatCurrency totalFiatForSendState() {
        CryptoCurrency total = totalCryptoForSendState();
        return total.toFiat(walletHelper.getLatestPrice());
    }

    public enum SendState {
        RECEIVE, TRANSFER, SEND, SEND_CANCELED, RECEIVE_CANCELED, FAILED_TO_BROADCAST_TRANSFER, FAILED_TO_BROADCAST_SEND, FAILED_TO_BROADCAST_RECEIVE, DOUBLESPEND_SEND,
    }

    public enum ConfirmationState {
        ONE_CONFIRM, TWO_CONFIRMS, CONFIRMED, UNCONFIRMED

    }

    public enum InviteState {
        SENT_PENDING, RECEIVED_PENDING, CONFIRMED, EXPIRED, CANCELED, SENT_ADDRESS_PROVIDED, RECEIVED_ADDRESS_PROVIDED
    }
}
