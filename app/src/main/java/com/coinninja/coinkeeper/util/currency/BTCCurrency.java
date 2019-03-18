package com.coinninja.coinkeeper.util.currency;

import java.math.BigDecimal;

public class BTCCurrency extends BaseCurrency implements Currency {
    public static final String ALT_CURRENCY_FORMAT = "#,##0.00000000 BTC";
    static final int WHOLE_NUM_MAX = 8;
    static final int SUB_NUM_MAX = 8;
    public static final long MAX_SATOSHI = 2099999997690000L;
    static final String SYMBOL = "\u20BF";
    static final String DEFAULT_CURRENCY_FORMAT = String.format("%s #,##0.########", SYMBOL);
    static final String INCREMENTAL_FORMAT = String.format("%s #,##0.########", SYMBOL);
    static final String STRING_FORMAT = "#,##0.########";

    private String currencyFormat = DEFAULT_CURRENCY_FORMAT;

    public BTCCurrency() {
        super();
    }

    public BTCCurrency(long initialValue) {
        super(initialValue);
    }

    public BTCCurrency(String initialValue) {
        super(initialValue);
    }

    public BTCCurrency(double initialValue) {
        super(initialValue);
    }

    public BTCCurrency(BigDecimal initialValue) {
        super(initialValue);
    }

    @Override
    public String getFormat() {
        return STRING_FORMAT;
    }

    @Override
    public String getIncrementalFormat() {
        return INCREMENTAL_FORMAT;
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
    public int getMaxNumSubValues() {
        return SUB_NUM_MAX;
    }

    @Override
    public int getMaxNumWholeValues() {
        return WHOLE_NUM_MAX;
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    @Override
    public boolean isValid() {
        return (toSatoshis() >= 0 && toSatoshis() <= MAX_SATOSHI);
    }

    @Override
    protected long getMaxLongValue() {
        return MAX_SATOSHI;
    }

    public long toSatoshis() {
        if (isZero()) {
            return 0L;
        }

        BigDecimal myBTCValue = toBigDecimal();
        BigDecimal mySatoshiValue = myBTCValue.multiply(new BigDecimal(100000000));

        return mySatoshiValue.longValue();
    }

    public String toUriFormattedString() {
        BTCCurrency btc = new BTCCurrency(this.toSatoshis());
        return String.valueOf(btc.value);
    }

}
