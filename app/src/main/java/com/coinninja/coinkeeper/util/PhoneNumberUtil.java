package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private Phonenumber.PhoneNumber attemptToConvertStringIntoPhoneNumber(int countryCode, String phoneNumber) {
        Phonenumber.PhoneNumber number = new Phonenumber.PhoneNumber();
        number.setCountryCode(countryCode);

        if (phoneNumber.contains("+1")) {
            throw new RuntimeException("this shouldn't happen");
        }
        return number.setNationalNumber(Long.parseLong(phoneNumber));
    }

    public Phonenumber.PhoneNumber toPhoneNumber(CNPhoneNumber phoneNumber) {
        if (phoneNumber == null || phoneNumber.getPhoneNumber() == null || phoneNumber.getPhoneNumber().isEmpty()) {
            return null;
        }
        return attemptToConvertStringIntoPhoneNumber(phoneNumber.getCountryCode(), phoneNumber.getPhoneNumber());
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

    public int getCountryCodeForRegion() {
        //TODO get local from provider
        Locale locale = new Locale("en", "US");
        com.google.i18n.phonenumbers.PhoneNumberUtil instance = com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();
        return instance.getCountryCodeForRegion(locale.getCountry());
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

    public String getContactOrNumber(Contact contact) {
        if (contact == null) return "";
        return (contact.getDisplayName() != null && !"".equals(contact.getDisplayName())) ? contact.getDisplayName() : contact.getPhoneNumber().toNationalDisplayText();
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

    public boolean isValidNumber(String number) {
        return _util.isValidNumber(toPhoneNumber(number));
    }
}
