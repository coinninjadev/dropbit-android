package com.coinninja.coinkeeper.ui.payment.request

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.viewModel.QrViewModel
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.io.ByteArrayInputStream

@RunWith(AndroidJUnit4::class)
class LndInvoiceRequestActivityTest {
    private val lndInvoiceRequest get() = LndInvoiceRequest("ln-encoded-invoice", BTCCurrency(0), "")
    private val creationIntent: Intent
        get() = Intent(ApplicationProvider.getApplicationContext(), LndInvoiceRequestActivity::class.java).also {
            it.putExtra(DropbitIntents.EXTRA_LND_INVOICE_REQUEST, lndInvoiceRequest)
        }

    private fun createScenario(intent: Intent = creationIntent): ActivityScenario<LndInvoiceRequestActivity> = ActivityScenario.launch(intent)

    @Test
    fun inits_correctly() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            assertThat(activity.lndInvoiceRequest).isEqualTo(lndInvoiceRequest)
            assertThat(activity.copyToBufferButton.text).isEqualTo("ln-encoded-invoice")
            assertThat(activity.memo.visibility).isEqualTo(View.GONE)
            assertThat(activity.amountDisplayView.visibility).isEqualTo(View.GONE)
            verify(activity.walletViewModel.currentPrice).observe(activity, activity.latestPriceObserver)
            verify(activity.qrViewModel.qrCodeUri).observe(activity, activity.qrCodeUriObserver)
            verify(activity.qrViewModel).requestQrCodeFor(lndInvoiceRequest.request)

        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun close_button_navigates_back() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.closeButton.performClick()

            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun pausing_removes_observers() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val latestPriceObserver = activity.latestPriceObserver
            val qrCodeUriObserver = activity.qrCodeUriObserver

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(activity.walletViewModel.currentPrice).removeObserver(latestPriceObserver)
            verify(activity.qrViewModel.qrCodeUri).removeObserver(qrCodeUriObserver)
        }

        scenario.close()
    }

    @Test
    fun sets_memo_when_it_is_present() {
        val scenario = createScenario(creationIntent.also {
            it.putExtra(DropbitIntents.EXTRA_LND_INVOICE_REQUEST, LndInvoiceRequest("ln-encoded", BTCCurrency(0), "--memo--"))
        })

        scenario.onActivity { activity ->
            assertThat(activity.memo.text).isEqualTo("--memo--")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun sets_amount_when_present() {
        val scenario = createScenario(creationIntent.also {
            it.putExtra(DropbitIntents.EXTRA_LND_INVOICE_REQUEST, LndInvoiceRequest("ln-encoded", BTCCurrency(150_000_000), ""))
        })

        scenario.onActivity { activity ->
            assertThat(activity.amountDisplayView.secondaryCurrencyText).isEqualTo("150,000,000 sats")
            assertThat(activity.amountDisplayView.primaryCurrencyText).isEqualTo("$0.00")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun renders_price_when_price_is_fetched() {
        val scenario = createScenario(creationIntent.also {
            it.putExtra(DropbitIntents.EXTRA_LND_INVOICE_REQUEST, LndInvoiceRequest("ln-encoded", BTCCurrency(150_000_000), ""))
        })

        scenario.onActivity { activity ->
            activity.latestPriceObserver.onChanged(USDCurrency(10_000_00))
            activity.latestPriceObserver.onChanged(USDCurrency(10_500_00))

            assertThat(activity.amountDisplayView.secondaryCurrencyText).isEqualTo("150,000,000 sats")
            assertThat(activity.amountDisplayView.primaryCurrencyText).isEqualTo("$15,000.00")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun renders_qr_code_when_qr_code_is_generated() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            val location = "file://local/generated/qr_code"
            val shadowResolver = Shadows.shadowOf(ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().contentResolver)
            shadowResolver.registerInputStream(Uri.parse(location), ByteArrayInputStream("".toByteArray()))
            val image = activity.qrCodeImage

            activity.qrCodeUriObserver.onChanged(Uri.parse(location))

            assertThat(image.visibility).isEqualTo(View.VISIBLE)
            assertThat(image.tag).isEqualTo(location)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun clicking_on_request_shows_chooser() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.requestButton.performClick()
        }

        val intent = Intents.getIntents().get(0)
        assertThat(intent.action).isEqualTo(Intent.ACTION_CHOOSER)

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Module
    class LndInvoiceRequestActivityTestModule {

        @Provides
        fun provideQRViewModel(): QrViewModel {
            val viewModel: QrViewModel = mock()
            whenever(viewModel.qrCodeUri).thenReturn(mock())
            return viewModel
        }

    }
}