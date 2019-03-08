package com.coinninja.coinkeeper.text;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;

import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

public class CurrencyFormattingTextWatcher implements TextWatcher {
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
        };
        currency = new USDCurrency();
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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

        String orig = editable.toString();
        String value = orig;
        boolean endsInDecimal = value.endsWith(".");
        value = value.replace(currency.getSymbol(), "").trim();
        if (value.isEmpty()) {
            value = "0";
        }

        int trailingZeros = calculateTrailingZeros(value);

        if (currency.update(value) && trailingZeros <= currency.getMaxNumSubValues()) {
            updateText(editable, endsInDecimal, trailingZeros);
            //TODO: I don't think we need a success callback
            callback.onValid(currency);
        } else {
            updateText(editable, endsInDecimal, trailingZeros - 1);
            callback.onInvalid(orig);
        }
    }

    private void updateText(Editable editable, boolean endsInDecimal, int trailingZeros) {
        selfChanged = true;
        editable.clear();

        if (endsInDecimal) {
            editable.append(currency.toIncrementalFormat(true));
        } else {
            editable.append(currency.toIncrementalFormat(trailingZeros));
        }

        Selection.setSelection(editable, editable.length());
        selfChanged = false;
    }


    private int calculateTrailingZeros(String value) {
        int trailingZeros = 0;

        String[] split = value.split("\\.");
        if (split.length == 2) {
            trailingZeros = split[1].length();
        }
        return trailingZeros;
    }

    public interface Callback {
        void onValid(Currency currency);

        void onInvalid(String text);
    }
}
