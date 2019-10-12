package com.coinninja.coinkeeper.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import app.dropbit.commons.currency.CryptoCurrency;
import app.dropbit.commons.currency.Currency;
import app.dropbit.commons.currency.FiatCurrency;

public class DefaultCurrencies implements Parcelable {
    public static final Creator<DefaultCurrencies> CREATOR = new Creator<DefaultCurrencies>() {
        @Override
        public DefaultCurrencies createFromParcel(Parcel in) {
            return new DefaultCurrencies(in);
        }

        @Override
        public DefaultCurrencies[] newArray(int size) {
            return new DefaultCurrencies[size];
        }
    };
    private final Currency primaryCurrency;
    private final Currency secondaryCurrency;

    public DefaultCurrencies(Currency primaryCurrency, Currency secondaryCurrency) {
        this.primaryCurrency = primaryCurrency;
        this.secondaryCurrency = secondaryCurrency;
    }

    protected DefaultCurrencies(Parcel in) {
        primaryCurrency = in.readParcelable(Currency.class.getClassLoader());
        secondaryCurrency = in.readParcelable(Currency.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(primaryCurrency, flags);
        dest.writeParcelable(secondaryCurrency, flags);
    }

    public FiatCurrency getFiat() {
        return getPrimaryCurrency().isFiat() ?
                (FiatCurrency) getPrimaryCurrency() :
                (FiatCurrency) getSecondaryCurrency();
    }

    public CryptoCurrency getCrypto() {
        return getPrimaryCurrency().isCrypto() ?
                (CryptoCurrency) getPrimaryCurrency() :
                (CryptoCurrency) getSecondaryCurrency();
    }

    public Currency getPrimaryCurrency() {
        return primaryCurrency;
    }

    public Currency getSecondaryCurrency() {
        return secondaryCurrency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryCurrency, secondaryCurrency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultCurrencies that = (DefaultCurrencies) o;
        return primaryCurrency.toLong() == that.primaryCurrency.toLong() &&
                secondaryCurrency.toLong() == that.secondaryCurrency.toLong();
    }
}
