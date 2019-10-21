package com.coinninja.coinkeeper.ui.payment.confirm

import android.content.Intent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.libbitcoin.model.UnspentTransactionOutput
import app.coinninja.cn.thunderdome.model.RequestInvoice
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.FeesManager
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.coinninja.coinkeeper.view.subviews.SharedMemoView
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class ConfirmPaymentActivityTest {

    private val creationIntent: Intent = Intent(ApplicationProvider.getApplicationContext(), ConfirmPaymentActivity::class.java)


    private fun createScenario(paymentHolder: PaymentHolder = PaymentHolder(),
                               toUser: Identity? = null,
                               transactionData: TransactionData? = null,
                               requestInvoice: RequestInvoice? = null,
                               mode: AccountMode = AccountMode.BLOCKCHAIN,
                               withAdjustableFees: Boolean = false,
                               isSendingMax: Boolean = false,
                               memo: String = ""

    ): ActivityScenario<ConfirmPaymentActivity> {

        val intent = creationIntent
        toUser?.let { paymentHolder.toUser = it }
        transactionData?.let { paymentHolder.transactionData = it }
        requestInvoice?.let { paymentHolder.requestInvoice = it }
        paymentHolder.memo = memo
        paymentHolder.isSendingMax = isSendingMax
        paymentHolder.accountMode = mode

        intent.putExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER, paymentHolder)

        val scenario = ActivityScenario.launch<ConfirmPaymentActivity>(intent)
        scenario.onActivity { activity ->
            whenever(activity.feesManager.isAdjustableFeesEnabled).thenReturn(withAdjustableFees)
            activity.onAccountModeChanged(mode)
        }
        return scenario
    }

    @Test
    fun lifecycle__inits_observes_view_models() {
        val scenario = createScenario()

        scenario.onActivity { activity ->

            verify(activity.fundingViewModel.transactionData).observe(activity, activity.transactionDataChangeObserver)
            verify(activity.syncWalletManager).cancel30SecondSync()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun lifecycle__pausing_removes_observers() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(activity.fundingViewModel.transactionData).removeObserver(activity.transactionDataChangeObserver)
        }

        scenario.close()
    }

    @Test
    fun pressing_close_button_finishes_payment() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.closeButton.performClick()

            verify(activity.activityNavigationUtil).navigateToHome(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // AMOUNT SENDING
    @Test
    fun payment_amount__renders_as_BLOCKCHAIN() {
        val paymentHolder = PaymentHolder(USDCurrency(10_000_00))
        paymentHolder.defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
        paymentHolder.updateValue(USDCurrency(100_00))
        val scenario = createScenario(
                paymentHolder = paymentHolder,
                mode = AccountMode.BLOCKCHAIN
        )

        scenario.onActivity { activity ->
            assertThat(activity.amountView.primaryCurrencyText).isEqualTo("$100.00")
            assertThat(activity.amountView.secondaryCurrencyText).isEqualTo("0.01")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun payment_amount__renders_as_LIGHTNING() {
        val paymentHolder = PaymentHolder(USDCurrency(10_000_00))
        paymentHolder.defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
        paymentHolder.updateValue(USDCurrency(100_00))
        val scenario = createScenario(
                paymentHolder = paymentHolder,
                mode = AccountMode.LIGHTNING
        )

        scenario.onActivity { activity ->
            assertThat(activity.amountView.primaryCurrencyText).isEqualTo("$100.00")
            assertThat(activity.amountView.secondaryCurrencyText).isEqualTo("1,000,000 sats")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // FEE 

    @Test
    fun network_fee__gone_when_LIGHTNING() {
        val scenario = createScenario(
                mode = AccountMode.LIGHTNING
        )

        scenario.onActivity { activity ->
            assertThat(activity.networkFeeView.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun network_fee__renders_when_BLOCKCHAIN() {
        val scenario = createScenario(
                paymentHolder = PaymentHolder(USDCurrency(10_000_00)),
                mode = AccountMode.BLOCKCHAIN,
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000)
        )

        scenario.onActivity { activity ->
            assertThat(activity.networkFeeView.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.networkFeeView.text).isEqualTo("Network fee 0.0001 ($1.00)")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // FEE SELECTION

    @Test
    fun adjustable_fees__renders_selection() {
        val scenario = createScenario(
                paymentHolder = PaymentHolder(USDCurrency(10_000_00)),
                mode = AccountMode.BLOCKCHAIN,
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000),
                withAdjustableFees = true
        )

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.VISIBLE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adjustable_fees__gone_for_BLOCKCHAIN__when_preference_not_set() {
        val scenario = createScenario(
                mode = AccountMode.BLOCKCHAIN,
                withAdjustableFees = false

        )

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adjustable_fees__gone_for_LIGHTNING() {
        val scenario = createScenario(
                mode = AccountMode.LIGHTNING
        )

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adjustable_fees__fee_recalculates_when_selection_changes__when_sending_max() {
        val scenario = createScenario(
                mode = AccountMode.BLOCKCHAIN,
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000, paymentAddress = "--payment-address--"),
                isSendingMax = true,
                withAdjustableFees = true
        )

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.VISIBLE)
            activity.adjustableFeesTabs.selectTab(activity.adjustableFeesTabs.getTabAt(1))

            verify(activity.fundingViewModel).fundMax(any(), eq(10.0))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adjustable_fees__fee_recalculates_when_selection_changes__when_not_sending_max() {
        val scenario = createScenario(
                mode = AccountMode.BLOCKCHAIN,
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000, paymentAddress = "--payment-address--"),
                withAdjustableFees = true
        )

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.VISIBLE)
            activity.adjustableFeesTabs.selectTab(activity.adjustableFeesTabs.getTabAt(1))

            verify(activity.fundingViewModel).fundTransaction(any(), eq(1_000_000), eq(10.0))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adjustable_fees__fee_recalculates_when_selection_changes__adopts_new_tx_data_and_updates_wait_time() {
        val scenario = createScenario(
                mode = AccountMode.BLOCKCHAIN,
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000, paymentAddress = "--payment-address--"),
                withAdjustableFees = true
        )

        val updatedTX = TransactionData(arrayOf(mock()), 1_000_000, 10_500, paymentAddress = "--payment-address--")

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.VISIBLE)
            activity.stagedFeePreference = FeesManager.FeeType.SLOW

            activity.transactionDataChangeObserver.onChanged(updatedTX)

            assertThat(activity.paymentHolder.transactionData.feeAmount).isEqualTo(10_500)
            assertThat(activity.estimatedDeliveryTime.text).isEqualTo(activity.getString(R.string.approx_hour_wait))
            assertThat(activity.feePreference).isEqualTo(FeesManager.FeeType.SLOW)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adjustable_fees__fee_recalculates_when_selection_changes__adopts_new_tx_data_and_updates_wait_time__sending_max_updates_payment_amount() {
        val holder = PaymentHolder(USDCurrency(10_500_00))
        holder.updateValue(BTCCurrency(1_000_000))
        holder.updateValue(holder.fiat)
        val scenario = createScenario(
                paymentHolder = holder,
                mode = AccountMode.BLOCKCHAIN,
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000, paymentAddress = "--payment-address--"),
                withAdjustableFees = true
        )

        val updatedTX = TransactionData(arrayOf(mock()), 900_500, 10_500, paymentAddress = "--payment-address--")

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.VISIBLE)
            activity.stagedFeePreference = FeesManager.FeeType.SLOW

            activity.transactionDataChangeObserver.onChanged(updatedTX)

            assertThat(activity.paymentHolder.transactionData.feeAmount).isEqualTo(10_500)
            assertThat(activity.paymentHolder.transactionData.amount).isEqualTo(900_500)
            assertThat(activity.paymentHolder.crypto.toLong()).isEqualTo(900_500)
            assertThat(activity.estimatedDeliveryTime.text).isEqualTo(activity.getString(R.string.approx_hour_wait))
            assertThat(activity.feePreference).isEqualTo(FeesManager.FeeType.SLOW)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adjustable_fees__fee_recalculates_when_selection_changes__notifies_user_that_adjusted_fee_gives_them_insufficient_funds() {
        val scenario = createScenario(
                mode = AccountMode.BLOCKCHAIN,
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000, paymentAddress = "--payment-address--"),
                withAdjustableFees = true
        )

        val updatedTX = TransactionData(emptyArray(), 0, 0)

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.VISIBLE)
            activity.stagedFeePreference = FeesManager.FeeType.SLOW

            activity.transactionDataChangeObserver.onChanged(updatedTX)

            assertThat(activity.paymentHolder.transactionData.feeAmount).isEqualTo(10_000)
            assertThat(activity.estimatedDeliveryTime.text).isEqualTo(activity.getString(R.string.approx_day_wait))
            assertThat(activity.feePreference).isEqualTo(FeesManager.FeeType.CHEAP)

            val dialog = activity.supportFragmentManager.findFragmentByTag(ConfirmPaymentActivity.errorDialogTag) as GenericAlertDialog
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.fee_too_high_error))
        }



        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // RECIEPIANT RENDERING

    @Test
    fun reciepiant__phone__number() {
        val scenario = createScenario(
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000, paymentAddress = "--payment-address--"),
                toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "", isVerified = true))

        scenario.onActivity { activity ->
            assertThat(activity.nameField.text).isEqualTo("+1 330-555-1111")
            assertThat(activity.addressField.text).isEqualTo("--payment-address--")
            assertThat(activity.avatar.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun reciepiant__phone__number__name() {
        val scenario = createScenario(
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000, paymentAddress = "--payment-address--"),
                toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = true))

        scenario.onActivity { activity ->
            assertThat(activity.nameField.text).isEqualTo("Joe Smoe")
            assertThat(activity.addressField.text).isEqualTo("--payment-address--")
            assertThat(activity.avatar.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun reciepiant__phone__number__name__not_verified() {
        val scenario = createScenario(
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000),
                toUser = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Joe Smoe", isVerified = false))

        scenario.onActivity { activity ->
            assertThat(activity.nameField.text).isEqualTo("Joe Smoe")
            assertThat(activity.addressField.text).isEqualTo("")
            assertThat(activity.avatar.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun reciepiant__twitter__handle__display_name__avatar() {
        val scenario = createScenario(
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000, paymentAddress = "--payment-address--"),
                toUser = Identity(IdentityType.TWITTER, "1234567890", "1234567890", "Joe Smoe",
                        handle = "@Joe", isVerified = false, avatarUrl = "http://example.com/avatar"))

        scenario.onActivity { activity ->
            assertThat(activity.nameField.text).isEqualTo("@Joe")
            assertThat(activity.avatar.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.avatar.tag).isEqualTo("http://example.com/avatar")
            assertThat(activity.addressField.text).isEqualTo("--payment-address--")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun reciepiant__twitter__handle__display_name__avatar__not_verified() {
        val scenario = createScenario(
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000),
                toUser = Identity(IdentityType.TWITTER, "1234567890", "1234567890", "Joe Smoe",
                        handle = "@Joe", isVerified = false, avatarUrl = "http://example.com/avatar"))

        scenario.onActivity { activity ->
            assertThat(activity.nameField.text).isEqualTo("@Joe")
            assertThat(activity.avatar.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.avatar.tag).isEqualTo("http://example.com/avatar")
            assertThat(activity.addressField.text).isEqualTo("")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun reciepiant__pay_to_address() {
        val scenario = createScenario(
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000, paymentAddress = "--payment-address--")
        )

        scenario.onActivity { activity ->
            assertThat(activity.nameField.text).isEqualTo("")
            assertThat(activity.avatar.visibility).isEqualTo(View.GONE)
            assertThat(activity.addressField.text).isEqualTo("--payment-address--")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun reciepiant__pay_to_lnd_invoice() {
        val requestInvoice = RequestInvoice(numSatoshis = 95238)
        requestInvoice.encoded = "ln--encoded-invoice"
        val scenario = createScenario(
                requestInvoice = requestInvoice
        )

        scenario.onActivity { activity ->
            assertThat(activity.addressField.text).isEqualTo(requestInvoice.encoded)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // MEMO RENDERING

    @Test
    fun memo__renders() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            verify(activity.sharedMemoView).render(activity.sharedMemoViewGroup,
                    activity.paymentHolder.isSharingMemo,
                    memoText = activity.paymentHolder.memo,
                    displayText = activity.paymentHolder.toUser?.displayName
            )

        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // MEMO RENDERING

    @Test
    fun authorizes_payment() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.confirmHoldButton.onConfirmHoldEndListener.onHoldCompleteSuccessfully()

            val authRequest = shadowOf(activity).peekNextStartedActivityForResult()
            assertThat(authRequest.requestCode).isEqualTo(ConfirmPaymentActivity.authRequestCode)
            assertThat(authRequest.intent.component.className).isEqualTo(AuthorizedActionActivity::class.java.name)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun authorization_success__invites_contact__blockchain() {
        val paymentHolder = PaymentHolder(
                evaluationCurrency = USDCurrency(10_500),
                toUser = Identity(IdentityType.TWITTER, "1234567890", hash = "1234567890",
                        handle = "@Joe", avatarUrl = "http://avatar", isVerified = true),
                memo = "Yo Joe!",
                publicKey = "--pub-key--",
                isSharingMemo = true
        )
        paymentHolder.transactionData = TransactionData(
                arrayOf(UnspentTransactionOutput("--txid--", 0, 10_500)),
                10_000, 500
        )

        val scenario = createScenario(paymentHolder,
                memo = paymentHolder.memo,
                mode = AccountMode.BLOCKCHAIN)

        scenario.onActivity { activity ->
            activity.onActivityResult(ConfirmPaymentActivity.authRequestCode, AuthorizedActionActivity.RESULT_AUTHORIZED, null)

            verify(activity.activityNavigationUtil).navigateToInviteSendScreen(activity, PendingInviteDTO(
                    paymentHolder.toUser!!,
                    10_500,
                    10_000,
                    500,
                    "Yo Joe!",
                    true,
                    paymentHolder.requestId
            ))

            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun authorization_success__sends_funds__blockchain() {
        val paymentHolder = PaymentHolder(
                evaluationCurrency = USDCurrency(10_500),
                toUser = Identity(IdentityType.TWITTER, "1234567890", hash = "1234567890",
                        handle = "@Joe", avatarUrl = "http://avatar", isVerified = true),
                memo = "Yo Joe!",
                publicKey = "--pub-key--",
                isSharingMemo = true
        )
        paymentHolder.transactionData = TransactionData(
                arrayOf(UnspentTransactionOutput("--txid--", 0, 10_500)),
                10_000, 500, paymentAddress = "--payment-address--"
        )

        val scenario = createScenario(paymentHolder, mode = AccountMode.BLOCKCHAIN, memo = paymentHolder.memo)

        val dto = BroadcastTransactionDTO(
                paymentHolder.transactionData,
                true,
                "Yo Joe!",
                paymentHolder.toUser,
                "--pub-key--"
        )

        scenario.onActivity { activity ->
            activity.onActivityResult(ConfirmPaymentActivity.authRequestCode, AuthorizedActionActivity.RESULT_AUTHORIZED, null)

            verify(activity.activityNavigationUtil).navigateToBroadcast(activity, dto)
            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun authorization_success__sends_funds__lightning() {
        val paymentHolder = PaymentHolder(
                evaluationCurrency = USDCurrency(10_500),
                toUser = Identity(IdentityType.TWITTER, "1234567890", hash = "1234567890",
                        handle = "@Joe", avatarUrl = "http://avatar", isVerified = true),
                memo = "Yo Joe!",
                publicKey = "--pub-key--",
                isSharingMemo = true
        )
        paymentHolder.requestInvoice = RequestInvoice(numSatoshis = 100418)
        paymentHolder.requestInvoice!!.encoded = "ln--encoded-invoice"

        val scenario = createScenario(paymentHolder, mode = AccountMode.LIGHTNING, memo = paymentHolder.memo)

        scenario.onActivity { activity ->
            activity.onActivityResult(ConfirmPaymentActivity.authRequestCode, AuthorizedActionActivity.RESULT_AUTHORIZED, null)

            verify(activity.activityNavigationUtil).navigateToLightningBroadcast(activity, paymentHolder)
            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun authorization_success__invites_contact__lightning() {
        val paymentHolder = PaymentHolder(
                evaluationCurrency = USDCurrency(10_500),
                toUser = Identity(IdentityType.TWITTER, "1234567890", hash = "1234567890",
                        handle = "@Joe", avatarUrl = "http://avatar", isVerified = true),
                memo = "Yo Joe!",
                publicKey = "--pub-key--",
                isSharingMemo = true
        )

        val scenario = createScenario(paymentHolder,
                mode = AccountMode.LIGHTNING,
                memo = paymentHolder.memo)

        scenario.onActivity { activity ->
            activity.onActivityResult(ConfirmPaymentActivity.authRequestCode, AuthorizedActionActivity.RESULT_AUTHORIZED, null)

            verify(activity.activityNavigationUtil).navigateToInviteContactScreen(activity, paymentHolder)
            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Module
    class ConfirmPaymentActivityTestModule {
        @Provides
        fun sharedMemoView(): SharedMemoView = mock()

        @Provides
        fun feesManager(): FeesManager {
            val feesManager: FeesManager = mock()
            whenever(feesManager.feePreference).thenReturn(FeesManager.FeeType.CHEAP)
            whenever(feesManager.fee(FeesManager.FeeType.FAST)).thenReturn(20.0)
            whenever(feesManager.fee(FeesManager.FeeType.SLOW)).thenReturn(10.0)
            whenever(feesManager.fee(FeesManager.FeeType.CHEAP)).thenReturn(5.0)
            return feesManager
        }

        @Provides
        fun fundingViewModelProvider(): FundingViewModelProvider {
            val provider = mock<FundingViewModelProvider>()
            val viewModel: FundingViewModel = mock()
            whenever(provider.provide(any())).thenReturn(viewModel)
            whenever(viewModel.transactionData).thenReturn(mock())
            return provider
        }
    }


}