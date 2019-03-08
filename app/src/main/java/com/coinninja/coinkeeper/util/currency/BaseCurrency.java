package com.coinninja.coinkeeper.util.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

abstract class BaseCurrency implements Currency {
    protected BigDecimal value;

    BaseCurrency() {
        value = new BigDecimal("0");
        value = value.stripTrailingZeros();
    }

    //TODO: the string constructor is ambiguous and should be removed in favor of double or long
    BaseCurrency(String initialValue) {
        value = fromFormattedDecimalString(initialValue);
        if (!isValid())
            throw new FormatNotValidException();
    }

    protected BigDecimal fromFormattedDecimalString(String initialValue) {
        BigDecimal scrubbed = scrubInput(initialValue);
        return scale(scrubbed);
    }

    protected BigDecimal scrubInput(String initialValue) {
        //todo: regex instead of 3 replacements?
        initialValue = initialValue.replace(",", "")
                .replace(" ", "")
                .replace(getSymbol(), "");
        return new BigDecimal(initialValue);
    }

    BaseCurrency(long initialValue) {
        value = scale(new BigDecimal(initialValue).movePointLeft(getMaxNumSubValues()));
    }

    private BigDecimal scale(BigDecimal initial) {
        return initial.stripTrailingZeros().setScale(getMaxNumSubValues(), RoundingMode.HALF_DOWN);
    }

    BaseCurrency(double initialValue) {
        value = new BigDecimal(initialValue).stripTrailingZeros().
                setScale(getMaxNumSubValues(), RoundingMode.HALF_UP);
        if (!isValid())
            throw new FormatNotValidException();
    }

    BaseCurrency(BigDecimal initialValue) {
        value = initialValue.stripTrailingZeros().
                setScale(getMaxNumSubValues(), RoundingMode.HALF_UP);
        if (!isValid())
            throw new FormatNotValidException();
    }

    @Override
    public abstract int getMaxNumSubValues();

    @Override
    public abstract int getMaxNumWholeValues();

    protected abstract String getIncrementalFormat();


    @Override
    public String getSymbol() {
        return "";
    }

    @Override
    public String getFormat() {
        throw new RuntimeException("Not Implemented!");
    }

    @Override
    public boolean isValid() {
        throw new RuntimeException("Not Implemented!");
    }

    @Override
    public boolean validate(String candidate) {
        try {
            BigDecimal bigDecimal = scrubInput(candidate);
            long longValue = bigDecimal.movePointRight(getMaxNumSubValues()).longValueExact();
            return getMaxLongValue() > longValue;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean update(String formattedValue) {
        if (validate(formattedValue)) {
            value = fromFormattedDecimalString(formattedValue);
            return true;
        }
        return false;
    }

    protected abstract long getMaxLongValue();

    @Override
    public String getCurrencyFormat() {
        throw new RuntimeException("Not Implemented!");
    }

    @Override
    public BTCCurrency toBTC(Currency conversionValue) {
        if (conversionValue == null || conversionValue.isZero()) {
            return new BTCCurrency();
        }

        if (this instanceof BTCCurrency) {
            return (BTCCurrency) this;
        }

        return new BTCCurrency(toBigDecimal().divide(conversionValue.toBigDecimal(),
                BTCCurrency.SUB_NUM_MAX, RoundingMode.HALF_UP));
    }

    @Override
    public USDCurrency toUSD(Currency conversionValue) {
        if (conversionValue == null || conversionValue.isZero()) {
            return new USDCurrency();
        }

        if (this instanceof USDCurrency) {
            return (USDCurrency) this;
        }

        return new USDCurrency(toBigDecimal().multiply(conversionValue.toBigDecimal()));
    }

    @Override
    public BigDecimal toBigDecimal() {
        return value;
    }

    @Override
    public String toString() {
        return value.setScale(getMaxNumSubValues(), RoundingMode.HALF_UP).
                stripTrailingZeros().toPlainString();
    }

    @Override
    public String toFormattedString() {
        return new DecimalFormat(getFormat()).format(value);
    }

    @Override
    public String toFormattedCurrency() {
        return new DecimalFormat(getCurrencyFormat()).
                format(value.setScale(getMaxNumSubValues(), RoundingMode.HALF_UP));

    }

    @Override
    public String toIncrementalFormat(int trailingZeros) {
        int requestedZeros = Math.min(trailingZeros, getMaxNumSubValues());

        String formatted = new DecimalFormat(getIncrementalFormat()).format(value);
        if (requestedZeros > 0) {
            String[] split = formatted.split("\\.");
            if (split.length == 2) {
                requestedZeros = requestedZeros - split[1].length();
            } else {
                formatted += ".";
            }
            for (int i = 0; i < requestedZeros; i++) {
                formatted += "0";
            }
        }
        return formatted;
    }

    @Override
    public String toIncrementalFormat() {
        return toIncrementalFormat(0);
    }

    @Override
    public String toIncrementalFormat(boolean endingDecimal) {
        String result = toIncrementalFormat(0);
        if (endingDecimal) {
            //TODO: this works until we get comma delimited decimals
            result += ".";
        }
        return result;
    }

    @Override
    public boolean isZero() {
        BigDecimal bigDecimal = toBigDecimal();
        return bigDecimal.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public long toLong() {
        long result = 0L;

        if (value != null)
            result = value.movePointRight(getMaxNumSubValues()).stripTrailingZeros().setScale(getMaxNumSubValues(), RoundingMode.HALF_UP).longValue();

        return result;
    }
}
