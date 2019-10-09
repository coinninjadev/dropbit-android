package com.coinninja.coinkeeper.ui.payment.request

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.thunderdome.CreateInvoiceViewModel
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.ui.memo.MemoCreator
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.viewModel.QrViewModel
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import kotlinx.android.synthetic.main.fragment_pay_dialog.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import java.io.ByteArrayInputStream

@RunWith(AndroidJUnit4::class)
class PayRequestActivityTest {

    private fun createScenario(accountMode: AccountMode = AccountMode.BLOCKCHAIN): ActivityScenario<PayRequestActivity> {
        val address = "--address--"
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().also { app ->
            app.accountManager = mock()
            app.bitcoinUriBuilder = mock()
            val bitcoinUri: BitcoinUri = mock()
            whenever(bitcoinUri.address).thenReturn(address)
            whenever(app.accountManager.nextReceiveAddress).thenReturn(address)
            whenever(app.bitcoinUriBuilder.setAddress(address)).thenReturn(app.bitcoinUriBuilder)
            whenever(app.bitcoinUriBuilder.build()).thenReturn(bitcoinUri)
            whenever(app.bitcoinUriBuilder.setAmount(any())).thenReturn(app.bitcoinUriBuilder)
            whenever(app.bitcoinUriBuilder.removeAmount()).thenReturn(app.bitcoinUriBuilder)
        }
        val scenario = ActivityScenario.launch(PayRequestActivity::class.java)
        scenario.onActivity {
            it.latestPriceObserver.onChanged(USDCurrency(10_500_00))
            it.onAccountModeChanged(accountMode)
        }
        return scenario
    }

    @Test
    fun inits_views() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            assertThat(activity.amountInputView.visibility).isEqualTo(View.GONE)
            assertThat(activity.amountInputView.canSendMax).isFalse()
            assertThat(activity.amountInputView.canToggleCurrencies).isFalse()
            assertThat(activity.amountInputView.paymentHolder.evaluationCurrency.toLong()).isEqualTo(10_500_00)
            verify(activity.walletViewModel.currentPrice).observe(activity, activity.latestPriceObserver)
            verify(activity.walletViewModel.isLightningLocked).observe(activity, activity.isLightningLockedObserver)
            assertThat(activity.accountModeToggle.onModeSelectedObserver).isEqualTo(activity.accountModeToggleObserver)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun removes_observers_when_paused() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val qrObserver = activity.qrCodeUriObserver
            val isLightningLockedObserver = activity.isLightningLockedObserver
            val priceObserver = activity.latestPriceObserver
            val qrViewModel = activity.qrViewModel
            val createInvoiceRequestObserver = activity.createInvoiceRequestObserver
            val createInvoiceViewModel = activity.createInvoiceViewModel
            val walletViewModel = activity.walletViewModel

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(qrViewModel.qrCodeUri, atLeast(1)).removeObserver(qrObserver)
            verify(walletViewModel.currentPrice).removeObserver(priceObserver)
            verify(walletViewModel.isLightningLocked).removeObserver(isLightningLockedObserver)
            verify(createInvoiceViewModel.request).removeObserver(createInvoiceRequestObserver)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // BITCOIN

    @Test
    fun allows_user_to_close_dialog() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            assertThat(activity.isFinishing).isFalse()

