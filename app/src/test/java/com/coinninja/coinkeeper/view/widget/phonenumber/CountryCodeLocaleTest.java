package com.coinninja.coinkeeper.view.widget.phonenumber;

import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CountryCodeLocaleTest {

    @Test
    public void provides_access_to_country_country_code() {
        Locale locale = new Locale("en", "US", "");

        CountryCodeLocale countryCodeLocale = new CountryCodeLocale(locale, 1);

        assertThat(countryCodeLocale.getCountryCode(), equalTo(1));
    }

    @Test
    public void provides_access_to_country_display_name() {
        Locale locale = new Locale("en", "US", "");

        CountryCodeLocale countryCodeLocale = new CountryCodeLocale(locale, 1);

        assertThat(countryCodeLocale.getDisplayName(), equalTo("United States"));
    }

    @Test
    public void provides_emoji_for_country_flag() {
        Locale locale = new Locale("en", "US", "");

        CountryCodeLocale countryCodeLocale = new CountryCodeLocale(locale, 1);

        assertThat(countryCodeLocale.getEmoji(), equalTo("\uD83C\uDDFA\uD83C\uDDF8"));

        locale = new Locale("es", "MX", "");

        countryCodeLocale = new CountryCodeLocale(locale, 1);

        assertThat(countryCodeLocale.getEmoji(), equalTo("\uD83C\uDDF2\uD83C\uDDFD"));
    }

    @Test
    public void provides_displayable_country_code() {
        Locale locale = new Locale("en", "US", "");

        CountryCodeLocale countryCodeLocale = new CountryCodeLocale(locale, 1);

        assertThat(countryCodeLocale.getDisplayCountryCode(), equalTo("+1"));
    }
}