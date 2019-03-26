package com.coinninja.coinkeeper.ui.payment;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isGone;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class PaymentInputViewTest {

    private PaymentInputView paymentInputView;
    private EditText primaryCurrency;
    private TextView secondaryCurrency;
    private PaymentHolder paymentHolder = new PaymentHolder();

    @Before
    public void setUp() {
        TestableActivity activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.fragment_pay_dialog);
        paymentInputView = withId(activity, R.id.payment_input_view);
        primaryCurrency = withId(activity, R.id.primary_currency);
        secondaryCurrency = withId(activity, R.id.secondary_currency);
        paymentHolder.setEvaluationCurrency(new USDCurrency(1000.00D));
        paymentHolder.setDefaultCurrencies(new DefaultCurrencies(new USDCurrency(), new BTCCurrency()));
    }

    @After
    public void tearDown() {
        paymentInputView = null;
        primaryCurrency = null;
        secondaryCurrency = null;
        paymentHolder = null;
    }

    @Test
    public void inflates_view() {
        assertNotNull(withId(paymentInputView, R.id.primary_currency));
        assertNotNull(withId(paymentInputView, R.id.secondary_currency));
    }

    @Test
    public void secondary_currency_is_hidden_by_default() {
        assertThat(secondaryCurrency, isGone());
    }

    @Test
    public void adds_currency_type_as_you_go_watcher_on_primary_currency() {
        primaryCurrency.setText("1.15");

        assertThat(primaryCurrency, hasText("$1.15"));
    }

    @Test
    public void provided_an_non_zero_conversion_value_secondary_currency_is_visible() {
        paymentInputView.setPaymentHolder(paymentHolder);
        assertThat(secondaryCurrency, isVisible());
        assertThat(secondaryCurrency, hasText("0"));
    }

    @Test
    public void renders_both_currencies_when_payment_holder_provided() {
        paymentHolder.updateValue(new USDCurrency(1.15D));

        paymentInputView.setPaymentHolder(paymentHolder);

        assertThat(primaryCurrency, hasText("$1.15"));
        assertThat(secondaryCurrency, hasText("0.00115"));
    }

    @Test
    public void updating_primary_currency_converts_to_secondary() {
        paymentInputView.setPaymentHolder(paymentHolder);

        primaryCurrency.setText("1.15");

        assertThat(secondaryCurrency, hasText("0.00115"));
    }

    @Test
    public void requests_focus_when_primary_value_is_zero() {
        paymentInputView.setPaymentHolder(paymentHolder);

        paymentInputView.requestFocus();

        assertTrue(primaryCurrency.hasFocus());
    }

    @Test
    public void requests_focus_when_initialized_with_zero_value() {
        paymentHolder.updateValue(new USDCurrency(10));
        paymentInputView.setPaymentHolder(paymentHolder);

        paymentInputView.requestFocus();

        assertFalse(primaryCurrency.hasFocus());
    }

    @Test
    public void sets_updates_currency_on_watcher_when_payment_holder_set() {
        paymentHolder.updateValue(new BTCCurrency(1D));
        paymentInputView.setPaymentHolder(paymentHolder);
        assertThat(primaryCurrency, hasText("1"));

        paymentHolder.updateValue(new USDCurrency(1D));
        paymentInputView.setPaymentHolder(paymentHolder);
        assertThat(primaryCurrency, hasText("$1.00"));
    }

    @Test
    public void shows_price_when_BTC_primary() {
        BTCCurrency btc = new BTCCurrency(1.0D);
        paymentHolder.updateValue(btc);

        paymentInputView.setPaymentHolder(paymentHolder);

        assertThat(primaryCurrency, hasText("1"));
        assertThat(secondaryCurrency, hasText("$1,000.00"));
    }

    @Test
    public void shows_price_when_USD_primary() {
        USDCurrency usd = new USDCurrency(5000.00D);
        paymentHolder.updateValue(usd);

        paymentInputView.setPaymentHolder(paymentHolder);

        assertThat(primaryCurrency, hasText("$5,000.00"));
        assertThat(secondaryCurrency, hasText("5"));
    }

    @Test
    public void can_toggle_primary_currency() {
        USDCurrency usd = new USDCurrency(5000.00D);
        paymentHolder.updateValue(usd);
        paymentInputView.setPaymentHolder(paymentHolder);

        View toggleView = withId(paymentInputView, R.id.primary_currency_toggle);
        clickOn(toggleView);

        assertThat(primaryCurrency, hasText("5"));
        assertThat(toggleView, isVisible());
    }

    @Test
    public void toggling_zero_zeros_input() {
        paymentHolder.updateValue(new USDCurrency());
        paymentInputView.setPaymentHolder(paymentHolder);
        clickOn(paymentInputView, R.id.primary_currency_toggle);
        assertThat(primaryCurrency, hasText("0"));

        paymentHolder.updateValue(new BTCCurrency());
        paymentInputView.setPaymentHolder(paymentHolder);
        clickOn(paymentInputView, R.id.primary_currency_toggle);
        assertThat(primaryCurrency, hasText("$0"));
    }

    @Test
    public void no_toggle_button_when_evaluation_currency_is_null() {
        paymentHolder.setEvaluationCurrency(null);

        paymentInputView.setPaymentHolder(paymentHolder);

        assertThat(withId(paymentInputView, R.id.primary_currency_toggle), isGone());
    }

    @Test
    public void shows_btc_icon_when_primary() {
        paymentHolder.updateValue(new BTCCurrency(5000.00D));
        paymentInputView.setPaymentHolder(paymentHolder);

        Drawable[] primaryCompoundDrawables = primaryCurrency.getCompoundDrawables();
        MatcherAssert.assertThat(shadowOf(primaryCompoundDrawables[0]).getCreatedFromResId(), equalTo(R.drawable.ic_btc_icon));
        assertNull(primaryCompoundDrawables[1]);
        assertNull(primaryCompoundDrawables[2]);
        assertNull(primaryCompoundDrawables[3]);

        Drawable[] altCompoundDrawables = secondaryCurrency.getCompoundDrawables();
        assertNull(altCompoundDrawables[0]);
        assertNull(altCompoundDrawables[1]);
        assertNull(altCompoundDrawables[2]);
        assertNull(altCompoundDrawables[3]);
    }

    @Test
    public void shows_btc_icon_when_secondary() {
        paymentHolder.updateValue(new USDCurrency(5000.00D));
        paymentInputView.setPaymentHolder(paymentHolder);

        Drawable[] primaryCompoundDrawables = primaryCurrency.getCompoundDrawables();
        assertNull(primaryCompoundDrawables[0]);
        assertNull(primaryCompoundDrawables[1]);
        assertNull(primaryCompoundDrawables[2]);
        assertNull(primaryCompoundDrawables[3]);

        Drawable[] altCompoundDrawables = secondaryCurrency.getCompoundDrawables();
        MatcherAssert.assertThat(shadowOf(altCompoundDrawables[0]).getCreatedFromResId(), equalTo(R.drawable.ic_btc_icon));
        assertNull(altCompoundDrawables[1]);
        assertNull(altCompoundDrawables[2]);
        assertNull(altCompoundDrawables[3]);
    }

    @Test
    public void does_not_show_btc_icon_when_secondary_and_no_evaluation_currency() {
        paymentHolder.updateValue(new USDCurrency(5000.00D));
        paymentHolder.setEvaluationCurrency(null);
        paymentInputView.setPaymentHolder(paymentHolder);

        Drawable[] primaryCompoundDrawables = primaryCurrency.getCompoundDrawables();
        assertNull(primaryCompoundDrawables[0]);
        assertNull(primaryCompoundDrawables[1]);
        assertNull(primaryCompoundDrawables[2]);
        assertNull(primaryCompoundDrawables[3]);

        Drawable[] altCompoundDrawables = secondaryCurrency.getCompoundDrawables();
        assertNull(altCompoundDrawables[0]);
        assertNull(altCompoundDrawables[1]);
        assertNull(altCompoundDrawables[2]);
        assertNull(altCompoundDrawables[3]);
    }
}