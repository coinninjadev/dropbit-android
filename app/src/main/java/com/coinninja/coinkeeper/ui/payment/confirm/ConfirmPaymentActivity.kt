package com.coinninja.coinkeeper.ui.payment.confirm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import app.dropbit.commons.currency.toBTCCurrency
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.android.helpers.styleAsBitcoin
import com.coinninja.android.helpers.styleAsLightning
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.FeesManager
import com.coinninja.coinkeeper.util.FeesManager.FeeType
import com.coinninja.coinkeeper.util.image.TwitterCircleTransform
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.coinninja.coinkeeper.view.subviews.SharedMemoView
import com.coinninja.coinkeeper.view.widget.AccountModeToggleButton
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.squareup.picasso.Picasso
import javax.inject.Inject

class ConfirmPaymentActivity : BaseActivity() {

    lateinit var paymentHolder: PaymentHolder
    val accountModeToggleButton: AccountModeToggleButton get() = findViewById(R.id.account_mode_toggle)
    val closeButton: View get() = findViewById(R.id.close_button)
    val sharedMemoViewGroup: View get() = findViewById(R.id.shared_transaction)
    val amountView: DefaultCurrencyDisplayView get() = findViewById(R.id.default_currency_view)
    val confirmHoldButton: ConfirmHoldButton get() = findViewById(R.id.confirm_pay_hold_progress_btn)
    val networkFeeView: TextView get() = findViewById(R.id.network_fee)
    val adjustableFeesVisibilityGroup: View get() = findViewById(R.id.adjustable_fee_group)
    val adjustableFeesTabs: TabLayout get() = findViewById(R.id.adjustable_fees)
    val estimatedDeliveryTime: TextView get() = findViewById(R.id.estimated_delivery_time)
    val nameField: TextView get() = findViewById(R.id.confirm_pay_name)
    val addressField: TextView get() = findViewById(R.id.confirm_pay_btc_address)
    val avatar: ImageView get() = findViewById(R.id.avatar_image_view)

    @Inject
    internal lateinit var picasso: Picasso

    @Inject
    internal lateinit var circleTransform: TwitterCircleTransform

    @Inject
    internal lateinit var sharedMemoView: SharedMemoView

    @Inject
    internal lateinit var feesManager: FeesManager

    @Inject
    lateinit var fundingViewModelProvider: FundingViewModelProvider

    lateinit var fundingViewModel: FundingViewModel

    lateinit var feePreference: FeeType
    var stagedFeePreference: FeeType = FeeType.FAST

