package com.coinninja.coinkeeper.presenter;

import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.FormatNotValidException;

public class LargeCurrencyConverstionPresenter extends CurrencyConversionPresenter {

    private View view;

    public LargeCurrencyConverstionPresenter(Currency currency) {
        super(currency);
    }

    @Override
    public void attach(View view) {
        this.view = view;
        super.attach(view);
    }

    @Override
    protected void initPricing() {
        String value = "";
        if (getEvaluationCurrency() != null) {
            if (getCurrency() instanceof BTCCurrency && !getCurrency().toString().equals("0")) {
                value = getCurrency().toFormattedString();
                view.setStandardWholeValues(value);
                view.updateAlternitiveCurrency(getCurrency().toUSD(getEvaluationCurrency()).toFormattedCurrency());
                if (value.indexOf(".") > 0) {
                    currentMode = Mode.RIGHT_OF_DELIM;
                }
            }
        }
    }

    @Override
    protected void handleSubBack() {
        popOffSub();

        String current = view.getWholeValues();
        if (current.indexOf(".") < 0) {
            currentMode = Mode.LEFT_OF_DELIM;
        }
    }

    protected void popOffSub() {
        popOffWhole();
    }

    protected void appendToSub(String value) {
        String current = view.getWholeValues();
        setCurrency(new BTCCurrency(current));
        if (current.indexOf(".") < 0) {
            value = "." + value;
            hideDelimeter();
        } else {
            String right = current.substring(current.indexOf(".") + 1, current.length());

            if (right.length() >= getCurrency().getMaxNumSubValues()) {
                view.invalidInput();
                return;
            }
        }

        appendToWhole(value);
    }

    @Override
    protected void showDelimeter() {
        view.showStandardDelimeterLarge();
    }

    @Override
    protected void popOffWhole() {
        String value = view.getWholeValues().replace(",", "");
        value = value.substring(0, value.length() - 1);
        if (value.isEmpty()) {
            value = "0";
        }

        showUpdatedValue(value);
    }

    @Override
    protected void appendToWhole(String value) {
        value = new StringBuilder(view.getWholeValues()).append(value).toString();
        if ("00".equals(value)) {
            view.invalidInput();
            return;
        } else if (value.startsWith("0") && !value.startsWith("0.")) {
            value = value.substring(1, value.length());
        }
        showUpdatedValue(value);
    }

    @Override
    public String getCompleteValue() {
        return view.getWholeValues();
    }

    @Override
    protected void showUpdatedValue(String value) {
        try {
            if (value.indexOf(".") >= 0) {
                String left = value.substring(0, value.indexOf("."));
                String right = value.substring(value.indexOf("."), value.length());
                if (!left.isEmpty()) {
                    setCurrency(new BTCCurrency(left));
                    left = getCurrency().toFormattedString();
                }
                value = left + right;
                view.setStandardWholeValues(value);
                setCurrency(new BTCCurrency(value));
            } else {
                setCurrency(new BTCCurrency(value));
                view.setStandardWholeValues(getCurrency().toFormattedString());
            }
            convertCurrency();
        } catch (FormatNotValidException ex) {
            view.invalidInput();
        }
    }

}
