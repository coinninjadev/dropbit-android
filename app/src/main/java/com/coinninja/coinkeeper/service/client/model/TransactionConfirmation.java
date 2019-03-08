package com.coinninja.coinkeeper.service.client.model;

public class TransactionConfirmation {
    public String txid;
    public int confirmations;

    public int getConfirmations() {
        return confirmations;
    }

    public String getTxid() {
        return txid;
    }
}
