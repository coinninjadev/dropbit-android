package com.coinninja.coinkeeper.service.client.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class DropBitInvitation implements Parcelable {
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
    String request_ttl;

    public DropBitInvitation(){
    }

    public DropBitInvitation(String id, long created_at, long updated_at, String address, String phone_number_hash, String status, String wallet_id) {
        this.id = "";
        this.created_at = 0L;
        this.updated_at = 0L;
        this.address = "";
        this.phone_number_hash = "";
        this.status = "";
        this.wallet_id = "";
        this.id = id;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.address = address;
        this.phone_number_hash = phone_number_hash;
        this.status = status;
        this.wallet_id = wallet_id;
    }

    protected DropBitInvitation(Parcel in) {
        id = in.readString();
        created_at = in.readLong();
        updated_at = in.readLong();
        address = in.readString();
        sender = in.readString();
        status = in.readString();
        phone_number_hash = in.readString();
        txid = in.readString();
        wallet_id = in.readString();
        address_pubkey = in.readString();
        request_ttl = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeLong(created_at);
        dest.writeLong(updated_at);
        dest.writeString(address);
        dest.writeString(sender);
        dest.writeString(status);
        dest.writeString(phone_number_hash);
        dest.writeString(txid);
        dest.writeString(wallet_id);
        dest.writeString(address_pubkey);
        dest.writeString(request_ttl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DropBitInvitation> CREATOR = new Creator<DropBitInvitation>() {
        @Override
        public DropBitInvitation createFromParcel(Parcel in) {
            return new DropBitInvitation(in);
        }

        @Override
        public DropBitInvitation[] newArray(int size) {
            return new DropBitInvitation[size];
        }
    };

    public String getId() {
        return id;
    }

    public long getUpdated_at() {
        return updated_at * 1000;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public String getAddress() {
        return address;

    }


    public void setTxid(String txid) {
        this.txid = txid;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public InviteMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(InviteMetadata metadata) {
        this.metadata = metadata;
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

    public long getCreatedAt() {
        return created_at * 1000;
    }

    public long getCreated_at() {
        return getCreatedAt();
    }
    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }


    public String getAddressPubKey() {

        return address_pubkey;
    }

    public String getRequest_ttl() {
        return request_ttl;
    }

    public void setRequest_ttl(String request_ttl) {
        this.request_ttl = request_ttl;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DropBitInvitation that = (DropBitInvitation) o;
        return created_at == that.created_at &&
                updated_at == that.updated_at &&
                Objects.equals(id, that.id) &&
                Objects.equals(address, that.address) &&
                Objects.equals(sender, that.sender) &&
                Objects.equals(status, that.status) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(phone_number_hash, that.phone_number_hash) &&
                Objects.equals(txid, that.txid) &&
                Objects.equals(wallet_id, that.wallet_id) &&
                Objects.equals(address_pubkey, that.address_pubkey) &&
                Objects.equals(request_ttl, that.request_ttl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, created_at, updated_at, address, sender, status, metadata, phone_number_hash, txid, wallet_id, address_pubkey, request_ttl);
    }
}
