package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.google.i18n.phonenumbers.Phonenumber;

import javax.inject.Inject;

public class PhoneNumberUtil {

    @Inject
    public PhoneNumberUtil() {
    }

    public String i18Formatted(Phonenumber.PhoneNumber phoneNumber) {
        if (phoneNumber == null) return null;
        return "+" + phoneNumber.getCountryCode() + String.valueOf(phoneNumber.getNationalNumber());
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

    public Phonenumber.PhoneNumber toPhoneNumber(String number) {
        if (number == null || number.isEmpty()) return null;
        return toPhoneNumber(1, number.replace("+1", ""));
    }

    public Phonenumber.PhoneNumber toPhoneNumber(int countryCode, String number) {
        if (number == null) return null;
        return attemptToConvertStringIntoPhoneNumber(countryCode, number);
    }

    public String toNationalDisplayText(Phonenumber.PhoneNumber phoneNumber) {
        if (phoneNumber == null) return null;

        com.google.i18n.phonenumbers.PhoneNumberUtil instance = com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();
        return instance.format(phoneNumber, com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
    }

    public String getContactOrNumber(Contact contact) {
        if (contact == null) return "";
        return (contact.getDisplayName() != null && !"".equals(contact.getDisplayName())) ? contact.getDisplayName() : contact.getPhoneNumber().toNationalDisplayText();
    }
}
