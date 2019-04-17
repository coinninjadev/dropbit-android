package com.coinninja.coinkeeper.util.currency;


import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

public class USDCurrency extends BaseCurrency implements FiatCurrency, Parcelable {
    private static final int WHOLE_NUM_MAX = 10;
    private static final int SUB_NUM_MAX = 2;
    public static final String SYMBOL = "$";
    static long MAX_DOLLAR_AMOUNT = Long.MAX_VALUE;
    private String currencyFormat = "$#,##0.00";
    private static final String incrementalFormat = "$#,##0.##";

    public USDCurrency() {
        super();
    }

    public USDCurrency(long initialValue) {
        super(initialValue);
    }

    public USDCurrency(String initialValue) {
        super(initialValue);
    }

    public USDCurrency(double initialValue) {
        super(initialValue);
    }

    public USDCurrency(BigDecimal initialValue) {
        super(initialValue);
    }

    protected USDCurrency(Parcel in) {
        this(in.readLong());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(toLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<USDCurrency> CREATOR = new Creator<USDCurrency>() {
        @Override
        public USDCurrency createFromParcel(Parcel in) {
            return new USDCurrency(in);
        }

        @Override
        public USDCurrency[] newArray(int size) {
            return new USDCurrency[size];
        }
    };

    public static void setMaxLimit(USDCurrency currency) {
        try {
            MAX_DOLLAR_AMOUNT = new BTCCurrency(BTCCurrency.MAX_SATOSHI).toUSD(currency).toLong();
        } catch (FormatNotValidException ex) {
            MAX_DOLLAR_AMOUNT = Long.MAX_VALUE;
        }

        if (MAX_DOLLAR_AMOUNT == 0L) {
            MAX_DOLLAR_AMOUNT = Long.MAX_VALUE;
        }
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    @Override
    public String getFormat() {
        return "#,###.##";
    }

    @Override
    public String getCurrencyFormat() {
        return currencyFormat;
    }

    @Override
    public void setCurrencyFormat(String format) {
        currencyFormat = format;
    }

    @Override
    public int getMaxNumWholeValues() {
        return WHOLE_NUM_MAX;
    }

    @Override
    public String getIncrementalFormat() {
        return incrementalFormat;
    }

    @Override
    public boolean isValid() {
        return toLong() <= getMaxLongValue();
    }

    @Override
    public long getMaxLongValue() {
        return MAX_DOLLAR_AMOUNT;
    }

    @Override
    public int getMaxNumSubValues() {
        return SUB_NUM_MAX;
    }

}
