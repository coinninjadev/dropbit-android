package com.coinninja.coinkeeper.service.client.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class AddressLookupResult implements Parcelable {
    @SerializedName("phone_number_hash")
    String phoneNumberHash;
    String address;
    @SerializedName("address_pubkey")
    String addressPubKey;

    public AddressLookupResult(String phoneNumberHash, String address, String addressPubKey) {
        this.phoneNumberHash = phoneNumberHash;
        this.address = address;
        this.addressPubKey = addressPubKey;
    }

    public AddressLookupResult() {
        this("", "", "");
    }

    protected AddressLookupResult(Parcel in) {
        phoneNumberHash = in.readString();
        address = in.readString();
        addressPubKey = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(phoneNumberHash);
        dest.writeString(address);
        dest.writeString(addressPubKey);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AddressLookupResult> CREATOR = new Creator<AddressLookupResult>() {
        @Override
        public AddressLookupResult createFromParcel(Parcel in) {
            return new AddressLookupResult(in);
        }

        @Override
        public AddressLookupResult[] newArray(int size) {
            return new AddressLookupResult[size];
        }
    };

    public String getPhoneNumberHash() {
        return phoneNumberHash;
    }

    public String getAddress() {
        return address;
    }

    public String getAddressPubKey() {
        return addressPubKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressLookupResult that = (AddressLookupResult) o;
        return Objects.equals(phoneNumberHash, that.phoneNumberHash) &&
                Objects.equals(address, that.address) &&
                Objects.equals(addressPubKey, that.addressPubKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumberHash, address, addressPubKey);
    }
}
