package app.dropbit.commons.currency;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

import app.dropbit.commons.R;

public class BTCCurrency extends BaseCurrency implements CryptoCurrency, Parcelable {
    public static final String ALT_CURRENCY_FORMAT = "#,##0.00000000 BTC";
    public static final long MAX_SATOSHI = 2099999997690000L;
    public static final String SYMBOL = "\u20BF";
    public static final Creator<BTCCurrency> CREATOR = new Creator<BTCCurrency>() {
        @Override
        public BTCCurrency createFromParcel(Parcel in) {
            return new BTCCurrency(in);
        }

        @Override
        public BTCCurrency[] newArray(int size) {
            return new BTCCurrency[size];
        }
    };
    static final int SUB_NUM_MAX = 8;
    private static final int WHOLE_NUM_MAX = 8;
    private static final String DEFAULT_CURRENCY_FORMAT = String.format("%s #,##0.########", SYMBOL);
    private static final String INCREMENTAL_FORMAT = NO_SYMBOL_FORMAT;
    private static final String STRING_FORMAT = "#,##0.########";
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

    protected BTCCurrency(Parcel in) {
        this(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(toLong());
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    @Override
    public String getFormat() {
        return STRING_FORMAT;
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
    public String getIncrementalFormat() {
        return INCREMENTAL_FORMAT;
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
        BTCCurrency btc = new BTCCurrency(toSatoshis());
        return String.valueOf(btc.value);
    }

    @Override
    public Drawable getSymbolDrawable(Context context) {
        return context.getDrawable(R.drawable.ic_btc_icon);
    }

    @Override
    public FiatCurrency toFiat(FiatCurrency conversionFiat) {
        return toUSD(conversionFiat);
    }
}