    val transactionDataChangeObserver: Observer<TransactionData> = Observer {
        if (it.amount > 0) {
            feePreference = stagedFeePreference
            paymentHolder.transactionData = it
            paymentHolder.updateValue(it.amount.toBTCCurrency())
            renderAmount()
        } else {
            showFeeMakesInsufficentFunds()
        }
        resetFundingModel()
        renderWaitTime()
        renderFee()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_payment)
        paymentDataFromCreation()
        fundingViewModel = fundingViewModelProvider.provide(this)
        feePreference = feesManager.feePreference
        closeButton.setOnClickListener { activityNavigationUtil.navigateToHome(this) }
        sharedMemoView.render(sharedMemoViewGroup, paymentHolder.isSharingMemo, paymentHolder.memo, paymentHolder.toUser?.displayName)
        accountModeToggleButton.active = false
        adjustableFeesTabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> fundWithNewFee(FeeType.FAST)
                    1 -> fundWithNewFee(FeeType.SLOW)
                    2 -> fundWithNewFee(FeeType.CHEAP)
                    else -> {
                    }
                }
            }

        })
        renderReciepiant()
        confirmHoldButton.setOnConfirmHoldEndListener {
            startActivityForResult(Intent(this, AuthorizedActionActivity::class.java), authRequestCode)
        }
    }


    override fun onAccountModeChanged(mode: AccountMode) {
        super.onAccountModeChanged(mode)
        accountModeToggleButton.mode = mode
        when (mode) {
            AccountMode.LIGHTNING -> renderAsLightning()
            AccountMode.BLOCKCHAIN -> renderAsBlockchain()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == authRequestCode
                    && resultCode == AuthorizedActionActivity.RESULT_AUTHORIZED -> {
                startBroadcast()
            }
            requestCode == authRequestCode
                    && resultCode == Activity.RESULT_CANCELED -> {
            }

            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        onAccountModeChanged(accountModeToggleButton.mode)
        fundingViewModel.transactionData.observe(this, transactionDataChangeObserver)
    }

    override fun onPause() {
        super.onPause()
        fundingViewModel.transactionData.removeObserver(transactionDataChangeObserver)
    }


    private fun startBroadcast() {
        if (shouldSendInvite()) {
            sendInvite()
        } else {
            broadcastTransaction()
        }
    }

    private fun shouldSendInvite(): Boolean {
        return paymentHolder.toUser != null && !paymentHolder.hasPaymentAddress()
    }

    private fun sendInvite() {
        paymentHolder.toUser?.let { toUser ->
            activityNavigationUtil.navigateToInviteSendScreen(this, PendingInviteDTO(
                    toUser,
                    paymentHolder.evaluationCurrency.toLong(),
                    paymentHolder.transactionData.amount,
                    paymentHolder.transactionData.feeAmount,
                    paymentHolder.memo,
                    paymentHolder.isSharingMemo
            ))
        }
        finish()
    }

    private fun broadcastTransaction() {
        activityNavigationUtil.navigateToBroadcast(this, BroadcastTransactionDTO(
                paymentHolder.transactionData,
                paymentHolder.isSharingMemo,
                paymentHolder.memo,
                paymentHolder.toUser,
                paymentHolder.publicKey
        ))
        finish()
    }

    private fun renderAsBlockchain() {
        confirmHoldButton.styleAsBitcoin()
        amountView.accountMode(AccountMode.BLOCKCHAIN)
        renderAmount()
        renderFee()
        if (feesManager.isAdjustableFeesEnabled) {
            adjustableFeesVisibilityGroup.show()
            renderAdjustableFees()
        } else {
            adjustableFeesVisibilityGroup.gone()
        }
    }

    private fun renderAsLightning() {
        confirmHoldButton.styleAsLightning()
        amountView.accountMode(AccountMode.LIGHTNING)
        renderAmount()
        networkFeeView.gone()
        adjustableFeesVisibilityGroup.gone()

    }

    private fun renderAmount() {
        amountView.renderValues(
                DefaultCurrencies(USDCurrency(), BTCCurrency()),
                paymentHolder.cryptoCurrency,
                paymentHolder.fiat
        )
    }

    private fun renderAdjustableFees() {
        val feesTabs = adjustableFeesTabs
        when (feePreference) {
            FeeType.FAST -> feesTabs.selectTab(feesTabs.getTabAt(0))
            FeeType.SLOW -> feesTabs.selectTab(feesTabs.getTabAt(1))
            else -> feesTabs.selectTab(feesTabs.getTabAt(2))
        }
        renderWaitTime()
    }

    private fun renderWaitTime() {
        estimatedDeliveryTime.setText(when (feePreference) {
            FeeType.FAST -> R.string.approx_ten_minutes
            FeeType.SLOW -> R.string.approx_hour_wait
            else -> R.string.approx_day_wait
        })
    }

    private fun fundWithNewFee(feeType: FeeType) {
        stagedFeePreference = feeType
        if (paymentHolder.isSendingMax)
            fundingViewModel.fundMax(paymentHolder.paymentAddress, feesManager.fee(stagedFeePreference))
        else
            fundingViewModel.fundTransaction(
                    paymentHolder.paymentAddress,
                    paymentHolder.transactionData.amount,
                    feesManager.fee(stagedFeePreference)
            )
    }

    private fun renderFee() {
        val feeAmount = paymentHolder.transactionData.feeAmount.toBTCCurrency()
        networkFeeView.apply {
            text = getString(
                    R.string.confirm_pay_fee,
                    feeAmount.toFormattedString(),
                    feeAmount.toUSD(paymentHolder.evaluationCurrency).toFormattedCurrency()
            )
            show()
        }
    }

    private fun renderReciepiant() {
        avatar.gone()
        paymentHolder.toUser?.let { identity ->
            nameField.text = if (identity.displayName.isNullOrEmpty()) identity.secondaryDisplayName else identity.displayName
                    ?: ""

            if (identity.identityType == IdentityType.TWITTER) {
                avatar.also {
                    it.tag = identity.avatarUrl
                    picasso.load(identity.avatarUrl).transform(circleTransform).into(it)
                    it.show()
                    nameField.text = identity.secondaryDisplayName
                }
            }
        }
        addressField.text = paymentHolder.paymentAddress
    }

    private fun paymentDataFromCreation() {
        if (intent.hasExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER)) {
            paymentHolder = intent.getParcelableExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER)
        } else {
            Toast.makeText(this, "Missing Payment Data", Toast.LENGTH_LONG).show()
            activityNavigationUtil.navigateToHome(this)
            finish()
        }
    }

    private fun showFeeMakesInsufficentFunds() {
        GenericAlertDialog.newInstance(
                getString(R.string.fee_too_high_error)
        ).show(supportFragmentManager, errorDialogTag)
    }

    private fun resetFundingModel() {
        fundingViewModel.transactionData.removeObserver(transactionDataChangeObserver)
        fundingViewModel.clear()
        fundingViewModel.transactionData.observe(this, transactionDataChangeObserver)
    }

    companion object {
        const val errorDialogTag: String = "ERROR_DIALOG"
        internal const val authRequestCode = 10
    }
}
