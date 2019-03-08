package com.coinninja.coinkeeper.service.client.model;

public class CNSharedMemo {

    Integer encrypted_format;
    String address;
    String encrypted_payload;
    String txid;

    public Integer getEncrypted_format() {
        return encrypted_format;
    }

    public void setEncrypted_format(Integer encrypted_format) {
        this.encrypted_format = encrypted_format;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEncrypted_payload() {
        return encrypted_payload;
    }

    public void setEncrypted_payload(String encrypted_payload) {
        this.encrypted_payload = encrypted_payload;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

}
