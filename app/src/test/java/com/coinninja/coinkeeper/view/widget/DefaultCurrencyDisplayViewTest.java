package com.coinninja.coinkeeper.view.widget;

import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import app.dropbit.commons.currency.BTCCurrency;
import app.dropbit.commons.currency.CryptoCurrency;
import app.dropbit.commons.currency.FiatCurrency;
import app.dropbit.commons.currency.USDCurrency;

import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
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
        defaultCurrencyDisplayView = activity.findViewById(R.id.default_currency_view);
        defaultCurrencyDisplayView.useCryptoSymbol(false);
        primaryCurrencyView = defaultCurrencyDisplayView.findViewById(R.id.primary_currency);
        secondaryCurrencyView = defaultCurrencyDisplayView.findViewById(R.id.secondary_currency);
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
    public void renders_values_with_out_direction() {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("$1.00"));
        assertThat(defaultCurrencyDisplayView.getSecondaryCurrencyText(), equalTo("0.0001"));
    }

    @Test
    public void renders_fiat_value() {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);

        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("$1.00"));

        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.TRANSFER, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("$1.00"));

        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.RECEIVE, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("$1.00"));
    }

    @Test
    public void updates_renders_after_default_currency_update() {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("$1.00"));
        assertThat(defaultCurrencyDisplayView.getSecondaryCurrencyText(), equalTo("0.0001"));

        defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        defaultCurrencyDisplayView.setDefaultCurrencyPreference(defaultCurrencies);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("0.0001"));
        assertThat(defaultCurrencyDisplayView.getSecondaryCurrencyText(), equalTo("$1.00"));
    }

    @Test
    public void updates_renders_after_default_currency_update_when_notified_of_change() {
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("$1.00"));
        assertThat(defaultCurrencyDisplayView.getSecondaryCurrencyText(), equalTo("0.0001"));

        defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        defaultCurrencyDisplayView.onDefaultCurrencyChanged(defaultCurrencies);
        assertThat(defaultCurrencyDisplayView.getPrimaryCurrencyText(), equalTo("0.0001"));
        assertThat(defaultCurrencyDisplayView.getSecondaryCurrencyText(), equalTo("$1.00"));
    }

    @Test
    public void renders_crypto_icon_primary() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        defaultCurrencyDisplayView.useCryptoIcon(true);
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);

        Drawable[] primaryCompoundDrawables = primaryCurrencyView.getCompoundDrawables();
        MatcherAssert.assertThat(shadowOf(primaryCompoundDrawables[0]).getCreatedFromResId(), equalTo(R.drawable.ic_btc_icon));
        assertNull(primaryCompoundDrawables[1]);
        assertNull(primaryCompoundDrawables[2]);
        assertNull(primaryCompoundDrawables[3]);

        Drawable[] altCompoundDrawables = secondaryCurrencyView.getCompoundDrawables();
        assertNull(altCompoundDrawables[0]);
        assertNull(altCompoundDrawables[1]);
        assertNull(altCompoundDrawables[2]);
        assertNull(altCompoundDrawables[3]);
    }

    @Test
    public void renders_crypto_icon_secondary() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        defaultCurrencyDisplayView.useCryptoIcon(true);
        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);

        Drawable[] primaryCompoundDrawables = primaryCurrencyView.getCompoundDrawables();
        assertNull(primaryCompoundDrawables[0]);
        assertNull(primaryCompoundDrawables[1]);
        assertNull(primaryCompoundDrawables[2]);
        assertNull(primaryCompoundDrawables[3]);

        Drawable[] altCompoundDrawables = secondaryCurrencyView.getCompoundDrawables();
        MatcherAssert.assertThat(shadowOf(altCompoundDrawables[0]).getCreatedFromResId(), equalTo(R.drawable.ic_btc_icon));
        assertNull(altCompoundDrawables[1]);
        assertNull(altCompoundDrawables[2]);
        assertNull(altCompoundDrawables[3]);
    }

    @Test
    public void renders_crypto_symbol_primary() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        defaultCurrencyDisplayView.useCryptoSymbol(true);
        defaultCurrencyDisplayView.useCryptoIcon(false);

        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);

        assertThat(primaryCurrencyView, hasText("\u20BF 0.0001"));
    }

    @Test
    public void renders_crypto_symbol_secondary() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        defaultCurrencyDisplayView.useCryptoSymbol(true);
        defaultCurrencyDisplayView.useCryptoIcon(false);

        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);

        assertThat(secondaryCurrencyView, hasText("\u20BF 0.0001"));
    }

    @Test
    public void renders_crypto_in_sats__mode_LIGHTNING() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        defaultCurrencyDisplayView.useCryptoSymbol(true);
        defaultCurrencyDisplayView.useCryptoIcon(false);
        defaultCurrencyDisplayView.accountMode(AccountMode.LIGHTNING);

        defaultCurrencyDisplayView.renderValues(defaultCurrencies, BindableTransaction.SendState.SEND, totalCrypto, fiatValue);

        assertThat(secondaryCurrencyView, hasText("10,000 sats"));
    }
}
