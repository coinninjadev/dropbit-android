package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

public class TransactionStats {
    @SerializedName("txid")
    private String transactionId;

    @SerializedName("coinbase")
    private boolean isCoinBase;

    @SerializedName("confirmations")
    private int numConfirmations;

    @SerializedName("fees_rate")
    private long feesRate;

    private long fees;

    private String miner;

    @SerializedName("vin_value")
    private long vinValue;

    @SerializedName("vout_value")
    private long voutValue;

    public void setTransactionId(String txid) {
        transactionId = txid;
    }

    public void setCoinBase(boolean coinBase) {
        isCoinBase = coinBase;
    }

    public void setFees(long fees) {
        this.fees = fees;
    }

    public void setFeesRate(long feesRate) {
        this.feesRate = feesRate;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    public void setNumConfirmations(int numConfirmations) {
        this.numConfirmations = numConfirmations;
    }

    public void setVinValue(long vinValue) {
        this.vinValue = vinValue;
    }

    public void setVoutValue(long voutValue) {
        this.voutValue = voutValue;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getNumConfirmations() {
        return numConfirmations;
    }

    public long getFees() {
        return fees;
    }

    public long getFeesRate() {
        return feesRate;
    }

    public String getMiner() {
        return miner;
    }

    public long getVinValue() {
        return vinValue;
    }

    public long getVoutValue() {
        return voutValue;
    }

    public boolean isCoinBase() {
        return isCoinBase;
    }
}
