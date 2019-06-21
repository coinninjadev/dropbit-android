package com.coinninja.coinkeeper.service.client.model;

public class SentInvite {
    String id;
    long created_at;
    long updated_at;
    String address;
    String sender;
    String status;
    InviteMetadata metadata;
    String phone_number_hash;
    String txid;
    String wallet_id;
    String address_pubkey;
    String delivery_id;

    public String getDelivery_id() {
        return delivery_id;
    }

    public String getId() {
        return id;
    }

    public long getCreated_at() {
        return created_at * 1000;
    }

    public long getUpdated_at() {
        return updated_at * 1000;
    }

    public String getAddress() {
        return address;
    }

    public String getSender() {
        return sender;
    }

    public String getStatus() {
        return status;
    }

    public InviteMetadata getMetadata() {
        return metadata;
    }

    public String getPhone_number_hash() {
        return phone_number_hash;
    }

    public String getTxid() {
        return txid;
    }

    public String getWallet_id() {
        return wallet_id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddressPubKey() {
        return address_pubkey;
    }
}
