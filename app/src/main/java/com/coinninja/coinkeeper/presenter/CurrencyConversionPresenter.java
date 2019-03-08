package com.coinninja.coinkeeper.presenter;


import com.coinninja.coinkeeper.util.Keys;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;


public class CurrencyConversionPresenter {

    private View view;
    private Currency currency;
    protected Mode currentMode = Mode.LEFT_OF_DELIM;
    private Currency evaluationCurrency;
    private Currency currentState;

    public CurrencyConversionPresenter(Currency currency) {
        this.currency = currency;
        evaluationCurrency = null;
    }

    public void attach(View view) {
        this.view = view;
        if (currency instanceof BTCCurrency) {
            view.setBTCCurrencySymbol(currency.getSymbol());
        } else {
            view.setStandardCurrencySymbol(currency.getSymbol());
        }
        initPricing();
    }

    protected void initPricing() {
        String value = "";

        if (evaluationCurrency != null) {
            if (currency instanceof USDCurrency && !currency.toString().equals("0") && !currency.toString().equals("0.00")) {
                value = currency.toFormattedCurrency().replace("$", "");
                String left = value.substring(0, value.indexOf("."));
                String right = value.substring(value.indexOf(".") + 1, value.length());
                view.setStandardWholeValues(left);
                view.showStandardDelimeter();
                view.setStandardSubValues(right);
                view.updateAlternitiveCurrency(currency.toBTC(evaluationCurrency).toFormattedCurrency());
                currentMode = Mode.RIGHT_OF_DELIM;
            } else if (currency instanceof BTCCurrency && !currency.toString().equals("0")) {
                currentMode = Mode.RIGHT_OF_DELIM;
            } else if (currency instanceof USDCurrency) {
                view.updateAlternitiveCurrency(currency.toBTC(evaluationCurrency).toFormattedCurrency());
            }
        }
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    private void handleLeftMode(Keys which) {
        switch (which) {
            case BACK:
                String value = view.getWholeValues();
                if (value.isEmpty() || "0".equals(value)) {
                    view.invalidInput();
                } else {
                    popOffWhole();
                }
                break;
            case DOT:
                currentMode = Mode.RIGHT_OF_DELIM;
                showDelimeter();
                break;
            case CLEAR:
                clearAll();
                break;

            default:
                appendToWhole(which.getValue());
        }
    }

    private void handleRightMode(Keys which) {
        switch (which) {

            case BACK:
                handleSubBack();
                break;

            case DOT:
                view.invalidInput();
                break;

            case CLEAR:
                clearAll();
                break;
            default:
                appendToSub(which.getValue());

        }
    }

    protected void handleSubBack() {
        if (view.getSubValues().isEmpty()) {
            currentMode = Mode.LEFT_OF_DELIM;
            view.hideStandardDelimeter();
        } else {
            popOffSub();
        }
    }

    protected void popOffSub() {
        String value = view.getSubValues();
        value = value.substring(0, value.length() - 1);
        currency = new USDCurrency(getCompleteValue());
        view.setStandardSubValues(value);
        convertCurrency();
    }

    protected void appendToSub(String value) {
        value = new StringBuilder(view.getSubValues()).append(value).toString();

        if (value.length() > currency.getMaxNumSubValues()) {
            view.invalidInput();
        } else {
            view.setStandardSubValues(value);
            currency = new USDCurrency(getCompleteValue());
            convertCurrency();
        }
    }

    protected void popOffWhole() {
        String value = view.getWholeValues();
        value = value.substring(0, value.length() - 1);
        if (value.isEmpty()) {
            value = "0";
        }

        showUpdatedValue(value);
    }

    protected void appendToWhole(String value) {
        value = new StringBuilder(view.getWholeValues()).append(value).toString();
        if ("00".equals(value)) {
            view.invalidInput();
            return;
        } else if (value.startsWith("0")) {
            value = value.substring(1, value.length());
        }

        showUpdatedValue(value);
    }


    protected void showUpdatedValue(String value) {
        if (value.length() > currency.getMaxNumWholeValues()) {
            view.invalidInput();
        } else {
            currency = new USDCurrency(value);
            view.setStandardWholeValues(currency.toFormattedString());
            convertCurrency();
        }

    }

    protected void hideDelimeter() {
        view.hideStandardDelimeter();
    }


    private void clearAll() {
        currentMode = Mode.LEFT_OF_DELIM;
        view.setStandardWholeValues("0");
        view.setStandardSubValues("");
        hideDelimeter();
        resetCurrency();
        convertCurrency();
        view.invalidInput();
    }

    private void resetCurrency() {
        if (currency instanceof USDCurrency) {
            currency = new USDCurrency();
        } else {
            currency = new BTCCurrency();
        }
    }

    protected void convertCurrency() {
        String value;

        if (currency instanceof USDCurrency) {
            value = currency.toBTC(evaluationCurrency).toFormattedCurrency();
        } else {
            value = currency.toUSD(evaluationCurrency).toFormattedCurrency();
        }

        if (view != null) {
            view.updateAlternitiveCurrency(value);
        }
    }

    protected void showDelimeter() {
        view.showStandardDelimeter();
    }

    public void onInput(Keys which) {
        switch (currentMode) {
            case LEFT_OF_DELIM:
                handleLeftMode(which);
                break;
            case RIGHT_OF_DELIM:
                handleRightMode(which);
                break;
        }
    }

    public void setEvaluationCurrency(Currency evaluationCurrency) {
        this.evaluationCurrency = evaluationCurrency;
        if (evaluationCurrency != null) {
            convertCurrency();
        }
    }

    public Currency getEvaluationCurrency() {
        return evaluationCurrency;
    }

    public String getCompleteValue() {
        return new StringBuilder(view.getWholeValues()).append(".")
                .append(view.getSubValues()).toString().toString();
    }

    public Currency getCurrentState() {
        if (currency instanceof USDCurrency) {
            currentState = new USDCurrency(getCompleteValue());
        } else {
            currentState = new BTCCurrency(getCompleteValue());
        }
        return currentState;
    }

    public enum Mode {
        LEFT_OF_DELIM, RIGHT_OF_DELIM
    }

    public interface View {
        void setBTCCurrencySymbol(String symbol);

        void setStandardCurrencySymbol(String symbol);

        void setStandardWholeValues(String wholeValues);

        void invalidInput();

        void showStandardDelimeter();

        void showStandardDelimeterLarge();

        void setStandardSubValues(String s);

        void hideStandardDelimeter();

        void updateAlternitiveCurrency(String value);

        String getWholeValues();

        String getSubValues();
    }
}
