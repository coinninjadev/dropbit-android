package com.coinninja.coinkeeper.util.currency;

import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class BTCCurrencyTest {

    @Test
    public void isValid() {
        assertTrue(new BTCCurrency(0L).isValid());
        assertTrue(new BTCCurrency(1L).isValid());
        assertTrue(new BTCCurrency(BTCCurrency.MAX_SATOSHI).isValid());
        assertFalse(new BTCCurrency(BTCCurrency.MAX_SATOSHI + 1).isValid());
        assertFalse(new BTCCurrency(-1L).isValid());
    }

    @Test
    public void update_valid() {
        BTCCurrency currency = new BTCCurrency(100L);

        String formattedValue = String.format("%s 1,99,,9.99", BTCCurrency.SYMBOL);
        assertTrue(currency.update(formattedValue));
        assertThat(currency.toLong(), equalTo(199999000000L));
    }

    @Test
    public void update_invalid() {
        long initialValue = 100L;
        BTCCurrency currency = new BTCCurrency(initialValue);

        String formattedValue = String.format("%s 1,999.999999999", BTCCurrency.SYMBOL);
        assertFalse(currency.update(formattedValue));
        assertThat(currency.toLong(), equalTo(initialValue));
    }

    @Test
    public void validate() {
        BTCCurrency currency = new BTCCurrency(100L);
        assertTrue(currency.validate("1,99,,9.99"));
        assertTrue(currency.validate("1,999.99"));
        assertTrue(currency.validate("1999.99"));
        assertFalse(currency.validate("1,999.999999999"));
        assertFalse(currency.validate("1.999.99"));
        assertFalse(currency.validate("21,000,000.01"));
    }

    @Test
    public void canConvertToUSD() {
        BTCCurrency currency = new BTCCurrency("1.00784009");
        USDCurrency conversionValue = new USDCurrency("6200");
        assertThat(currency.toUSD(conversionValue).toFormattedCurrency(), equalTo("$6,248.61"));
    }

    @Test
    public void currencyClearsFormatting() {
        BTCCurrency currency = new BTCCurrency("1,000.008");

        assertThat(currency.toFormattedString(), equalTo("1,000.008"));
    }

    @Test
    public void canConvertToBTC_noop() {
        long initialValue = 10000000000L;
        BTCCurrency currency = new BTCCurrency(initialValue);
        USDCurrency conversionValue = new USDCurrency("6200");
        assertThat(currency.toBTC(conversionValue).toSatoshis(), equalTo(initialValue));
    }

    @Test
    public void canInitFromSatoshi() {
        BTCCurrency currency = new BTCCurrency(1880L);
        assertThat(currency.toSatoshis(), equalTo(1880L));
    }

    // Validation
    @Test

    public void validatesSmaller() {
        assertTrue(new BTCCurrency(20999999.9769d).isValid());
    }

    @Test(expected = FormatNotValidException.class)
    public void validatesWhenConstructedWithString() {
        new BTCCurrency("21000000");
    }

    @Test(expected = FormatNotValidException.class)
    public void validatesWhenConstructedWithDouble() {
        new BTCCurrency(21000000d);
    }

    @Test(expected = FormatNotValidException.class)
    public void validatesWhenConstructedWithBigDecimal() {
        new BTCCurrency(new BigDecimal(21000000d));
    }

    // Formatting
    @Test
    public void can_use_alt_formatting() {
        BTCCurrency currency = new BTCCurrency(1880L);
        assertThat(currency.toFormattedCurrency(), equalTo("\u20BF 0.0000188"));

        currency.setCurrencyFormat(BTCCurrency.ALT_CURRENCY_FORMAT);

        assertThat(currency.toFormattedCurrency(), equalTo("0.00001880 BTC"));
    }

    @Test
    public void toIncrementalFormat() {
        assertThat(new BTCCurrency(1880L).toIncrementalFormat(), equalTo("\u20BF 0.0000188"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(), equalTo("\u20BF 1"));
        assertThat(new BTCCurrency("1.1").toIncrementalFormat(), equalTo("\u20BF 1.1"));
        assertThat(new BTCCurrency("1111.1").toIncrementalFormat(), equalTo("\u20BF 1,111.1"));
        assertThat(new BTCCurrency(0.111111189).toIncrementalFormat(), equalTo("\u20BF 0.11111119"));
    }

    @Test
    public void toIncrementalFormat_TrailingZeros() {
        assertThat(new BTCCurrency(1880L).toIncrementalFormat(8), equalTo("\u20BF 0.00001880"));
        assertThat(new BTCCurrency(1880L).toIncrementalFormat(9), equalTo("\u20BF 0.00001880"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(0), equalTo("\u20BF 1"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(false), equalTo("\u20BF 1"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(true), equalTo("\u20BF 1."));
        assertThat(new BTCCurrency("1").toIncrementalFormat(1), equalTo("\u20BF 1.0"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(2), equalTo("\u20BF 1.00"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(3), equalTo("\u20BF 1.000"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(4), equalTo("\u20BF 1.0000"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(5), equalTo("\u20BF 1.00000"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(6), equalTo("\u20BF 1.000000"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(7), equalTo("\u20BF 1.0000000"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(8), equalTo("\u20BF 1.00000000"));
        assertThat(new BTCCurrency("1").toIncrementalFormat(9), equalTo("\u20BF 1.00000000"));
    }

    @Test
    public void usdHasASymbol() {
        assertThat(new BTCCurrency().getSymbol(), equalTo("\u20BF"));
    }

    // Initialization
    @Test
    public void initEmpty() {
        assertThat(new BTCCurrency().toSatoshis(), equalTo(00000000L));
    }

    @Test
    public void initFromString() {
        assertThat(new BTCCurrency("100").toSatoshis(), equalTo(10000000000L));
    }

    @Test
    public void initFromDouble() {
        assertThat(new BTCCurrency(100d).toSatoshis(), equalTo(10000000000L));
    }

    @Test
    public void initFromBigDecimal() {
        assertThat(new BTCCurrency(new BigDecimal(100.000457899d)).toSatoshis(),
                equalTo(10000045790L));
    }

}