            activity.closeButton.performClick()

            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun set_up_copy_button_with_bitcoin_address() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            assertThat(activity.copyToBufferButton.text).isEqualTo("--address--")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun clicking_on_request_shows_chooser() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.requestFundsButton.performClick()
        }

        val intent = Intents.getIntents().get(0)
        assertThat(intent.action).isEqualTo(Intent.ACTION_CHOOSER)

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun updates_qr_code_when_generated() {
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
    fun allows_user_to_add_amount() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            assertThat(activity.addAmountButton.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.amountInputView.visibility).isEqualTo(View.GONE)

            activity.addAmountButton.performClick()

            assertThat(activity.addAmountButton.visibility).isEqualTo(View.GONE)
            assertThat(activity.amountInputView.visibility).isEqualTo(View.VISIBLE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun requests_new_bitcoin_uri_when_amount_entered() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.payment_input_view.primaryCurrency.setText("10.00")

            val ordered = inOrder(activity.bitcoinUriBuilder, activity.qrViewModel)

            ordered.verify(activity.bitcoinUriBuilder).setAmount(activity.payment_input_view.paymentHolder.btcCurrency)
            ordered.verify(activity.bitcoinUriBuilder).build()
            ordered.verify(activity.qrViewModel).requestQrCodeFor(activity.requestAddress)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun requests_new_bitcoin_uri_when_amount_zeroed() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.payment_input_view.primaryCurrency.setText("")

            val ordered = inOrder(activity.bitcoinUriBuilder, activity.qrViewModel)

            ordered.verify(activity.bitcoinUriBuilder).removeAmount()
            ordered.verify(activity.bitcoinUriBuilder).build()
            ordered.verify(activity.qrViewModel).requestQrCodeFor(activity.requestAddress)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun copy_button_only_shows_address_when_amount_entered() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.payment_input_view.primaryCurrency.setText("10.00")


            assertThat(activity.copyToBufferButton.text).isEqualTo("--address--")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }
    // Lightning

    @Test
    fun lightning_locked_by_default() {
        val scenario = createScenario(AccountMode.LIGHTNING)

        scenario.onActivity { activity ->
            assertThat(activity.accountModeToggle.isLightningLocked).isTrue()

            activity.isLightningLockedObserver.onChanged(false)

            assertThat(activity.accountModeToggle.isLightningLocked).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun changing_amount_does_not_request_qr_code() {
        val scenario = createScenario(AccountMode.LIGHTNING)

        scenario.onActivity { activity ->
            activity.payment_input_view.primaryCurrency.setText("10.00")


            verify(activity.bitcoinUriBuilder, times(0)).setAmount(activity.payment_input_view.paymentHolder.btcCurrency)
            verify(activity.qrViewModel, times(0)).requestQrCodeFor(activity.requestAddress)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun creates_invoice_when_pressed() {
        val scenario = createScenario(AccountMode.LIGHTNING)

        scenario.onActivity { activity ->
            activity.payment_input_view.primaryCurrency.setText("10.00")
            activity.addMemoButton.setText("-- memo --")

            activity.requestFundsButton.performClick()

            verify(activity.createInvoiceViewModel.request).observe(activity, activity.createInvoiceRequestObserver)
            verify(activity.createInvoiceViewModel).createInvoiceFor(activity.amountInputView.paymentHolder.btcCurrency.toLong(), "-- memo --")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun navigates_to_review_created_invoice() {
        val scenario = createScenario(AccountMode.LIGHTNING)

        scenario.onActivity { activity ->
            activity.payment_input_view.primaryCurrency.setText("10.00")
            activity.addMemoButton.setText("-- memo --")

            activity.createInvoiceRequestObserver.onChanged("ln--encoded_invoice")

            verify(activity.activityNavigationUtil).navigateToShowLndInvoice(activity,
                    LndInvoiceRequest(
                            "ln--encoded_invoice",
                            activity.payment_input_view.paymentHolder.btcCurrency,
                            "-- memo --"
                    ))
            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun allows_user_to_create_a_memo() {
        val scenario = createScenario(AccountMode.LIGHTNING)

        scenario.onActivity { activity ->
            activity.addMemoButton.performClick()

            verify(activity.memoCreator).createMemo(activity, activity.createMemoCallback, "")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun forwards_memo_when_present() {
        val scenario = createScenario(AccountMode.LIGHTNING)

        scenario.onActivity { activity ->
            activity.addMemoButton.text = "I am a memo"
            activity.addMemoButton.performClick()

            verify(activity.memoCreator).createMemo(activity, activity.createMemoCallback, "I am a memo")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun updates_button_text_with_memo_copy() {
        val scenario = createScenario(AccountMode.LIGHTNING)

        scenario.onActivity { activity ->
            activity.createMemoCallback.onMemoCreated("")
            assertThat(activity.addMemoButton.text).isEqualTo(activity.getString(R.string.add_a_memo))

            activity.createMemoCallback.onMemoCreated("I am a memo")
            assertThat(activity.addMemoButton.text).isEqualTo("I am a memo")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }


    //Toggling between modes

    @Test
    fun toggling_to_lightning_changes_mode_to_lightning() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.isLightningLockedObserver.onChanged(false)
            activity.amountInputView.show()
            activity.addMemoButton.show()
            activity.qrCodeImage.gone()
            activity.addAmountButton.gone()
            activity.amountInputView.paymentHolder.updateValue(USDCurrency(10_00))

            activity.accountModeToggle.lightningButton.performClick()

            verify(activity.walletViewModel, atLeast(1)).setMode(AccountMode.LIGHTNING)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()

    }

    @Test
    fun observing_mode_change_updates_to_that_mode___Lightning() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.isLightningLockedObserver.onChanged(false)
            activity.amountInputView.show()
            activity.addMemoButton.show()
            activity.qrCodeImage.gone()
            activity.addAmountButton.gone()
            activity.amountInputView.paymentHolder.updateValue(USDCurrency(10_00))

            activity.onAccountModeChanged(AccountMode.LIGHTNING)

            assertThat(activity.accountModeToggle.mode).isEqualTo(AccountMode.LIGHTNING)
            assertThat(activity.qrCodeImage.visibility).isEqualTo(View.GONE)
            assertThat(activity.addAmountButton.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.addMemoButton.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.amountInputView.visibility).isEqualTo(View.GONE)
            assertThat(activity.amountInputView.paymentHolder.primaryCurrency.isZero).isTrue()
            assertThat(activity.copyToBufferButton.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.copyLabel.visibility).isEqualTo(View.INVISIBLE)
            assertThat(activity.requestFundsButton.text).isEqualTo(activity.getText(R.string.create_invoice))
            assertThat(activity.amountInputView.onValidEntryObserver).isNull()
            assertThat(activity.amountInputView.onZeroedObserver).isNull()
            verify(activity.qrViewModel.qrCodeUri, atLeast(1)).removeObserver(activity.qrCodeUriObserver)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun toggling_to_blockchain_clears_input_and_shows_blockchain() {
        val scenario = createScenario(AccountMode.LIGHTNING)

        scenario.onActivity { activity ->

            activity.accountModeToggle.blockchainButton.performClick()

            verify(activity.walletViewModel, atLeast(1)).setMode(AccountMode.BLOCKCHAIN)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()

    }

    @Test
    fun observing_mode_change_updates_to_that_mode___Blockchain() {
        val scenario = createScenario(AccountMode.LIGHTNING)

        scenario.onActivity { activity ->
            activity.isLightningLockedObserver.onChanged(false)
            activity.amountInputView.show()
            activity.addMemoButton.show()
            activity.qrCodeImage.gone()
            activity.addAmountButton.gone()
            activity.copyToBufferButton.gone()
            activity.copyToBufferButton.gone()
            activity.copyLabel.gone()
            activity.amountInputView.paymentHolder.updateValue(USDCurrency(10_00))

            activity.onAccountModeChanged(AccountMode.BLOCKCHAIN)

            assertThat(activity.accountModeToggle.mode).isEqualTo(AccountMode.BLOCKCHAIN)
            assertThat(activity.qrCodeImage.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.addAmountButton.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.addMemoButton.visibility).isEqualTo(View.GONE)
            assertThat(activity.amountInputView.visibility).isEqualTo(View.GONE)
            assertThat(activity.copyToBufferButton.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.copyLabel.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.requestFundsButton.text).isEqualTo(activity.getText(R.string.send_request))
            assertThat(activity.amountInputView.paymentHolder.primaryCurrency.isZero).isTrue()
            assertThat(activity.amountInputView.onValidEntryObserver).isNotNull()
            assertThat(activity.amountInputView.onZeroedObserver).isNotNull()
            verify(activity.qrViewModel.qrCodeUri, atLeast(1)).observe(activity, activity.qrCodeUriObserver)
            verify(activity.qrViewModel, atLeast(1)).requestQrCodeFor(activity.requestAddress)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }


    @Module
    class PayRequestActivityTestModule {

        @Provides
        fun provideQRViewModel(): QrViewModel {
            val viewModel: QrViewModel = mock()
            whenever(viewModel.qrCodeUri).thenReturn(mock())
            return viewModel
        }

        @Provides
        fun provideMemoCreator(): MemoCreator = mock()

        @Provides
        fun createInvoiceViewModel(): CreateInvoiceViewModel {
            val createInvoiceViewModel: CreateInvoiceViewModel = mock()
            whenever(createInvoiceViewModel.request).thenReturn(mock())
            return createInvoiceViewModel
        }


    }
}
