package com.coinninja.coinkeeper.view.widget.phonenumber;

import java.util.Locale;
import java.util.Objects;

public class CountryCodeLocale implements Comparable<CountryCodeLocale> {
    private Locale locale;

    private int countryCode;

    public CountryCodeLocale(Locale locale, int countryCode) {
        this.locale = locale;
        this.countryCode = countryCode;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public String getEmoji() {
        String countryCode = locale.getCountry();
        int firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
    }

    public String getDisplayName() {
        return locale.getDisplayCountry();
    }

    public String getDisplayCountryCode() {
        return String.format("+%s", getCountryCode());
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryCodeLocale that = (CountryCodeLocale) o;
        return countryCode == that.countryCode &&
                Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale.getCountry(), countryCode);
    }

    @Override
    public int compareTo(CountryCodeLocale o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }
}
