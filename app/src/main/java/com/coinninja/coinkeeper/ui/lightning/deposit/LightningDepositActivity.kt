package com.coinninja.coinkeeper.ui.lightning.deposit

import android.os.Bundle
import android.widget.ImageButton
import androidx.annotation.CallSuper
import androidx.lifecycle.Observer
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.dropbit.commons.currency.CryptoCurrency
import app.dropbit.commons.currency.FiatCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.android.helpers.showKeyboard
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.bitcoin.isFunded
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.payment.PaymentInputView
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import javax.inject.Inject

class LightningDepositActivity : BaseActivity() {

    @Inject
    lateinit var currencyPreference: CurrencyPreference

    @Inject
    lateinit var fundingViewModelProvider: FundingViewModelProvider

    lateinit var fundingViewModel: FundingViewModel

    internal val paymentHolder = PaymentHolder()

    internal var confirmed: Boolean = false
    internal var transactionData: TransactionData? = null
    internal var lightningBalance: CryptoCurrency? = null

    internal val transactionDataObserver: Observer<TransactionData> = Observer {
        transactionData = it
        if (isValidTransaction(it) && confirmed) {
            submitPaymentForBroadCast()
        } else {
            confirmed = false
        }
    }

    private fun isValidTransaction(transactionData: TransactionData): Boolean {
        val hasAmount = paymentHolder.fiat.toLong() > 0
        if (!hasAmount) {
            notifyOfMinimumDepositLimit()
            return hasAmount
        }

        val funded = transactionData.isFunded()
        if (!funded) {
            notifyOfNonSufficientFunds()
            return funded
        }
        return isValidPaymentAmount()
    }

    private fun isValidPaymentAmount(): Boolean {
        val toLow = depositAmountView.paymentHolder.fiat.toLong() < MIN_DEPOSIT_AMOUNT.toLong()
        if (toLow) {
            notifyOfToLittleToDeposit()
        }
        val balance = lightningBalance?.toFiat(paymentHolder.evaluationCurrency) ?: USDCurrency(0)
        val toHigh = depositAmountView.paymentHolder.fiat.toLong() + balance.toLong() > MAX_DEPOSIT_AMOUNT.toLong()
        if (toHigh) {
            notifyOfToMuchToDeposit()
        }
        return !toLow && !toHigh
    }


    internal val depositAmountView: PaymentInputView get() = findViewById(R.id.deposit_value)
    internal val close: ImageButton get() = findViewById(R.id.close_button)
    private val confirmButton: ConfirmHoldButton get() = findViewById(R.id.confirm_button)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lightning_deposit)
        fundingViewModel = fundingViewModelProvider.provide(this)
        depositAmountView.canSendMax = false
        paymentHolder.updateValue(USDCurrency(0))
    }

    override fun onResume() {
        super.onResume()
        fundingViewModel.transactionData.observe(this, transactionDataObserver)
        paymentHolder.defaultCurrencies = currencyPreference.currenciesPreference
        close.setOnClickListener { onBackPressed() }
        confirmButton.apply {
            isEnabled = true
            setOnConfirmHoldEndListener { onConfirmationCompleted() }
            setOnConfirmHoldBeginListener { onConfirmationStarted() }
        }

        depositAmountView.postDelayed(
                { accountModeManager.overrideBalanceWith(AccountMode.BLOCKCHAIN) }, 300
        )

        depositAmountView.paymentHolder = paymentHolder
    }

    override fun onLatestPriceChanged(currentPrice: FiatCurrency) {
        super.onLatestPriceChanged(currentPrice)
        paymentHolder.evaluationCurrency = currentPrice
        depositAmountView.paymentHolder = paymentHolder
        if (intent.hasExtra(DropbitIntents.EXTRA_AMOUNT)) {
            initWithAmount()
        } else {
            depositAmountView.showKeyboard()

        }
    }

    override fun onLightningBalanceChanged(balance: CryptoCurrency) {
        super.onLightningBalanceChanged(balance)
        lightningBalance = balance
    }

    override fun onPause() {
        fundingViewModel.transactionData.removeObserver(transactionDataObserver)
        accountModeManager.clearOverrides()
        super.onPause()
    }

    private fun initWithAmount() {
        intent.getParcelableExtra<USDCurrency>(DropbitIntents.EXTRA_AMOUNT)?.let { amount ->
            paymentHolder.updateValue(amount)
            fundingViewModel.fundLightningDeposit(paymentHolder.cryptoCurrency.toLong())
            intent.removeExtra(DropbitIntents.EXTRA_AMOUNT)
        }
    }

    private fun notifyOfMinimumDepositLimit() {
        GenericAlertDialog.newInstance(
                getString(
                        R.string.load_lightning_invalid_amount,
                        MIN_DEPOSIT_AMOUNT.toFormattedCurrency()
                )
        ).show(supportFragmentManager, "INVALID_DEPOSIT_DIALOG")
    }

    private fun notifyOfNonSufficientFunds() {
        GenericAlertDialog.newInstance(
                getString(
                        R.string.load_lightning_insufficient_funds,
                        paymentHolder.cryptoCurrency.toFormattedCurrency()
                )
        ).show(supportFragmentManager, "NON_SUFFICIENT_FUNDS_DIALOG")
    }

    private fun notifyOfToLittleToDeposit() {
        GenericAlertDialog.newInstance(
                getString(
                        R.string.load_lightning_under_min_deposit,
                        paymentHolder.fiat.toFormattedCurrency(),
                        MIN_DEPOSIT_AMOUNT.toFormattedCurrency()
                )
        ).show(supportFragmentManager, "INVALID_DEPOSIT_DIALOG")
    }

    private fun notifyOfToMuchToDeposit() {
        val balance = lightningBalance?.toFiat(paymentHolder.evaluationCurrency) ?: USDCurrency(0)
        GenericAlertDialog.newInstance(
                getString(
                        R.string.load_lightning_over_max_limit,
                        paymentHolder.fiat.toFormattedCurrency(),
                        balance.toFormattedCurrency(),
                        MAX_DEPOSIT_AMOUNT.toFormattedCurrency()
                )
        ).show(supportFragmentManager, "INVALID_DEPOSIT_DIALOG")
    }

    internal fun onConfirmationCompleted() {
        confirmed = true
        fundingViewModel.fundLightningDeposit(paymentHolder.cryptoCurrency.toLong())
    }

    private fun onConfirmationStarted() {
        confirmed = false
    }


    private fun submitPaymentForBroadCast() {
        transactionData?.let {
            activityNavigationUtil.navigateToBroadcast(this, BroadcastTransactionDTO(it))
            confirmed = false
        }
    }

    companion object {
        val MIN_DEPOSIT_AMOUNT = USDCurrency(5_00)
        val MAX_DEPOSIT_AMOUNT = USDCurrency(500_00)
    }
}

