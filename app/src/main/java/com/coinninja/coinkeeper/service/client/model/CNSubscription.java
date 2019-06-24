package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CNSubscription that = (CNSubscription) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(ownerType, that.ownerType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerId, ownerType);
    }
}
