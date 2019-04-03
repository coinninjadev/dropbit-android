package com.coinninja.coinkeeper.service.client.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.util.Hasher;

import java.util.Objects;

public class Contact implements Parcelable {
    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
    String displayName;
    PhoneNumber phoneNumber;
    boolean isVerified;
    String hash;

    public Contact() {
    }

    public Contact(PhoneNumber phoneNumber, String displayName, boolean isVerified) {
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.isVerified = isVerified;
        hash = new Hasher().hash(getNumberWithCountryCode());
    }

    protected Contact(Parcel in) {
        displayName = in.readString();
        phoneNumber = new PhoneNumber(in.readString());
        isVerified = in.readByte() != 0;
        hash = in.readString();
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
        hash = new Hasher().hash(getNumberWithCountryCode());
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumberString) {
        setPhoneNumber(new PhoneNumber(phoneNumberString));
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getNumberWithCountryCode() {
        if (phoneNumber == null || phoneNumber.toString() == null || phoneNumber.toString().isEmpty()) { return ""; }
        return phoneNumber.toString().replace("+", "");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(phoneNumber.toString());
        dest.writeByte((byte) (isVerified ? 1 : 0));
        dest.writeString(hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return isVerified == contact.isVerified &&
                Objects.equals(displayName, contact.displayName) &&
                Objects.equals(phoneNumber, contact.phoneNumber) &&
                Objects.equals(hash, contact.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, phoneNumber, isVerified, hash);
    }

    public String toDisplayNameOrInternationalPhoneNumber() {
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        } else {
            return phoneNumber.toInternationalDisplayText();
        }
    }
}
