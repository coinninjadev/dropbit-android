package com.coinninja.coinkeeper.text;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;

import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormatSymbols;

import javax.inject.Inject;

import static java.lang.String.format;

public class CurrencyFormattingTextWatcher implements TextWatcher {
    private final String decimalSeparator;
    private final String decimalPattern;
    private final char groupingSeparator;
    private Callback callback;
    private Currency currency;
    private boolean selfChanged;

    @Inject
    public CurrencyFormattingTextWatcher() {
        callback = new Callback() {
            @Override
            public void onValid(Currency currency) {
            }

            @Override
            public void onInvalid(String text) {
            }

            @Override
            public void onZeroed() {
            }

            @Override
            public void onInput() {

            }
        };
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalSeparator = String.valueOf(decimalFormatSymbols.getDecimalSeparator());
        groupingSeparator = decimalFormatSymbols.getGroupingSeparator();
        decimalPattern = format("\\%1$s", decimalSeparator);
        setCurrency(new USDCurrency());
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (selfChanged) {
            return;
        }

        String value = editable.toString();

        if (isValid(value) && currency.update(value)) {
            updateText(editable, value);
            callback.onValid(currency);
        } else {
            updateText(editable, undoChange(value));
            callback.onInvalid(value);
        }


    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        try {
            this.currency = currency.getClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            this.currency = currency;
        }
        this.currency.setCurrencyFormat(currency.getIncrementalFormat());
    }

    @NotNull
    private String undoChange(String value) {
        value = value.substring(0, value.length() - 1);
        if (!currency.update(value)) {
            value = undoChange(value);
        }
        return value;
    }

    private boolean isValid(String value) {
        return !containsMultipleDecimals(value) &&
                !hasGroupingAfterPrecision(value);
    }

    private boolean hasGroupingAfterPrecision(String value) {
        int locationOfDecimalSeparator = value.lastIndexOf(decimalSeparator);
        return (locationOfDecimalSeparator != -1) && (locationOfDecimalSeparator < value.lastIndexOf(groupingSeparator));
    }

    private boolean containsMultipleDecimals(String value) {
        return value.indexOf(decimalSeparator) != value.lastIndexOf(decimalSeparator);
    }

    private void updateText(Editable editable, String value) {
        selfChanged = true;
        editable.clear();

        String text = formatIncrement(value).trim();
        editable.append(text);
        checkZero(text);

        Selection.setSelection(editable, text.length());
        selfChanged = false;
    }

    private void checkZero(String text) {
        String value = text;
        value = value.replace(currency.getSymbol(), "");
        if (value.length() <= 1 && "0".equals(value)) {
            callback.onZeroed();
        } else {
            callback.onInput();
        }
    }

    private String formatIncrement(String value) {
        String formatted = currency.toFormattedCurrency();

        if (value.endsWith(decimalSeparator))
            formatted += decimalSeparator;

        formatted = addTrailingZeros(value, formatted);

        return formatted;
    }

    private String addTrailingZeros(String value, String formatted) {
        int trailingZeros = calculateTrailingZeros(value);

        if (trailingZeros > 0 && !formatted.contains(decimalSeparator)) {
            formatted += decimalSeparator;
        }

        for (int i = 0; i < trailingZeros; i++) {
            formatted += "0";
        }
        return formatted;
    }

    private int calculateTrailingZeros(String value) {
        int trailingZeros = 0;
        char zero = '0';

        String[] split = value.split(decimalPattern);
        if (split.length == 2) {
            String d = split[1];
            for (char c : d.toCharArray()) {
                if (zero == c) {
                    trailingZeros += 1;
                } else {
                    trailingZeros = 0;
                }
            }
            trailingZeros = Math.min(trailingZeros, currency.getMaxNumSubValues());
        }
        return trailingZeros;
    }

    public interface Callback {
        void onValid(Currency currency);

        void onInvalid(String text);

        void onZeroed();

        void onInput();
    }
}
