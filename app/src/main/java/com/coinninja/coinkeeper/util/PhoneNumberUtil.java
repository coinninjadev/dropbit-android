package com.coinninja.coinkeeper.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

public class PhoneNumberUtil {

    private final com.google.i18n.phonenumbers.PhoneNumberUtil _util;

    public PhoneNumberUtil() {
        this(com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance());
    }

    @Inject
    public PhoneNumberUtil(com.google.i18n.phonenumbers.PhoneNumberUtil _util) {
        this._util = _util;
    }

    public String i18Formatted(Phonenumber.PhoneNumber phoneNumber) {
        if (phoneNumber == null) return null;
        return _util.format(phoneNumber, com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    public Phonenumber.PhoneNumber toPhoneNumber(CNPhoneNumber phoneNumber) {
        if (phoneNumber == null || phoneNumber.getPhoneNumber() == null || phoneNumber.getPhoneNumber().isEmpty()) {
            return null;
        }
        return attemptToConvertStringIntoPhoneNumber(phoneNumber.getCountryCode(), phoneNumber.getPhoneNumber());
    }

    public int getCountryCodeForRegion() {
        return _util.getCountryCodeForRegion(CoinKeeperApplication.appComponent.getLocale().getCountry());
    }

    @Nullable
    public Phonenumber.PhoneNumber toPhoneNumber(@NonNull String number) {
        if (number.isEmpty()) return null;

        number = patchNumberWithCountryCode(number);
        Phonenumber.PhoneNumber phoneNumber;

        try {
            phoneNumber = _util.parse(number, CoinKeeperApplication.appComponent.getLocale().getCountry());
        } catch (NumberParseException e) {
            e.printStackTrace();
            phoneNumber = null;
        }
        return phoneNumber;
    }

    public Phonenumber.PhoneNumber toPhoneNumber(int countryCode, String number) {
        if (number == null) return null;
        return attemptToConvertStringIntoPhoneNumber(countryCode, number);
    }

    public String toNationalDisplayText(Phonenumber.PhoneNumber phoneNumber) {
        if (phoneNumber == null) return "";
        return _util.format(phoneNumber, com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
    }

    public String toInternationalDisplayText(Phonenumber.PhoneNumber phoneNumber) {
        if (phoneNumber == null) return "";
        return _util.format(phoneNumber, com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }

    public boolean isValidNumber(String number) {
        if (number == null || number.isEmpty() || _util == null) {
            return false;
        }

        Phonenumber.PhoneNumber phoneNumber = toPhoneNumber(number);

        if (phoneNumber == null) {
            return false;
        }

        return _util.isValidNumber(phoneNumber);
    }

    Phonenumber.PhoneNumber attemptToConvertStringIntoPhoneNumber(int countryCode, String phoneNumber) {
        Phonenumber.PhoneNumber number = new Phonenumber.PhoneNumber();
        number.setCountryCode(countryCode);

        if (phoneNumber.startsWith("+1")) {
            phoneNumber = phoneNumber.replace("+1", "");
        }

        return number.setNationalNumber(Long.parseLong(phoneNumber));
    }

    private String patchNumberWithCountryCode(String number) {
        if (number.startsWith("+")) return number;
        String temp;

        Set<Integer> supportedCallingCodes = _util.getSupportedCallingCodes();
        for (Integer code : supportedCallingCodes) {
            if (number.startsWith(String.valueOf(code))) {
                temp = String.format("+%s", number);
                if (isValidNumber(temp)) {
                    return temp;
                }
            }
        }

        if (!number.startsWith("+")) {
            Locale locale = CoinKeeperApplication.appComponent.getLocale();
            int countryCode = _util.getCountryCodeForRegion(locale.getCountry());
            return String.format("+%s%s", countryCode, number);
        }

        return number;
    }
}
