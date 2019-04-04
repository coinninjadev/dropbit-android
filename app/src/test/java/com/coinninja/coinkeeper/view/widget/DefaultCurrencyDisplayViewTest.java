package com.coinninja.coinkeeper.view.widget;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.FiatCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class DefaultCurrencyDisplayViewTest {

    private DefaultCurrencyDisplayView defaultCurrencyDisplayView;
    private TextView secondaryCurrencyView;
    private TextView primaryCurrencyView;
    private DefaultCurrencies defaultCurrencies;
    private CryptoCurrency totalCrypto = new BTCCurrency(10000L);
    private FiatCurrency fiatValue = new USDCurrency(1.00D);

    @Before
    public void setUp() {
        TestableActivity activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.adapter_item_transaction_record);
        defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        defaultCurrencyDisplayView = withId(activity, R.id.default_currency_view);
        primaryCurrencyView = withId(defaultCurrencyDisplayView, R.id.primary_currency);
        secondaryCurrencyView = withId(defaultCurrencyDisplayView, R.id.secondary_currency);
    }

    @Test
    public void renders_content_vertically() {
        assertThat(defaultCurrencyDisplayView.getOrientation(), equalTo(LinearLayout.VERTICAL));
    }

    @Test
    public void renders_views() {
        assertNotNull(primaryCurrencyView);
        assertNotNull(secondaryCurrencyView);
    }

    @Test
    public void renders_crypto_value() {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);

        assertThat(defaultCurrencyDisplayView.getSecondaryCurrencyText(), equalTo("0.0001"));
    }

    @Test
    public void renders_fiat_value() {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);

        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("- $1.00"));

        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.TRANSFER, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("- $1.00"));

        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.RECEIVE, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("+ $1.00"));
    }

    @Test
    public void updates_renders_after_default_currency_update() {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("- $1.00"));
        assertThat(defaultCurrencyDisplayView.getSecondaryCurrencyText(), equalTo("0.0001"));

        defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        defaultCurrencyDisplayView.setDefaultCurrencyPreference(defaultCurrencies);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("- 0.0001"));
        assertThat(defaultCurrencyDisplayView.getSecondaryCurrencyText(), equalTo("$1.00"));
    }

    @Test
    public void updates_renders_after_default_currency_update_when_notified_of_change() {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("- $1.00"));
        assertThat(defaultCurrencyDisplayView.getSecondaryCurrencyText(), equalTo("0.0001"));

        defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        defaultCurrencyDisplayView.onDefaultCurrencyChanged(defaultCurrencies);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("- 0.0001"));
        assertThat(defaultCurrencyDisplayView.getSecondaryCurrencyText(), equalTo("$1.00"));
    }

    @Test
    public void shows_positive_change_by_default() {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.RECEIVE, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("+ $1.00"));

        defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        defaultCurrencyDisplayView.setDefaultCurrencyPreference(defaultCurrencies);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("+ 0.0001"));
    }

    @Test
    public void can_turn_off_rendering_of_positive_change() {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.RECEIVE, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("+ $1.00"));

        defaultCurrencyDisplayView.setShowPositiveChange(false);

        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("$1.00"));
    }
}
