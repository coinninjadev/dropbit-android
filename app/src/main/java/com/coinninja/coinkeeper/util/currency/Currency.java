package com.coinninja.coinkeeper.util.currency;

import android.os.Parcelable;

import java.math.BigDecimal;

public interface Currency extends Parcelable {

    String getSymbol();

    String getFormat();

    String getCurrencyFormat();

    void setCurrencyFormat(String format);

    int getMaxNumSubValues();

    int getMaxNumWholeValues();

    String getIncrementalFormat();

    long toLong();

    String toFormattedString();

    String toFormattedCurrency();

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
