package com.coinninja.coinkeeper.model;

import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PaymentHolderTest {

    private PaymentHolder holder;

    @Before
    public void setUp() {
        holder = new PaymentHolder(new USDCurrency(5000d), new TransactionFee(10L, 15L, 20L));
    }

    @Test
    public void provides_crypto_currency_when_primary() {
        holder.loadPaymentFrom(new BTCCurrency(1d));

        assertThat(holder.getCryptoCurrency().toLong(), equalTo(100000000L));
    }

    @Test
    public void provides_secondary_currency__BTC() {
        USDCurrency usd = new USDCurrency(10.0d);

        holder.loadPaymentFrom(usd);

        assertThat(holder.getSecondaryCurrency().toLong(), equalTo(200000L));
    }

    @Test
    public void provides_secondary_currency__FIAT() {
        BTCCurrency btcCurrency = new BTCCurrency(1.0d);

        holder.loadPaymentFrom(btcCurrency);

        assertThat(holder.getSecondaryCurrency().toFormattedCurrency(), equalTo("$5,000.00"));
    }

    @Test
    public void provides_primary_currency__FIAT() {
        USDCurrency usd = new USDCurrency(10.0d);

        holder.loadPaymentFrom(usd);

        assertThat(holder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo(usd.toFormattedCurrency()));
    }

    @Test
    public void provides_primary_currency__CRYPTO() {
        BTCCurrency btcCurrency = new BTCCurrency(1.0d);

        holder.loadPaymentFrom(btcCurrency);

        assertThat(holder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo(btcCurrency.toFormattedCurrency()));
    }

    @Test
    public void setting_btc_currency_defines_mode__CRYPTO() {
        holder.loadPaymentFrom(new BTCCurrency(1.0d));

        assertThat(holder.getPaymentMode(), equalTo(PaymentHolder.PaymentMode.CRYPTO));
    }

    @Test
    public void setting_usd_currency_defines_mode__FIAT() {
        holder.loadPaymentFrom(new USDCurrency(5.00d));

        assertThat(holder.getPaymentMode(), equalTo(PaymentHolder.PaymentMode.FIAT));
    }

    @Test
    public void spendable_funds_intialize_as_none() {
        assertThat(holder.getSpendableBalance().toSatoshis(), equalTo(0L));
    }

    @Test
    public void holds_available_spendable_balance() {
        holder.setSpendableBalance(new BTCCurrency(1000L));

        assertThat(holder.getSpendableBalance().toSatoshis(), equalTo(1000L));
    }


    @Test
    public void given_btc_to_spend_converts_to_fiat() {
        Currency usd = holder.loadPaymentFrom(new BTCCurrency(1.0d));

        assertThat(usd.toFormattedCurrency(), equalTo("$5,000.00"));
    }

    @Test
    public void given_dollars_to_spend_it_converts_fiat() {
        holder.loadPaymentFrom(new USDCurrency(25d));

        assertThat(holder.getBtcCurrency().toSatoshis(), equalTo(500000L));
    }

    @Test
    public void clear_payment_clears_pub_key_and_address() {
        holder.loadPaymentFrom(new USDCurrency(25d));
        holder.setPublicKey("-pub-key-");
        holder.setPaymentAddress("-address-");

        holder.clearPayment();

        assertThat(holder.getPaymentAddress(), equalTo(""));
        assertThat(holder.getPublicKey(), equalTo(""));
    }
}