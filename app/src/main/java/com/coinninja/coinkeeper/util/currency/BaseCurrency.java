package com.coinninja.coinkeeper.util.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

abstract class BaseCurrency implements Currency {
    private final String stripPattern;
    protected BigDecimal value;
    private String decimalSeparator;

    BaseCurrency() {
        decimalSeparator = String.valueOf(DecimalFormatSymbols.getInstance().getDecimalSeparator());
        stripPattern = String.format("[^0-9 %1$s]|[%1$s](?!\\d)", decimalSeparator);
        value = new BigDecimal("0");
        value = value.stripTrailingZeros();
    }

    BaseCurrency(String initialValue) {
        this();
        value = fromFormattedDecimalString(initialValue);
        if (!isValid())
            throw new FormatNotValidException();
    }

    BaseCurrency(long initialValue) {
        this();
        valueFromLong(initialValue);
    }


    BaseCurrency(double initialValue) {
        this();
        value = new BigDecimal(initialValue).stripTrailingZeros().
                setScale(getMaxNumSubValues(), RoundingMode.HALF_UP);
        if (!isValid())
            throw new FormatNotValidException();
    }

    BaseCurrency(BigDecimal initialValue) {
        this();
        value = initialValue.stripTrailingZeros().
                setScale(getMaxNumSubValues(), RoundingMode.HALF_UP);
        if (!isValid())
            throw new FormatNotValidException();
    }

    @Override
    public String getSymbol() {
        return "";
    }

    @Override
    public String getFormat() {
        throw new RuntimeException("Not Implemented!");
    }

    @Override
    public String getCurrencyFormat() {
        throw new RuntimeException("Not Implemented!");
    }

    @Override
    public abstract int getMaxNumSubValues();

    @Override
    public abstract int getMaxNumWholeValues();

    @Override
    public long toLong() {
        long result = 0L;

        if (value != null)
            result = value.movePointRight(getMaxNumSubValues()).stripTrailingZeros().setScale(getMaxNumSubValues(), RoundingMode.HALF_UP).longValue();

        return result;
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
    public BigDecimal toBigDecimal() {
        return value;
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
    public boolean update(String formattedValue) {
        if (validate(formattedValue)) {
            value = fromFormattedDecimalString(formattedValue);
            return true;
        }
        return false;
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
    public boolean isZero() {
        BigDecimal bigDecimal = toBigDecimal();
        return bigDecimal.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public boolean isCrypto() {
        return this instanceof CryptoCurrency;
    }

    @Override
    public boolean isFiat() {
        return this instanceof FiatCurrency;
    }

    @Override
    public String toString() {
        return value.setScale(getMaxNumSubValues(), RoundingMode.HALF_UP).
                stripTrailingZeros().toPlainString();
    }

    @Override
    public void zero() {
        valueFromLong(0);
    }

    protected BigDecimal fromFormattedDecimalString(String initialValue) {
        BigDecimal scrubbed = scrubInput(initialValue);
        return scale(scrubbed);
    }

    protected BigDecimal scrubInput(String initialValue) {
        initialValue = initialValue.replace(getSymbol(), "").trim();
        initialValue = initialValue.replaceAll(stripPattern, "");
        initialValue = initialValue.replace(decimalSeparator, ".");
        if (initialValue.isEmpty()) {
            return new BigDecimal(0L);
        }
        return new BigDecimal(initialValue);
    }

    private void valueFromLong(long initialValue) {
        value = scale(new BigDecimal(initialValue).movePointLeft(getMaxNumSubValues()));
    }

    protected abstract long getMaxLongValue();

    private BigDecimal scale(BigDecimal initial) {
        return initial.stripTrailingZeros().setScale(getMaxNumSubValues(), RoundingMode.HALF_DOWN);
    }
}
