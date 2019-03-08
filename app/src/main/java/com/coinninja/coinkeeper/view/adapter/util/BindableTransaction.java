package com.coinninja.coinkeeper.view.adapter.util;

import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class BindableTransaction {
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
    private int numConfirmations = 0;
    private long historicalTransactionUSDValue = 0L;
    private boolean isSharedMemo = false;
    private String memo;

    public BindableTransaction() {
        memo = "";
        fundingAddress = "";
        targetAddress = "";
        contactName = "";
        contactPhoneNumber = "";
        serverInviteId = "";
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

    public Currency getValueCurrency() {
        return new BTCCurrency(getValue());
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getFee() {
        return fee;
    }

    public Currency getFeeCurrency() {
        return new BTCCurrency(getFee());
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getTotalTransactionCost() {
        return getFee() + getValue();
    }

    public Currency getTotalTransactionCostCurrency() {
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

    public void setHistoricalInviteUSDValue(Long historicalUSDValue) {
        historicalInviteUSDValue = historicalUSDValue;
    }

    public Long getHistoricalInviteUSDValue() {
        return historicalInviteUSDValue;
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

    public void setConfirmationCount(int numConfirmations) {
        this.numConfirmations = numConfirmations;
    }

    public int getConfirmationCount() {
        return numConfirmations;
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
            try {
                PhoneNumberUtil util = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber number = util.parse(contactPhoneNumber, "US");
                return util.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            } catch (NumberParseException e) {
                return contactPhoneNumber;
            }
        }
        return targetAddress;
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
