package com.coinninja.coinkeeper.text;

import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class PhoneNumberFormattingTextWatcher implements TextWatcher {

    private final Map<String, String> discardableValues;
    private final Locale locale;
    private Callback callback;
    private boolean selfChanged = false;
    private AsYouTypeFormatter formatter;
    private PhoneNumberUtil phoneNumberUtil;
    private String formattedTemplateNumber;

    private Phonenumber.PhoneNumber templateNumber;
    private Phonenumber.PhoneNumber phoneNumber;
    private boolean stopWatching = false;

    public PhoneNumberFormattingTextWatcher(@NotNull Locale locale, @NotNull Callback callback) {
        this.locale = locale;
        this.callback = callback;
        Map<String, String> aMap = new HashMap<>();
        aMap.put(" ", "");
        aMap.put("-", "-");
        aMap.put("(", "");
        aMap.put(") ", "-");
        aMap.put(")", "-");
        discardableValues = Collections.unmodifiableMap(aMap);
        phoneNumberUtil = PhoneNumberUtil.getInstance();
        formatter = phoneNumberUtil.getAsYouTypeFormatter(locale.getCountry());
        templateNumber = phoneNumberUtil.getExampleNumberForType(locale.getCountry(),
                PhoneNumberUtil.PhoneNumberType.MOBILE);
        phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setCountryCode(templateNumber.getCountryCode());
        formattedTemplateNumber = formatInput(String.valueOf(templateNumber.getNationalNumber()));
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (selfChanged) {
            return;
        }

        stopWatching = containsNonDialableDidget(s.toString().toCharArray());
    }

    @Override
    public synchronized void afterTextChanged(Editable s) {
        if (selfChanged || stopWatching) {
            return;
        }

        String text = formatInput(s);
        selfChanged = true;
        s.replace(0, s.length(), text, 0, text.length());
        // another watcher can compete with our change
        if (text.equals(s.toString())) {
            Selection.setSelection(s, text.length());
        }
        selfChanged = false;
        validateNumber(text);
    }

    private void validateNumber(String text) {
        String copy = text;
        copy = PhoneNumberUtils.stripSeparators(text);

        if (String.valueOf(templateNumber.getNationalNumber()).length() != copy.length()) {
            return;
        }

        phoneNumber.setNationalNumber(Long.parseLong(copy));
        if (phoneNumberUtil.isValidNumber(phoneNumber)) {
            callback.onPhoneNumberValid(phoneNumber);
        } else {
            callback.onPhoneNumberInValid(text);
        }
    }

    private boolean containsNonDialableDidget(char[] chars) {
        for (char c : chars) {
            if (!PhoneNumberUtils.isNonSeparator(c) &&
                    !discardableValues.containsKey(String.valueOf(c))) {
                return true;
            }
        }
        return false;
    }

    private String formatInput(Editable s) {
        return formatInput(s.toString());
    }

    private String formatInput(String s) {
        formatter.clear();
        String text = "";
        String current = s;
        current = PhoneNumberUtils.stripSeparators(current);
        current = current.replace("+", "");
        if (beginsWithCountryCode(current)) {
            current = current.substring(1, current.length());
        }

        for (char c : current.toCharArray()) {
            text = formatter.inputDigit(c);

            for (String key : discardableValues.keySet()) {
                text = text.replace(key, discardableValues.get(key));
            }
            if (formattedTemplateNumber != null && formattedTemplateNumber.length() <= text.length()) {
                return text;
            }
        }
        return text;
    }

    private boolean beginsWithCountryCode(String current) {
        return current.startsWith(String.valueOf(phoneNumber.getCountryCode()));
    }

    public interface Callback {
        void onPhoneNumberValid(Phonenumber.PhoneNumber phoneNumber);

        void onPhoneNumberInValid(String text);
    }
}
