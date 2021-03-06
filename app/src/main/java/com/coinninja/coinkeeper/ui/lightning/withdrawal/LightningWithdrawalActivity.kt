package com.coinninja.coinkeeper.ui.lightning.withdrawal

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.Observer
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.dropbit.commons.currency.*
import app.dropbit.commons.util.decimalFormat
import com.coinninja.android.helpers.showKeyboard
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.payment.PaymentInputView
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import javax.inject.Inject

class LightningWithdrawalActivity : BaseActivity() {

    @Inject
    lateinit var currencyPreference: CurrencyPreference

    @Inject
    lateinit var fundingViewModelProvider: FundingViewModelProvider

    lateinit var fundingViewModel: FundingViewModel

    internal val paymentHolder = PaymentHolder(defaultCurrencies = DefaultCurrencies(SatoshiCurrency(), USDCurrency()))

    internal var confirmed: Boolean = false
    internal var lightningBalance: CryptoCurrency = SatoshiCurrency(0)
    internal var dropBitFeeValue: SatoshiCurrency = SatoshiCurrency(0)
    internal var networkFeeValue: SatoshiCurrency = SatoshiCurrency(0)

    val onValidEntryObserver = object : PaymentInputView.OnValidEntryObserver {
        override fun onValidEntry() {
            val amount = paymentHolder.crypto.toLong()
            zeroFees()
            if (amount >= minWithdrawAmount) {
                fundingViewModel.fundLightningWithdrawal(amount)
            }
        }
    }

    internal val dropbitFeeObserver: Observer<CryptoCurrency> = Observer {
        dropBitFeeValue = it.toSats(paymentHolder.evaluationCurrency)
        dropbitFee.text = getString(
                R.string.lightning_fee_value, it.toLong().decimalFormat(),
                it.toUSD(paymentHolder.evaluationCurrency).toFormattedCurrency()
        )
    }

    internal val networkFeeObserver: Observer<CryptoCurrency> = Observer {
        networkFeeValue = it.toSats(paymentHolder.evaluationCurrency)
        networkFee.text = getString(
                R.string.lightning_fee_value,
                it.toLong().decimalFormat(),
                it.toUSD(paymentHolder.evaluationCurrency).toFormattedCurrency()
        )
    }

    internal val onConfirmed: ConfirmHoldButton.OnConfirmHoldEndListener =
            ConfirmHoldButton.OnConfirmHoldEndListener {
                processWithdrawal()
            }

    internal val closeButton: ImageButton get() = findViewById(R.id.close_button)
    internal val withdrawalAmount: PaymentInputView get() = findViewById(R.id.withdrawal_value)
    internal val confirmButton: ConfirmHoldButton get() = findViewById(R.id.confirm_button)
    internal val dropbitFee: TextView get() = findViewById(R.id.dropbit_fee_value)
    internal val networkFee: TextView get() = findViewById(R.id.network_fee_value)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lightning_withdrawal)
        fundingViewModel = fundingViewModelProvider.provide(this)
        closeButton.setOnClickListener { onBackPressed() }
        withdrawalAmount.postDelayed({ accountModeManager.changeMode(AccountMode.LIGHTNING) }, 300)
        withdrawalAmount.paymentHolder = paymentHolder
        withdrawalAmount.canSendMax = false
        withdrawalAmount.canToggleCurrencies = true
        withdrawalAmount.onValidEntryObserver = onValidEntryObserver
    }

    override fun onResume() {
        super.onResume()
        withdrawalAmount.showKeyboard()
        confirmButton.setOnConfirmHoldBeginListener {
            confirmed = false
        }
        confirmButton.setOnConfirmHoldEndListener(onConfirmed)
        zeroFees()
        fundingViewModel.lightningWithdrawalDropbitFee.observe(this, dropbitFeeObserver)
        fundingViewModel.lightningWithdrawalNetworkFee.observe(this, networkFeeObserver)
    }

    override fun onPause() {
        super.onPause()
        fundingViewModel.lightningWithdrawalDropbitFee.removeObserver(dropbitFeeObserver)
        fundingViewModel.lightningWithdrawalNetworkFee.removeObserver(networkFeeObserver)
    }

    override fun onLatestPriceChanged(currentPrice: FiatCurrency) {
        super.onLatestPriceChanged(currentPrice)
        if (paymentHolder.evaluationCurrency.isZero) {
            paymentHolder.evaluationCurrency = currentPrice
            withdrawalAmount.paymentHolder = paymentHolder
        } else {
            paymentHolder.evaluationCurrency = currentPrice
        }
    }

    override fun onLightningBalanceChanged(balance: CryptoCurrency) {
        super.onLightningBalanceChanged(balance)
        lightningBalance = balance.toSats(paymentHolder.evaluationCurrency)
    }

    internal fun processWithdrawal() {
        if (isValidWithdrawal) {
            activityNavigationUtil.showWithdrawalCompleted(this,
                    WithdrawalRequest(paymentHolder.crypto.toLong(), dropBitFeeValue.toLong(), networkFeeValue.toLong()))
        } else {
            confirmed = false
        }
    }

    private fun zeroFees() {
        dropbitFee.text = getText(R.string.na)
        networkFee.text = getText(R.string.na)
    }

    private val isValidWithdrawal: Boolean
        get() =
            if (paymentHolder.crypto.isZero) {
                showWithdrawingZeroMessage()
                false
            } else if (paymentHolder.crypto.toLong() < minWithdrawAmount) {
                showWithdrawingBelowMinimum()
                false
            } else if (!isFunded) {
                showNonSufficientFundsMessage()
                false
            } else {
                true
            }

    private val totalWithdrawalAmount: Currency get() = paymentHolder.crypto

    private val isFunded: Boolean
        get() {
            if (!lightningBalance.isZero && !dropBitFeeValue.isZero && !networkFeeValue.isZero && !paymentHolder.crypto.isZero) {
                return totalWithdrawalAmount.toLong() <= lightningBalance.toLong()
            }
            return false
        }

    private fun showNonSufficientFundsMessage() {
        GenericAlertDialog.newInstance(
                getString(
                        R.string.unload_lightning_insufficient_funds,
                        lightningBalance.toFormattedCurrency()

                )
        ).show(supportFragmentManager, "INVALID_WITHDRAWAL")
    }

    private fun showWithdrawingZeroMessage() {
        GenericAlertDialog.newInstance(
                getString(R.string.unload_minimum_amount_required)
        ).show(supportFragmentManager, "INVALID_WITHDRAWAL")
    }

    private fun showWithdrawingBelowMinimum() {
        GenericAlertDialog.newInstance(
                getString(
                        R.string.unload_minimum_amount_notification,
                        minWithdrawAmount.decimalFormat()
                )
        ).show(supportFragmentManager, "INVALID_WITHDRAWAL")
    }

    companion object {
        private val minWithdrawAmount: Long = 40_000
    }
}
