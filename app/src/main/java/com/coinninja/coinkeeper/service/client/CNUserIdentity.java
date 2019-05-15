package com.coinninja.coinkeeper.service.client;

public class CNUserIdentity {
    private String id;
    private String created_at;
    private String updated_at;
    private String type;
    private String identity;
    private String hash;
    private String handle;
    private String status;
    private String phone;

    public String getId() {
        return id;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getUpdatedAt() {
        return updated_at;
    }

    public String getType() {
        return type;
    }

    public String getIdentity() {
        return identity;
    }

    public String getHash() {
        return hash;
    }

    public String getHandle() {
        return handle;
    }

    public String getStatus() {
        return status;
    }

    public void setType(String phone) {
        this.phone = phone;
    }
}
