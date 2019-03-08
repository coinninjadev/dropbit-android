package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

public class GsonAddress {
    private static int PRECISION = 8;
    String address;

    @SerializedName("txid")
    String transactionId;

    long time;
    long vin;
    long vout;

    int derivationIndex;

    public String getAddress() {
        return address;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public long getTime() {
        // convert to millis
        return time * 1000;
    }

    public long getVin() {
        return vin;
    }

    public long getVout() {
        return vout;
    }

    public int getDerivationIndex() {
        return derivationIndex;
    }

    public void setDerivationIndex(int derivationIndex) {
        this.derivationIndex = derivationIndex;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
