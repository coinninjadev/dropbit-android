package com.coinninja.coinkeeper.text;

import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class PhoneNumberFormattingTextWatcher implements TextWatcher {

    public static final int MIN_PHONE_NUMBER_LENGTH = 6;
    private final Map<String, String> discardableValues;
    private Locale locale;
    private Callback callback;
    private boolean selfChanged = false;
    private AsYouTypeFormatter formatter;
    private PhoneNumberUtil phoneNumberUtil;
    private String formattedTemplateNumber;

    private Phonenumber.PhoneNumber templateNumber;
    private Phonenumber.PhoneNumber phoneNumber;
    private boolean stopWatching = false;
    private String i18nMatcher;
    private String ccMatcher;

    public PhoneNumberFormattingTextWatcher(@NotNull Locale locale, @NotNull Callback callback) {
        this.callback = callback;
        Map<String, String> aMap = new HashMap<>();
        aMap.put(" ", " ");
        aMap.put("(", "");
        aMap.put(")", "");
        aMap.put("-", "-");
        discardableValues = Collections.unmodifiableMap(aMap);
        phoneNumberUtil = PhoneNumberUtil.getInstance();
        phoneNumber = new Phonenumber.PhoneNumber();
        updateLocale(locale);
    }

    public void updateLocale(Locale locale) {
        this.locale = locale;
        templateNumber = phoneNumberUtil.getExampleNumberForType(locale.getCountry(),
                PhoneNumberUtil.PhoneNumberType.MOBILE);
        phoneNumber.setCountryCode(templateNumber.getCountryCode());
        formatter = phoneNumberUtil.getAsYouTypeFormatter(locale.getCountry());
        i18nMatcher = String.format("+%s", templateNumber.getCountryCode());
        ccMatcher = String.format("%s", templateNumber.getCountryCode());
        formattedTemplateNumber = phoneNumberUtil.format(templateNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (selfChanged) {
            return;
        }

        stopWatching = containsNonDialableDigit(s.toString().toCharArray());
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
        if (text.length() < MIN_PHONE_NUMBER_LENGTH) { return; }

        try {
            phoneNumber = phoneNumberUtil.parse(text, locale.getCountry());
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                callback.onPhoneNumberValid(phoneNumber);
            } else if (formattedTemplateNumber.length() == text.length()) {
                callback.onPhoneNumberInValid(text);
            }
        } catch (NumberParseException e) {
            e.printStackTrace();
            callback.onPhoneNumberInValid(text);
        }
    }

    private String prependCountryCode(String text) {
        if (text.startsWith(i18nMatcher)) {
            return text;
        } else if (text.startsWith(ccMatcher) && text.length() >= ccMatcher.length()) {
            return prependCountryCode(text.replaceFirst(ccMatcher, ""));
        } else if (!text.startsWith(i18nMatcher) && text.length() >= i18nMatcher.length()) {
            return String.format("%s%s", i18nMatcher, text);
        } else if (!text.startsWith("+") && !text.startsWith(ccMatcher.substring(0, 1))) {
            return String.format("%s%s", i18nMatcher, text);
        } else {
            return i18nMatcher;
        }
    }

    private boolean containsNonDialableDigit(char[] chars) {
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
        s = prependCountryCode(s);
        formatter.clear();
        String text = "";
        String current = s;
        current = stripSeparators(current);
        int maxLen = formattedTemplateNumber != null ? PhoneNumberUtils.stripSeparators(formattedTemplateNumber).length() : 0;

        if (maxLen <= current.length()) {
            current = current.substring(0, maxLen);
        }

        for (char c : current.toCharArray()) {
            text = formatter.inputDigit(c);
        }

        return text;
    }

    private String stripSeparators(String text) {
        text = PhoneNumberUtils.stripSeparators(text);
        for (String key : discardableValues.keySet()) {
            text = text.replace(key, discardableValues.get(key));
        }
        return text;
    }


    public interface Callback {
        void onPhoneNumberValid(Phonenumber.PhoneNumber phoneNumber);

        void onPhoneNumberInValid(String text);
    }
}
