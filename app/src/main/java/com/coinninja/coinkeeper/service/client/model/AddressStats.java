package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

public class AddressStats {
    String address;
    long balance;
    long received;
    long spent;
    @SerializedName("tx_count")
    int numTransactions;

    public String getAddress() {
        return address;
    }

    public long getBalance() {
        return balance;
    }

    public long getReceived() {
        return received;
    }

    public long getSpent() {
        return spent;
    }

    public int getNumTransactions() {
        return numTransactions;
    }
}
