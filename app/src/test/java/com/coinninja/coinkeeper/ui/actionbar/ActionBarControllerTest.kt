package com.coinninja.coinkeeper.ui.actionbar

import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.CryptoCurrency
import com.coinninja.coinkeeper.util.currency.FiatCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplaySyncView
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.mockito.ArgumentCaptor


class ActionBarControllerTest {

    private val activity: AppCompatActivity = mock()

    private fun createController(): ActionBarController = ActionBarController(mock()).also {
        whenever(activity.menuInflater).thenReturn(mock())
        whenever(activity.supportActionBar).thenReturn(mock())
        whenever(activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)).thenReturn(mock())
        whenever(activity.findViewById<TextView>(R.id.appbar_title)).thenReturn(mock())
        whenever(it.walletViewModel.syncInProgress).thenReturn(mock())
        whenever(it.walletViewModel.chainHoldings).thenReturn(mock())
        whenever(it.walletViewModel.chainHoldingsWorth).thenReturn(mock())
        whenever(it.walletViewModel.defaultCurrencyPreference).thenReturn(mock())
    }

    @Test
    fun actionbar_gone_configuration_test() {
        val controller = createController()
        val actionBarTyped = TypedValue()


        actionBarTyped.resourceId = R.id.actionbar_gone
        controller.setTheme(activity, actionBarTyped)
        assertThat(controller.isActionBarGone).isTrue()
        assertThat(controller.isUpEnabled).isFalse()
        assertThat(controller.optionMenuLayout).isNull()
    }

    @Test
    fun actionbar_light__up_on_configuration_test() {
        val controller = createController()
        val actionBarTyped = TypedValue()

        actionBarTyped.resourceId = R.id.actionbar_up_on
        controller.setTheme(activity, actionBarTyped)
        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isTrue()
        assertThat(controller.optionMenuLayout).isNull()


        actionBarTyped.resourceId = R.id.actionbar_up_on_balance_on
        controller.setTheme(activity, actionBarTyped)
        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isTrue()
        assertThat(controller.optionMenuLayout).isNull()


        actionBarTyped.resourceId = R.id.actionbar_up_on_with_nav_bar
        controller.setTheme(activity, actionBarTyped)
        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isTrue()
        assertThat(controller.optionMenuLayout).isNull()


        actionBarTyped.resourceId = R.id.actionbar_up_on_with_nav_bar_balance_on_charts_on
        controller.setTheme(activity, actionBarTyped)
        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isTrue()
        assertThat(controller.optionMenuLayout).isEqualTo(R.menu.actionbar_light_charts_menu)


        actionBarTyped.resourceId = R.id.actionbar_up_on_with_nav_bar_balance_on
        controller.setTheme(activity, actionBarTyped)
        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isTrue()
        assertThat(controller.optionMenuLayout).isNull()


        actionBarTyped.resourceId = R.id.actionbar_up_on_skip_on
        controller.setTheme(activity, actionBarTyped)
        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isTrue()
        assertThat(controller.optionMenuLayout).isEqualTo(R.menu.actionbar_light_skip_menu)
    }

    @Test
    fun actionbar_light__up_off_configuration_test() {
        val controller = createController()
        val actionBarTyped = TypedValue()

        actionBarTyped.resourceId = R.id.actionbar_up_off
        controller.setTheme(activity, actionBarTyped)
        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isFalse()
        assertThat(controller.optionMenuLayout).isNull()


        actionBarTyped.resourceId = R.id.actionbar_up_off_close_on
        controller.setTheme(activity, actionBarTyped)
        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isFalse()
        assertThat(controller.optionMenuLayout).isEqualTo(R.menu.actionbar_light_close_menu)


        actionBarTyped.resourceId = R.id.actionbar_up_off_skip_on
        controller.setTheme(activity, actionBarTyped)
        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isFalse()
        assertThat(controller.optionMenuLayout).isEqualTo(R.menu.actionbar_light_skip_menu)
    }


    @Test(expected = IllegalStateException::class)
    fun throw_illegal_state_exception_when_theme_unknown() {
        val controller = createController()

        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = -1

        controller.setTheme(activity, actionBarTyped)

    }

    @Test
    fun remove_can_container_layout_if_them_is_action_gone() {
        val controller = createController()
        val cnContainerLayout = mock<View>()
        whenever(activity.findViewById<View>(R.id.cn_appbar_layout_container)).thenReturn(cnContainerLayout)

        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_gone
        }

        controller.setTheme(activity, actionBarTyped)

        verify(cnContainerLayout).visibility = View.GONE
    }

    @Test
    fun if_up_is_enabled_then_setDisplayHomeAsUpEnabled_true() {
        val controller = createController()
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_on
        }

        controller.setTheme(activity, actionBarTyped)

        verify(activity.supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
    }

    @Test
    fun if_up_is_not_enabled_then_setDisplayHomeAsUpEnabled_false() {
        val controller = createController()
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_off
        }

        controller.setTheme(activity, actionBarTyped)

        verify(activity.supportActionBar)!!.setDisplayHomeAsUpEnabled(false)
    }

    @Test
    fun set_title_to_app_bar() {
        val controller = createController()
        val title = " --- TITLE --"
        whenever(activity.supportActionBar!!.title).thenReturn(title)
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_off
        }

        controller.setTheme(activity, actionBarTyped)

        val textView = activity.findViewById<TextView>(R.id.appbar_title)!!
        verify(textView).visibility = View.VISIBLE
        verify(textView).text = title
        verify(activity.supportActionBar!!).title = ""
    }

    @Test
    fun set_title_to_app_bar_directly() {
        val controller = createController()
        val title = " --- TITLE --"
        val titleWeDoNotWant = " --- TITLE BAD --"
        whenever(activity.supportActionBar!!.title).thenReturn(titleWeDoNotWant)
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_off
        }
        controller.setTheme(activity, actionBarTyped)


        controller.displayTitle(activity, title)


        val textView = activity.findViewById<TextView>(R.id.appbar_title)!!
        verify(textView, times(2)).visibility = View.VISIBLE
        verify(textView).text = title
        verify(activity.supportActionBar!!, times(2)).title = ""
    }

    @Test
    fun shows_balance_when_theme_requests_it_up_on_balance_on() {
        val controller = createController()
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_on_balance_on
        }

        controller.setTheme(activity, actionBarTyped)

        val balanceView = activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)!!
        verify(balanceView).visibility = View.VISIBLE
        assertThat(controller.isUpEnabled).isTrue()
    }

    @Test
    fun shows_balance_when_theme_requests_it_drawer_with_balance_on() {
        val controller = createController()
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_on_with_nav_bar_balance_on
        }

        controller.setTheme(activity, actionBarTyped)

        val balanceView = activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)!!
        verify(balanceView).visibility = View.VISIBLE
    }

    @Test
    fun shows_balance_with_charts() {
        val controller = createController()
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_on_with_nav_bar_balance_on_charts_on
        }

        controller.setTheme(activity, actionBarTyped)
        val menu = mock<Menu>()
        controller.inflateActionBarMenu(activity, menu)

        val balanceView = activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)!!
        verify(balanceView).visibility = View.VISIBLE
        verify(activity.menuInflater).inflate(R.menu.actionbar_light_charts_menu, menu)
    }

    @Test
    fun balance_loads_data_when_enabled() {
        val controller = createController()
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_on_with_nav_bar_balance_on
        }

        controller.setTheme(activity, actionBarTyped)

        verify(controller.walletViewModel).loadCurrencyDefaults()
        verify(controller.walletViewModel).loadHoldingBalances()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun balance_view_observes_syncing_activity() {
        val controller = createController()
        val argumentCaptor: ArgumentCaptor<Observer<Boolean>> = ArgumentCaptor.forClass(Observer::class.java)
                as ArgumentCaptor<Observer<Boolean>>
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_on_with_nav_bar_balance_on
        }

        controller.setTheme(activity, actionBarTyped)

        verify(controller.walletViewModel.syncInProgress, atLeastOnce()).observe(eq(activity), argumentCaptor.capture())

        argumentCaptor.value.onChanged(true)
        verify(activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)).showSyncingUI()
        argumentCaptor.value.onChanged(false)
        verify(activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)).hideSyncingUI()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun balance_view_observes_chainHoldings_changes() {
        val controller = createController()
        val holdings = BTCCurrency(1000L)
        val worth = USDCurrency(500.00)
        val currenciesPreferences = DefaultCurrencies(USDCurrency(), BTCCurrency())
        whenever(controller.walletViewModel.chainHoldingsWorth.value).thenReturn(worth)
        whenever(controller.walletViewModel.defaultCurrencyPreference.value).thenReturn(currenciesPreferences)
        val argumentCaptor: ArgumentCaptor<Observer<CryptoCurrency>> = ArgumentCaptor.forClass(Observer::class.java)
                as ArgumentCaptor<Observer<CryptoCurrency>>
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_on_with_nav_bar_balance_on
        }

        controller.setTheme(activity, actionBarTyped)

        verify(controller.walletViewModel.chainHoldings, atLeastOnce()).observe(eq(activity), argumentCaptor.capture())

        argumentCaptor.value.onChanged(holdings)

        verify(activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)).renderValues(currenciesPreferences, holdings, worth)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun balance_view_observes_chainHoldingsWorth_changes() {
        val controller = createController()
        val holdings = BTCCurrency(1000L)
        val worth = USDCurrency(500.00)
        val currenciesPreferences = DefaultCurrencies(USDCurrency(), BTCCurrency())
        whenever(controller.walletViewModel.chainHoldings.value).thenReturn(holdings)
        whenever(controller.walletViewModel.defaultCurrencyPreference.value).thenReturn(currenciesPreferences)
        val argumentCaptor: ArgumentCaptor<Observer<FiatCurrency>> = ArgumentCaptor.forClass(Observer::class.java)
                as ArgumentCaptor<Observer<FiatCurrency>>
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_on_with_nav_bar_balance_on
        }

        controller.setTheme(activity, actionBarTyped)

        verify(controller.walletViewModel.chainHoldingsWorth, atLeastOnce()).observe(eq(activity), argumentCaptor.capture())

        argumentCaptor.value.onChanged(worth)
        verify(activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)).renderValues(currenciesPreferences, holdings, worth)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun observes_balance_toggles_default_currency_preference() {
        val controller = createController()
        val holdings = BTCCurrency(1000L)
        val worth = USDCurrency(500.00)
        val currenciesPreferences = DefaultCurrencies(USDCurrency(), BTCCurrency())
        whenever(controller.walletViewModel.chainHoldings.value).thenReturn(holdings)
        whenever(controller.walletViewModel.chainHoldingsWorth.value).thenReturn(worth)
        val argumentCaptor: ArgumentCaptor<Observer<DefaultCurrencies>> = ArgumentCaptor.forClass(Observer::class.java)
                as ArgumentCaptor<Observer<DefaultCurrencies>>
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_on_with_nav_bar_balance_on
        }

        controller.setTheme(activity, actionBarTyped)

        verify(controller.walletViewModel.defaultCurrencyPreference, atLeastOnce()).observe(eq(activity), argumentCaptor.capture())

        argumentCaptor.value.onChanged(currenciesPreferences)
        verify(activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)).renderValues(currenciesPreferences, holdings, worth)
    }

    @Test
    fun clicking_balance_toggles_default_currency_preference() {
        val controller = createController()
        val argumentCaptor: ArgumentCaptor<View.OnClickListener> = ArgumentCaptor.forClass(View.OnClickListener::class.java)
        val actionBarTyped = TypedValue().apply {
            resourceId = R.id.actionbar_up_on_with_nav_bar_balance_on
        }

        controller.setTheme(activity, actionBarTyped)
        verify(activity.findViewById<DefaultCurrencyDisplaySyncView>(R.id.balance)).setOnClickListener(argumentCaptor.capture())
        argumentCaptor.value.onClick(mock())

        verify(controller.walletViewModel).toggleDefaultCurrencyPreference()
    }
}