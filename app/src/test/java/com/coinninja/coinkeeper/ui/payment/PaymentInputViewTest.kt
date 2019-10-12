package com.coinninja.coinkeeper.ui.payment

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.android.helpers.Views.clickOn
import com.coinninja.android.helpers.Views.withId
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.matchers.TextViewMatcher.hasText
import com.coinninja.matchers.ViewMatcher.isGone
import com.coinninja.matchers.ViewMatcher.isVisible
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class PaymentInputViewTest {

    private val activity = Robolectric.setupActivity(TestableActivity::class.java).also {
        it.appendLayout(R.layout.fragment_pay_dialog)
    }

    private val paymentInputView: PaymentInputView get() = activity.findViewById(R.id.payment_input_view)
    private val paymentHolder: PaymentHolder = PaymentHolder().also {
        it.evaluationCurrency = USDCurrency(1000.00)
        it.defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
    }

    @Test
    fun inflates_view() {
        assertNotNull(withId(paymentInputView, R.id.primary_currency))
        assertNotNull(withId(paymentInputView, R.id.secondary_currency))
    }

    @Test
    fun secondary_currency_is_hidden_by_default() {
        assertThat<TextView>(paymentInputView.secondaryCurrency, isGone())
    }

    @Test
    fun adds_currency_type_as_you_go_watcher_on_primary_currency() {
        paymentInputView.paymentHolder = paymentHolder

        paymentInputView.primaryCurrency.setText("1.15")

        assertThat<EditText>(paymentInputView.primaryCurrency, hasText("$1.15"))
    }

    @Test
    fun provided_an_non_zero_conversion_value_secondary_currency_is_visible() {
        paymentInputView.paymentHolder = paymentHolder
        assertThat<TextView>(paymentInputView.secondaryCurrency, isVisible())
        assertThat<TextView>(paymentInputView.secondaryCurrency, hasText("0"))
    }

    @Test
    fun renders_both_currencies_when_payment_holder_provided() {
        paymentHolder.updateValue(USDCurrency(1.15))

        paymentInputView.paymentHolder = paymentHolder

        assertThat<EditText>(paymentInputView.primaryCurrency, hasText("$1.15"))
        assertThat<TextView>(paymentInputView.secondaryCurrency, hasText("0.00115"))
    }

    @Test
    fun updating_primary_currency_converts_to_secondary() {
        paymentInputView.paymentHolder = paymentHolder

        paymentInputView.primaryCurrency.setText("1.15")

        assertThat<TextView>(paymentInputView.secondaryCurrency, hasText("0.00115"))
    }

    @Test
    fun updating_primary_currency_notifies_observer_of_valid_entry() {
        paymentInputView.paymentHolder = paymentHolder
        val onValidEntryObserver = mock<PaymentInputView.OnValidEntryObserver>()
        paymentInputView.onValidEntryObserver = onValidEntryObserver

        paymentInputView.primaryCurrency.setText("1.15")

        verify(onValidEntryObserver).onValidEntry()
    }

    @Test
    fun updating_primary_currency_converts_to_secondary__LIGHTNING_MODE() {
        paymentInputView.paymentHolder = paymentHolder
        paymentInputView.accountMode = AccountMode.LIGHTNING

        paymentInputView.primaryCurrency.setText("1.15")

        assertThat<TextView>(paymentInputView.secondaryCurrency, hasText("115,000 sats"))
    }

    @Test
    fun requests_focus_when_initialized_with_zero_value() {
        paymentHolder.updateValue(USDCurrency(10))
        paymentInputView.paymentHolder = paymentHolder

        assertFalse(paymentInputView.primaryCurrency.hasFocus())
    }

    @Test
    fun sets_updates_currency_on_watcher_when_payment_holder_set() {
        paymentHolder.updateValue(BTCCurrency(1.0))
        paymentInputView.paymentHolder = paymentHolder
        assertThat<EditText>(paymentInputView.primaryCurrency, hasText("1"))

        paymentHolder.updateValue(USDCurrency(1.0))
        paymentInputView.paymentHolder = paymentHolder
        assertThat<EditText>(paymentInputView.primaryCurrency, hasText("$1.00"))
    }

    @Test
    fun shows_price_when_BTC_primary() {
        val btc = BTCCurrency(1.0)
        paymentHolder.updateValue(btc)

        paymentInputView.paymentHolder = paymentHolder

        assertThat<EditText>(paymentInputView.primaryCurrency, hasText("1"))
        assertThat<TextView>(paymentInputView.secondaryCurrency, hasText("$1,000.00"))
    }

    @Test
    fun shows_price_when_USD_primary() {
        val usd = USDCurrency(5000.00)
        paymentHolder.updateValue(usd)

        paymentInputView.paymentHolder = paymentHolder

        assertThat<EditText>(paymentInputView.primaryCurrency, hasText("$5,000.00"))
        assertThat<TextView>(paymentInputView.secondaryCurrency, hasText("5"))
    }

    @Test
    fun can_toggle_primary_currency() {
        val usd = USDCurrency(5000.00)
        paymentHolder.updateValue(usd)
        paymentInputView.paymentHolder = paymentHolder

        val toggleView = withId<View>(paymentInputView, R.id.primary_currency_toggle)
        clickOn(toggleView)

        assertThat<EditText>(paymentInputView.primaryCurrency, hasText("5"))
        assertThat(toggleView, isVisible())
    }

    @Test
    fun toggling_zero_zeros_input() {
        paymentHolder.updateValue(USDCurrency())
        paymentInputView.paymentHolder = paymentHolder
        clickOn(paymentInputView, R.id.primary_currency_toggle)
        assertThat<EditText>(paymentInputView.primaryCurrency, hasText("0"))

        paymentHolder.updateValue(BTCCurrency())
        paymentInputView.paymentHolder = paymentHolder
        clickOn(paymentInputView, R.id.primary_currency_toggle)
        assertThat<EditText>(paymentInputView.primaryCurrency, hasText("$0"))
    }

    @Test
    fun shows_btc_icon_when_primary() {
        paymentHolder.updateValue(BTCCurrency(5000.00))
        paymentInputView.paymentHolder = paymentHolder

        val primaryCompoundDrawables = paymentInputView.primaryCurrency.compoundDrawables
        assertThat(shadowOf(primaryCompoundDrawables[0]).createdFromResId, equalTo(R.drawable.ic_btc_icon))
        assertNull(primaryCompoundDrawables[1])
        assertNull(primaryCompoundDrawables[2])
        assertNull(primaryCompoundDrawables[3])

        val altCompoundDrawables = paymentInputView.secondaryCurrency.compoundDrawables
        assertNull(altCompoundDrawables[0])
        assertNull(altCompoundDrawables[1])
        assertNull(altCompoundDrawables[2])
        assertNull(altCompoundDrawables[3])
    }

    @Test
    fun shows_btc_icon_when_secondary() {
        paymentHolder.updateValue(USDCurrency(5000.00))
        paymentInputView.paymentHolder = paymentHolder

        val primaryCompoundDrawables = paymentInputView.primaryCurrency.compoundDrawables
        assertNull(primaryCompoundDrawables[0])
        assertNull(primaryCompoundDrawables[1])
        assertNull(primaryCompoundDrawables[2])
        assertNull(primaryCompoundDrawables[3])

        val altCompoundDrawables = paymentInputView.secondaryCurrency.compoundDrawables
        assertThat(shadowOf(altCompoundDrawables[0]).createdFromResId, equalTo(R.drawable.ic_btc_icon))
        assertNull(altCompoundDrawables[1])
        assertNull(altCompoundDrawables[2])
        assertNull(altCompoundDrawables[3])
    }

    @Test
    fun does_not_show_btc_icon_when_secondary_and_lightning_mode() {
        paymentHolder.updateValue(USDCurrency(5000.00))
        paymentInputView.accountMode =AccountMode.LIGHTNING
        paymentInputView.paymentHolder = paymentHolder

        val primaryCompoundDrawables = paymentInputView.primaryCurrency.compoundDrawables
        assertNull(primaryCompoundDrawables[0])
        assertNull(primaryCompoundDrawables[1])
        assertNull(primaryCompoundDrawables[2])
        assertNull(primaryCompoundDrawables[3])

        val altCompoundDrawables = paymentInputView.secondaryCurrency.compoundDrawables
        assertNull(altCompoundDrawables[0])
        assertNull(altCompoundDrawables[1])
        assertNull(altCompoundDrawables[2])
        assertNull(altCompoundDrawables[3])
    }

    @Test
    fun adding_value_hides_send_max_button() {
        paymentHolder.updateValue(USDCurrency(5000.00))
        paymentInputView.paymentHolder = paymentHolder
        paymentInputView.primaryCurrency.setText("1")

        assertThat<Button>(paymentInputView.sendMax, isGone())
    }

    @Test
    fun removing_all_values_reveals_send_max_button() {
        paymentHolder.updateValue(USDCurrency(5000.00))
        paymentInputView.paymentHolder = paymentHolder
        val primaryCurrency = paymentInputView.primaryCurrency
        val sendMax = paymentInputView.sendMax

        primaryCurrency.setText("1")
        assertThat(sendMax, isGone())

        primaryCurrency.setText("$0")
        assertThat(sendMax, isVisible())
    }

    @Test
    fun removing_all_values_reveals_send_max_button__zero_with_dot() {
        paymentHolder.updateValue(USDCurrency(5000.00))
        paymentInputView.paymentHolder = paymentHolder
        val primaryCurrency = paymentInputView.primaryCurrency
        val sendMax = paymentInputView.sendMax

        primaryCurrency.setText("$0.")
        assertThat(sendMax, isGone())
    }

    @Test
    fun sending_max_event_can_be_observed() {
        val sendMaxObserver = mock(PaymentInputView.OnSendMaxObserver::class.java)
        paymentHolder.updateValue(USDCurrency(0.0))
        paymentInputView.paymentHolder = paymentHolder
        clickOn(paymentInputView.sendMax)
        paymentInputView.setOnSendMaxObserver(sendMaxObserver)

        clickOn(paymentInputView.sendMax)

        verify<PaymentInputView.OnSendMaxObserver>(sendMaxObserver).onSendMax()
    }

    @Test
    fun updating_payment_holder_with_value_hides_send_max_button() {
        val sendMaxObserver = mock(PaymentInputView.OnSendMaxObserver::class.java)
        paymentHolder.updateValue(USDCurrency(100.0))
        paymentInputView.setOnSendMaxObserver(sendMaxObserver)

        paymentInputView.paymentHolder = paymentHolder

        assertThat<Button>(paymentInputView.sendMax, isGone())
    }

    @Test
    fun observing_clearing_of_send_max() {
        val sendMaxObserver = mock(PaymentInputView.OnSendMaxObserver::class.java)
        val sendMaxClearedObserver = mock(PaymentInputView.OnSendMaxClearedObserver::class.java)
        paymentHolder.updateValue(USDCurrency(0.0))
        paymentInputView.paymentHolder = paymentHolder
        clickOn(paymentInputView.sendMax)
        paymentInputView.setOnSendMaxObserver(sendMaxObserver)
        paymentInputView.setOnSendMaxClearedObserver(sendMaxClearedObserver)

        clickOn(paymentInputView.sendMax)

        paymentInputView.primaryCurrency.setText("0.01")

        verify(sendMaxObserver).onSendMax()
        verify(sendMaxClearedObserver, times(1)).onSendMaxCleared()
    }

    @Test
    fun can_disable_sending_max() {
        paymentInputView.canSendMax = false

        assertThat(paymentInputView.sendMax, isGone())
    }
}