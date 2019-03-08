package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

public class CNSubscription {

    String id;

    @SerializedName("owner_id")
    String ownerId;

    @SerializedName("owner_type")
    String ownerType;

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }
}
