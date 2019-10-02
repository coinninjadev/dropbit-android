package com.coinninja.coinkeeper.ui.payment.confirm

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import app.dropbit.commons.currency.toBTCCurrency
import com.coinninja.android.helpers.gone
import com.coinninja.android.helpers.show
import com.coinninja.android.helpers.styleAsBitcoin
import com.coinninja.android.helpers.styleAsLightning
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.FeesManager
import com.coinninja.coinkeeper.util.FeesManager.FeeType
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton
import com.coinninja.coinkeeper.view.subviews.SharedMemoView
import com.coinninja.coinkeeper.view.widget.AccountModeToggleButton
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
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

    @Inject
    internal lateinit var sharedMemoView: SharedMemoView

    @Inject
    internal lateinit var feesManager: FeesManager

    lateinit var feePreference: FeeType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_payment)
        paymentDataFromCreation()
        feePreference = feesManager.feePreference
        closeButton.setOnClickListener { activityNavigationUtil.navigateToHome(this) }
        sharedMemoView.render(sharedMemoViewGroup, paymentHolder.isSharingMemo, paymentHolder.memo, paymentHolder.toUser?.displayName)
        accountModeToggleButton.active = false
    }

    override fun onAccountModeChanged(mode: AccountMode) {
        super.onAccountModeChanged(mode)
        accountModeToggleButton.mode = mode
        when (mode) {
            AccountMode.LIGHTNING -> renderAsLightning()
            AccountMode.BLOCKCHAIN -> renderAsBlockchain()
        }
    }

    override fun onResume() {
        super.onResume()
        onAccountModeChanged(accountModeToggleButton.mode)
    }

    private fun renderAsBlockchain() {
        confirmHoldButton.styleAsBitcoin()
        amountView.apply {
            accountMode(AccountMode.BLOCKCHAIN)
            renderValues(
                    DefaultCurrencies(USDCurrency(), BTCCurrency()),
                    paymentHolder.cryptoCurrency,
                    paymentHolder.fiat
            )
        }
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
        amountView.apply {
            accountMode(AccountMode.LIGHTNING)
            renderValues(
                    DefaultCurrencies(USDCurrency(), BTCCurrency()),
                    paymentHolder.cryptoCurrency,
                    paymentHolder.fiat
            )
        }
        networkFeeView.gone()
        adjustableFeesVisibilityGroup.gone()

    }

    private fun renderAdjustableFees() {
        val feesTabs = adjustableFeesTabs
        when (feePreference) {
            FeeType.FAST -> feesTabs.selectTab(feesTabs.getTabAt(0))
            FeeType.SLOW -> feesTabs.selectTab(feesTabs.getTabAt(1))
            else -> feesTabs.selectTab(feesTabs.getTabAt(2))
        }
        feesTabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

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
        renderWaitTime()
    }

    private fun renderWaitTime() {
        estimatedDeliveryTime.setText(when (feePreference) {
            FeeType.FAST -> R.string.approx_ten_minutes
            FeeType.SLOW -> R.string.approx_hour_wait
            else -> R.string.day
        })
    }

    private fun fundWithNewFee(feeType: FeeType) {
        feePreference = feeType
/*
        val transactionData: TransactionData
        transactionData = if (paymentUtil.isSendingMax) {
            transactionFundingManager.buildFundedTransactionData(
                    paymentUtil.getAddress(),
                    feesManager.fee(feeType)
            )
        } else {
            transactionFundingManager.buildFundedTransactionData(
                    paymentUtil.getAddress(),
                    feesManager.fee(feeType),
                    getPaymentHolder().transactionData.amount, null
            )
        }
        if (transactionData.amount > 0) {
            feePref = feeType
            getPaymentHolder().transactionData = transactionData
        } else {
            AlertDialogBuilder.build(getContext(), getString(R.string.fee_too_high_error)).show()
        }
*/
        renderWaitTime()
        renderFee()
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

    private fun paymentDataFromCreation() {
        if (intent.hasExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER)) {
            paymentHolder = intent.getParcelableExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER)
        } else {
            Toast.makeText(this, "Missing Payment Data", Toast.LENGTH_LONG).show()
            activityNavigationUtil.navigateToHome(this)
            finish()
        }
    }
}
