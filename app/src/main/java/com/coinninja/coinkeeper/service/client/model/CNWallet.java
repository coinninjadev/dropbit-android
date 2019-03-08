package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

public class CNWallet {

    String id;

    @SerializedName("public_key_string")
    String publicKeyString;

    @SerializedName("created_at")
    long createdDate;

    @SerializedName("updated_at")
    long updatedDate;

    public String getId() {
        return id;
    }

    public String getPublicKeyString() {
        return publicKeyString;
    }

    public long getUpdatedDate() {
        return updatedDate * 1000;
    }

    public long getCreatedDate() {
        return createdDate * 1000;
    }
}
