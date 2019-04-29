package com.coinninja.coinkeeper.model;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.robolectric.annotation.Config;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class PaymentHolderTest {


    DefaultCurrencies defaultCurrencies;
    private PaymentHolder holder;

    @Before
    public void setUp() {
        holder = new PaymentHolder(new USDCurrency(5000d));
        defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        holder.setDefaultCurrencies(defaultCurrencies);
        holder.updateValue(new USDCurrency(5000d));
    }

    @Test
    public void copies_address_from_current_to_new_transaction_data_when_set() {
        TransactionData transactionData = new TransactionData(new UnspentTransactionOutput[0], 0, 0,
                0, mock(DerivationPath.class), "");

        String paymentAddress = "--address--";
        holder.setPaymentAddress(paymentAddress);
        holder.setTransactionData(transactionData);

        assertThat(holder.getPaymentAddress(), equalTo(paymentAddress));
    }

    @Test
    public void copies_address_from_current_to_new_transaction_data_when_set__only_overrides_when_currently_has_address() {
        String paymentAddress = "--address--";
        TransactionData transactionData = new TransactionData(new UnspentTransactionOutput[0], 0, 0,
                0, mock(DerivationPath.class), paymentAddress);

        holder.setPaymentAddress("");
        holder.setTransactionData(transactionData);

        assertThat(holder.getPaymentAddress(), equalTo(paymentAddress));
    }

    @Test
    public void sets_address_on_transaction_data_when_available() {
        String paymentAddress = "--address--";
        holder.setPaymentAddress(paymentAddress);

        assertThat(holder.getPaymentAddress(), equalTo(paymentAddress));
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
        holder.setTransactionData(new TransactionData(new UnspentTransactionOutput[3], 10000000L, 1000L, 45000L, mock(DerivationPath.class), ""));
        holder.setPublicKey("-pub-key-");
        holder.setPaymentAddress("-address-");
        holder.updateValue(new USDCurrency(1.d));

        holder.clearPayment();

        assertThat(holder.getPaymentAddress(), equalTo(""));
        assertThat(holder.getPublicKey(), equalTo(""));
        assertThat(holder.getTransactionData().getUtxos().length, equalTo(0));
        assertThat(holder.getTransactionData().getAmount(), equalTo(0L));
        assertThat(holder.getTransactionData().getFeeAmount(), equalTo(0L));
        assertThat(holder.getTransactionData().getChangeAmount(), equalTo(0L));
        assertThat(holder.getPrimaryCurrency().toLong(), equalTo(0L));
    }

    @Test
    public void sets_max_limit_for_fiat() {
        holder.setEvaluationCurrency(new USDCurrency(1D));

        holder.setMaxLimitForFiat();

        assertThat(((USDCurrency) holder.getEvaluationCurrency()).getMaxLongValue(), equalTo(2099999998L));
    }
}