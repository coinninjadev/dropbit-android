package com.coinninja.coinkeeper.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.TelephonyManager;

import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;

public class PhoneNumber implements Parcelable {

    private Phonenumber.PhoneNumber _phoneNumber;
    private PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();

    public PhoneNumber() {

    }

    public PhoneNumber(String i18n) {
        _phoneNumber = phoneNumberUtil.toPhoneNumber(i18n);
    }

    public PhoneNumber(Phonenumber.PhoneNumber phoneNumber) {
        _phoneNumber = phoneNumber;
    }

    public PhoneNumber(int countryCode, String phoneNumber) {
        this(String.format("+%s%s", countryCode, phoneNumber));
    }

    public PhoneNumber(CNPhoneNumber phoneNumber) {
        _phoneNumber = phoneNumberUtil.toPhoneNumber(phoneNumber);
    }

    protected PhoneNumber(Parcel in) {
        _phoneNumber = (Phonenumber.PhoneNumber) in.readSerializable();
    }

    public static final Creator<PhoneNumber> CREATOR = new Creator<PhoneNumber>() {
        @Override
        public PhoneNumber createFromParcel(Parcel in) {
            return new PhoneNumber(in);
        }

        @Override
        public PhoneNumber[] newArray(int size) {
            return new PhoneNumber[size];
        }
    };

    public long getNationalNumber() {
        return _phoneNumber == null ? 0L : _phoneNumber.getNationalNumber();
    }

    @Override
    @NotNull
    public String toString() {
        return phoneNumberUtil.i18Formatted(_phoneNumber);
    }

    public String displayTextForLocale() {
        if(phoneNumberUtil.getCountryCodeForRegion() == getCountryCode()) {
            return toNationalDisplayText();
        } else {
            return toInternationalDisplayText();
        }
    }

    public String toNationalDisplayText(){
        return phoneNumberUtil.toNationalDisplayText(_phoneNumber);
    }

    public String toInternationalDisplayText() {
        return phoneNumberUtil.toInternationalDisplayText(_phoneNumber);
    }

    public int getCountryCode() {
        return _phoneNumber.getCountryCode();
    }

    @Deprecated
    public Phonenumber.PhoneNumber getPhoneNumber() {
        return _phoneNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(_phoneNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(_phoneNumber, that._phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_phoneNumber);
    }

    public boolean isValid() {
        return _phoneNumber != null && phoneNumberUtil.isValidNumber(toString());
    }

}
