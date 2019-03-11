package com.coinninja.coinkeeper.service.client.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.google.gson.annotations.SerializedName;

public class CNPhoneNumber implements Parcelable {

    @SerializedName("country_code")
    int countryCode;
    @SerializedName("phone_number")
    String phoneNumber;

    public CNPhoneNumber() {
    }

    public CNPhoneNumber(PhoneNumber phoneNumber) {
        countryCode = phoneNumber.getCountryCode();
        this.phoneNumber = String.valueOf(phoneNumber.getNationalNumber());
    }

    public CNPhoneNumber(int countryCode, String number) {
        this.countryCode = countryCode;
        phoneNumber = number;
    }

    public CNPhoneNumber(String i18) {
        countryCode = Integer.valueOf(i18.substring(1, 2));
        phoneNumber = i18.substring(2);
    }

    protected CNPhoneNumber(Parcel in) {
        countryCode = in.readInt();
        phoneNumber = in.readString();
    }

    public static final Creator<CNPhoneNumber> CREATOR = new Creator<CNPhoneNumber>() {
        @Override
        public CNPhoneNumber createFromParcel(Parcel in) {
            return new CNPhoneNumber(in);
        }

        @Override
        public CNPhoneNumber[] newArray(int size) {
            return new CNPhoneNumber[size];
        }
    };

    public int getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(countryCode);
        dest.writeString(phoneNumber);
    }

    public PhoneNumber toPhoneNumber() {
        return new PhoneNumber(this);
    }
}
