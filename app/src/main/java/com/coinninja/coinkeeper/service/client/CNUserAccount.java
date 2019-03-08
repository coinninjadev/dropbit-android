package com.coinninja.coinkeeper.service.client;

import com.google.gson.annotations.SerializedName;

public class CNUserAccount {

    private String id;
    private String status;
    private String wallet_id;

    @SerializedName("phone_number_hash")
    private String phoneNumberHash;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWallet_id() {
        return wallet_id;
    }

    public void setWallet_id(String wallet_id) {
        this.wallet_id = wallet_id;
    }

    public String getPhoneNumberHash() {
        return phoneNumberHash;
    }
}
