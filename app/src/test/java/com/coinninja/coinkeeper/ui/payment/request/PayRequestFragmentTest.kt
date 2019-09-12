package com.coinninja.coinkeeper.ui.payment.request

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.view.button.CopyToBufferButton
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.io.ByteArrayInputStream

@RunWith(AndroidJUnit4::class)
class PayRequestFragmentTest {

    @Test
    fun set_up_copy_button_with_bitcoin_address() {
        val scenario = createScenario()

        scenario.onFragment { fragment ->
            assertThat(fragment.findViewById<CopyToBufferButton>(R.id.request_copy_button)!!.text).isEqualTo("--address--")
        }
    }

    @Test
    fun clicking_on_request_shows_chooser() {
        val scenario = createScenario()

        scenario.onFragment { fragment ->
            fragment.findViewById<View>(R.id.request_funds)!!.performClick()
        }

        val intent = Intents.getIntents().get(0)
        assertThat(intent.action).isEqualTo(Intent.ACTION_CHOOSER)
    }

    @Test
    fun request_rendering_of_a_qr_code_for_given_request() {
        val scenario = createScenario()

        scenario.onFragment { fragment ->
            val orderedOperations = inOrder(fragment.qrViewModel, fragment.qrViewModel.qrCodeUri)
            orderedOperations.verify(fragment.qrViewModel.qrCodeUri).observe(fragment, fragment.qrCodeUriObserver)
            orderedOperations.verify(fragment.qrViewModel).requestQrCodeFor(fragment.requestAddress)
        }
    }

    @Test
    fun removes_observer_from_qr_code_view_model_when_paused() {
        val scenario = createScenario()

        scenario.onFragment { fragment ->
            val observer = fragment.qrCodeUriObserver
            val viewModel = fragment.qrViewModel

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(viewModel.qrCodeUri).removeObserver(observer)
        }
    }

    @Test
    fun updates_qr_code_when_generated() {
        val scenario = createScenario()

        scenario.onFragment { fragment ->
            val location = "file://local/generated/qr_code"
            val shadowResolver = shadowOf(ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().contentResolver)
            shadowResolver.registerInputStream(Uri.parse(location), ByteArrayInputStream("".toByteArray()))
            val image = fragment.findViewById<ImageView>(R.id.qr_code)!!

            fragment.qrCodeUriObserver.onChanged(Uri.parse(location))

            assertThat(image.visibility).isEqualTo(View.VISIBLE)
            assertThat(image.tag).isEqualTo(location)
        }
    }

    private fun createScenario(): FragmentScenario<PayRequestFragment> {
        val address = "--address--"
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().also { app ->
            app.accountManager = mock()
            app.bitcoinUriBuilder = mock()
            val bitcoinUri: BitcoinUri = mock()
            whenever(bitcoinUri.address).thenReturn(address)
            whenever(app.accountManager.nextReceiveAddress).thenReturn(address)
            whenever(app.bitcoinUriBuilder.setAddress(address)).thenReturn(app.bitcoinUriBuilder)
            whenever(app.bitcoinUriBuilder.build()).thenReturn(bitcoinUri)

        }
        return FragmentScenario.launch(PayRequestFragment::class.java)
    }
}