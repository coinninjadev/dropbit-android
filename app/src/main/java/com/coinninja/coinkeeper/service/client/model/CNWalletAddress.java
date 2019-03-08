package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

public class CNWalletAddress {
    String id;

    @SerializedName("created_at")
    long createdAt;

    @SerializedName("updated_at")
    long updateAt;

    String address;

    @SerializedName("wallet_id")
    String walletId;

    @SerializedName("address_pubkey")
    String publicKey;

    public String getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt * 1000;
    }

    public long getUpdateAt() {
        return updateAt * 1000;
    }

    public String getAddress() {
        return address;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
