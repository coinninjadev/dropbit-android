package com.coinninja.coinkeeper.util.currency;

import org.junit.Test;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;

import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class USDCurrencyTest {

    @Test
    public void sets_max_value_statically() {
        USDCurrency.SET_MAX_LIMIT(new USDCurrency(0));
        assertThat(USDCurrency.MAX_DOLLAR_AMOUNT, equalTo(Long.MAX_VALUE));

        USDCurrency.SET_MAX_LIMIT(new USDCurrency(10000));
        assertThat(USDCurrency.MAX_DOLLAR_AMOUNT, equalTo(209999999769L));
    }

    @Test
    public void instantiated_from_long() {
        assertThat(new USDCurrency(100L).toFormattedCurrency(), equalTo("$1.00"));
    }

    @Test
    public void update_valid() {
        USDCurrency currency = new USDCurrency(100L);

        String formattedValue = "$1,99$,,9.99";
        assertTrue(currency.update(formattedValue));
        assertThat(currency.toLong(), equalTo(199999L));
    }

    @Test
    public void update_invalid() {
        long initialValue = 100L;
        USDCurrency currency = new USDCurrency(initialValue);

        String formattedValue = "$1,999.999";
        assertFalse(currency.update(formattedValue));
        assertThat(currency.toLong(), equalTo(initialValue));
    }

    @Test
    public void validate() {
        USDCurrency currency = new USDCurrency(100L);
        assertTrue(currency.validate("$1,99$,,9.99"));
        assertTrue(currency.validate("$1,999.99"));
        assertTrue(currency.validate("1999.99"));
        assertTrue(currency.validate("$1,999,999,999,999.99"));
        assertFalse("overflow", currency.validate("$1,999,999,999,999,999,999,999,999,999,999.99"));
        assertFalse(currency.validate("$1,999.999"));
        assertFalse(currency.validate("$1.999.99"));
    }

    @Test
    public void instantiated_from_long_zero() {
        USDCurrency usdCurrency = new USDCurrency(0L);
        assertThat(usdCurrency.toFormattedCurrency(), equalTo("$0.00"));
        assertTrue(usdCurrency.isZero());
    }


    @Test
    public void converts_to_long() {
        assertThat(new USDCurrency(100L).toLong(), equalTo(100L));
    }

    // Conversions
    @Test
    public void canConvertToUSD() {
        USDCurrency currency = new USDCurrency("100");
        USDCurrency conversionValue = new USDCurrency("6200");
        assertThat(currency.toUSD(conversionValue).toFormattedCurrency(), equalTo("$100.00"));
    }

    @Test
    public void canConvertToBTC() {
        USDCurrency usdCurrency = new USDCurrency("100");
        USDCurrency currency = new USDCurrency("6200");
        assertThat(usdCurrency.toBTC(currency).toSatoshis(), equalTo(1612903L));
    }

    @Test
    public void canConvertToAWholeBTC() {

        USDCurrency usdCurrency = new USDCurrency("6993.17");
        USDCurrency eval = new USDCurrency("6993.17");

        assertThat(usdCurrency.toBTC(eval).toSatoshis(), equalTo(100000000L));

        usdCurrency = new USDCurrency("7040.22");
        eval = new USDCurrency("7040.22");

        assertThat(usdCurrency.toBTC(eval).toSatoshis(), equalTo(100000000L));
    }

    // Formatting
    @Test
    public void hasAFormat() {
        assertThat(new USDCurrency().getFormat(), equalTo("#,###.##"));
    }

    @Test
    public void hasCurrencyFormat() {
        assertThat(new USDCurrency().getCurrencyFormat(), equalTo("$#,##0.00"));
    }

    @Test
    public void usdHasASymbol() {
        assertThat(new USDCurrency().getSymbol(), equalTo("$"));
    }

    // Initialization
    @Test
    public void initEmpty() {
        assertThat(new USDCurrency().toFormattedCurrency(), equalTo("$0.00"));
    }

    @Test
    public void initFromString() {
        assertThat(new USDCurrency("100").toFormattedCurrency(), equalTo("$100.00"));
    }

    @Test
    public void initFromDouble() {
        assertThat(new USDCurrency(100d).toFormattedCurrency(), equalTo("$100.00"));
    }

    @Test
    public void initFromBigDecimal() {
        assertThat(new USDCurrency(new BigDecimal(100d)).toFormattedCurrency(), equalTo("$100.00"));
    }
}