package com.coinninja.coinkeeper.service.client.model;

public class ReceivedInvite {

    String id;
    long created_at;
    long updated_at;
    String address;
    String sender;
    String status;
    String request_ttl;
    String txid;
    InviteMetadata metadata;

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

    public String getRequest_ttl() {
        return request_ttl;
    }

    public String getTxid() {
        return txid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRequest_ttl(String request_ttl) {
        this.request_ttl = request_ttl;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public void setMetadata(InviteMetadata metadata) {
        this.metadata = metadata;
    }

}

