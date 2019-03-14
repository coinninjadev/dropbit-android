package com.coinninja.coinkeeper.util;

import com.google.i18n.phonenumbers.Phonenumber;

public class VariableLengthPhoneNumberUtil {

    public Phonenumber.PhoneNumber spliceNationalPrefixIntoPhoneNumberIfNecessary(Phonenumber.PhoneNumber phoneNumber) {
        String nationalNumber = String.valueOf(phoneNumber.getNationalNumber());
        VariableLengthCountry country = VariableLengthCountry.from(phoneNumber.getCountryCode());
        switch (country) {
            case UNNECESSARY:
                return phoneNumber;
            default:
                if (nationalNumber.startsWith(country.getOptionalNationalPrefix())) { return phoneNumber; }
                nationalNumber = String.format("%s%s", country.getOptionalNationalPrefix(), nationalNumber);
                phoneNumber.setNationalNumber(Long.valueOf(nationalNumber).longValue());
        }

        return phoneNumber;
    }

}
