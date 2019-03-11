package com.coinninja.coinkeeper.view.widget.phonenumber;

import com.coinninja.coinkeeper.util.java.LocaleUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

public class CountryCodeLocaleGenerator {
    private final LocaleUtil localeUtil;
    private final PhoneNumberUtil phoneNumberUtil;

    @Inject
    CountryCodeLocaleGenerator(LocaleUtil localeUtil, PhoneNumberUtil phoneNumberUtil) {
        this.localeUtil = localeUtil;
        this.phoneNumberUtil = phoneNumberUtil;
    }

    public List<CountryCodeLocale> generate() {
        Map<Integer, CountryCodeLocale> locales = new HashMap<>();

        int cc;
        CountryCodeLocale countryCodeLocale;
        for (Locale locale : localeUtil.getAvailableLocales()) {
            cc = phoneNumberUtil.getCountryCodeForRegion(locale.getCountry());
            if (locale.getCountry().isEmpty() || cc == 0) {
                continue;
            }

            countryCodeLocale = new CountryCodeLocale(locale, cc);
            if (!locales.containsKey(countryCodeLocale.hashCode())) {
                locales.put(countryCodeLocale.hashCode(), countryCodeLocale);
            }
        }

        List<CountryCodeLocale> values = new ArrayList<>(locales.values());
        Collections.sort(values);

        return values;
    }
}
