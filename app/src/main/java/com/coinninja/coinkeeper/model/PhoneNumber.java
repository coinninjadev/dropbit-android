package com.coinninja.coinkeeper.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.util.Hasher;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.VariableLengthPhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PhoneNumber implements Parcelable {

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
    private Phonenumber.PhoneNumber _phoneNumber;
    private PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();
    private Hasher hasher = new Hasher();
    private VariableLengthPhoneNumberUtil variableLengthPhoneNumberUtil = new VariableLengthPhoneNumberUtil();

    PhoneNumber() {
    }

    public PhoneNumber(String i18n) {
        _phoneNumber = phoneNumberUtil.toPhoneNumber(i18n);
    }

    public PhoneNumber(Phonenumber.PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            return;
        }
        _phoneNumber = new Phonenumber.PhoneNumber();
        _phoneNumber.setNationalNumber(phoneNumber.getNationalNumber());
        _phoneNumber.setCountryCode(phoneNumber.getCountryCode());
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

    public long getNationalNumber() {
        return _phoneNumber == null ? 0L : _phoneNumber.getNationalNumber();
    }

    public String displayTextForLocale() {
        if (phoneNumberUtil.getCountryCodeForRegion() == getCountryCode()) {
            return toNationalDisplayText();
        } else {
            return toInternationalDisplayText();
        }
    }

    public String toNationalDisplayText() {
        return phoneNumberUtil.toNationalDisplayText(_phoneNumber);
    }

    public String toInternationalDisplayText() {
        return phoneNumberUtil.toInternationalDisplayText(_phoneNumber);
    }

    public int getCountryCode() {
        return _phoneNumber == null ? 0 : _phoneNumber.getCountryCode();
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
    public int hashCode() {
        return Objects.hash(_phoneNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(_phoneNumber, that._phoneNumber);
    }

    @Override
    @NotNull
    public String toString() {
        return phoneNumberUtil.i18Formatted(_phoneNumber);
    }

    public boolean isValid() {
        return _phoneNumber != null && phoneNumberUtil.isValidNumber(toString());
    }

    public String getHashReadyPhoneNumber() {
        return String.valueOf(variableLengthPhoneNumberUtil.spliceNationalPrefixIntoPhoneNumberIfNecessary(_phoneNumber).getNationalNumber());
    }

    @NotNull
    public CNPhoneNumber toCNPhoneNumber() {
        return new CNPhoneNumber(toString());
    }

    @Nullable
    public String toHash() {
        if (_phoneNumber.hasCountryCode() && _phoneNumber.hasNationalNumber())
            return hasher.hash(this);
        return null;
    }
}
