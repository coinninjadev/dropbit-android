package com.coinninja.coinkeeper.util.currency;

import java.math.BigDecimal;

public interface Currency {

    String getSymbol();

    String getFormat();

    String getCurrencyFormat();

    void setCurrencyFormat(String format);

    int getMaxNumSubValues();

    int getMaxNumWholeValues();

    long toLong();

    String toFormattedString();

    String toFormattedCurrency();

    String toIncrementalFormat();

    String toIncrementalFormat(int trailingZeros);

    String toIncrementalFormat(boolean endingDecimal);

    BigDecimal toBigDecimal();

    BTCCurrency toBTC(Currency conversionValue);

    USDCurrency toUSD(Currency conversionValue);

    boolean update(String formattedValue);

    boolean isValid();

    boolean validate(String candidate);

    boolean isZero();

    boolean isCrypto();

    boolean isFiat();
}
