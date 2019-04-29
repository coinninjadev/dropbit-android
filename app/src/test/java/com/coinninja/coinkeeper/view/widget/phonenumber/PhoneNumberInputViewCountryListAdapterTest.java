package com.coinninja.coinkeeper.view.widget.phonenumber;

import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class PhoneNumberInputViewCountryListAdapterTest {
    private List<CountryCodeLocale> countryCodeLocales;
    private PhoneNumberInputViewCountryListAdapter adapter;

    @Before
    public void setUp() {
        countryCodeLocales = new ArrayList<>();
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "GB"), 44));
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "US"), 1));
        adapter = new PhoneNumberInputViewCountryListAdapter(RuntimeEnvironment.application, countryCodeLocales);
    }

    @After
    public void tearDown() {
        countryCodeLocales.clear();
        countryCodeLocales = null;
        adapter = null;
    }

    @Test
    public void adapter_binds_to_view_at_position_0() {
        View view = adapter.getView(0, null, null);

        TextView emoji = view.findViewById(R.id.phone_number_widget_country_selector_flag);
        TextView country = view.findViewById(R.id.phone_number_widget_country_selector_country_name);
        TextView countryCode = view.findViewById(R.id.phone_number_widget_country_selector_country_code);


        CountryCodeLocale countryCodeLocale = countryCodeLocales.get(0);
        assertThat(emoji, hasText(countryCodeLocale.getEmoji()));
        assertThat(country, hasText(countryCodeLocale.getDisplayName()));
        assertThat(countryCode, hasText(countryCodeLocale.getDisplayCountryCode()));
    }

    @Test
    public void adapter_binds_to_view_at_position_1() {
        View view = adapter.getView(1, null, null);

        TextView emoji = view.findViewById(R.id.phone_number_widget_country_selector_flag);
        TextView country = view.findViewById(R.id.phone_number_widget_country_selector_country_name);
        TextView countryCode = view.findViewById(R.id.phone_number_widget_country_selector_country_code);


        CountryCodeLocale countryCodeLocale = countryCodeLocales.get(1);
        assertThat(emoji, hasText(countryCodeLocale.getEmoji()));
        assertThat(country, hasText(countryCodeLocale.getDisplayName()));
        assertThat(countryCode, hasText(countryCodeLocale.getDisplayCountryCode()));
    }


}