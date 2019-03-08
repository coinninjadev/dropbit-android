package com.coinninja.coinkeeper.service.client.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class InvitedContact implements Parcelable {
    String id = "";
    long created_at = 0L;
    long updated_at = 0L;
    String address = "";
    String phone_number_hash = "";
    String status = "";
    String wallet_id = "";


    public InvitedContact() {
        this("", 0L, 0L, "", "", "", "");
    }

    public InvitedContact(String id, long created_at, long updated_at, String address, String phone_number_hash, String status, String wallet_id) {
        this.id = id;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.address = address;
        this.phone_number_hash = phone_number_hash;
        this.status = status;
        this.wallet_id = wallet_id;
    }

    public InvitedContact(Parcel in) {
        id = in.readString();
        created_at = in.readLong();
        updated_at = in.readLong();
        address = in.readString();
        phone_number_hash = in.readString();
        status = in.readString();
        wallet_id = in.readString();
    }

    public static final Creator<InvitedContact> CREATOR = new Creator<InvitedContact>() {
        @Override
        public InvitedContact createFromParcel(Parcel in) {
            return new InvitedContact(in);
        }

        @Override
        public InvitedContact[] newArray(int size) {
            return new InvitedContact[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeLong(created_at);
        dest.writeLong(updated_at);
        dest.writeString(address);
        dest.writeString(phone_number_hash);
        dest.writeString(status);
        dest.writeString(wallet_id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone_number_hash() {
        return phone_number_hash;
    }

    public void setPhone_number_hash(String phone_number_hash) {
        this.phone_number_hash = phone_number_hash;
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

    public long getCreatedAt() {
        return created_at * 1000;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvitedContact that = (InvitedContact) o;
        return created_at == that.created_at &&
                updated_at == that.updated_at &&
                Objects.equals(id, that.id) &&
                Objects.equals(address, that.address) &&
                Objects.equals(phone_number_hash, that.phone_number_hash) &&
                Objects.equals(status, that.status) &&
                Objects.equals(wallet_id, that.wallet_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, created_at, updated_at, address, phone_number_hash, status, wallet_id);
    }
}
