package com.coinninja.coinkeeper.model;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.robolectric.annotation.Config;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class PaymentHolderTest {

    private PaymentHolder holder;

    @Mock
    DefaultCurrencies defaultCurrencies;

    @Mock
    private CurrencyPreference currencyPreference;

    @Before
    public void setUp() {
        holder = new PaymentHolder(new USDCurrency(5000d), new TransactionFee(10L, 15L, 20L));
        holder.setDefaultCurrencies(currencyPreference.getCurrenciesPreference());

        when(defaultCurrencies.getPrimaryCurrency()).thenReturn(new USDCurrency());
        when(defaultCurrencies.getSecondaryCurrency()).thenReturn(new BTCCurrency());
        when(defaultCurrencies.getFiat()).thenReturn(new USDCurrency());
        when(defaultCurrencies.getCrypto()).thenReturn(new BTCCurrency(1.d));
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        holder.setDefaultCurrencies(defaultCurrencies);
    }

    @Test
    public void provides_crypto_currency_when_primary() {
        assertThat(holder.getCryptoCurrency().toLong(), equalTo(100000000L));
    }

    @Test
    public void provides_secondary_currency__BTC() {
        USDCurrency usd = new USDCurrency(10.0d);

        holder.updateValue(usd);

        assertThat(holder.getSecondaryCurrency().toLong(), equalTo(200000L));
    }

    @Test
    public void provides_secondary_currency__FIAT() {
        holder.toggleCurrencies();
        BTCCurrency btcCurrency = new BTCCurrency(1.0d);

        holder.updateValue(btcCurrency);

        assertThat(holder.getSecondaryCurrency().toFormattedCurrency(), equalTo("$5,000.00"));
    }

    @Test
    public void provides_primary_currency__FIAT() {
        USDCurrency usd = new USDCurrency(10.0d);

        holder.updateValue(usd);

        assertThat(holder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo(usd.toFormattedCurrency()));
    }

    @Test
    public void provides_primary_currency__CRYPTO() {
        holder.toggleCurrencies();
        BTCCurrency btcCurrency = new BTCCurrency(1.0d);

        holder.updateValue((btcCurrency));

        assertThat(holder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo(btcCurrency.toFormattedCurrency()));
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
        holder.toggleCurrencies();
        Currency usd = holder.updateValue(new BTCCurrency(1.0d));

        assertThat(usd.toFormattedCurrency(), equalTo("$5,000.00"));
    }

    @Test
    public void given_dollars_to_spend_it_converts_fiat() {
        holder.updateValue(new USDCurrency(25d));
        holder.toggleCurrencies();
        assertThat(holder.getBtcCurrency().toSatoshis(), equalTo(500000L));
    }

    @Test
    public void toggle_currency_when_provided_with_secondary() {
        holder.updateValue(new USDCurrency(25d));
        holder.updateValue(new BTCCurrency(1L));
        assertThat(holder.getPrimaryCurrency().toLong(), equalTo(1L));
        assertTrue(holder.getPrimaryCurrency() instanceof BTCCurrency);
    }

    @Test
    public void clear_payment_clears_pub_key_and_address() {
        holder.updateValue(new USDCurrency(25d));
        holder.setPublicKey("-pub-key-");
        holder.setPaymentAddress("-address-");

        holder.clearPayment();

        assertThat(holder.getPaymentAddress(), equalTo(""));
        assertThat(holder.getPublicKey(), equalTo(""));
    }
}