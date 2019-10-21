package com.coinninja.coinkeeper.ui.payment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import app.dropbit.commons.currency.*
import com.coinninja.android.helpers.Input
import com.coinninja.android.helpers.Views.clearCompoundDrawablesOn
import com.coinninja.android.helpers.Views.renderBTCIconOnCurrencyViewPair
import com.coinninja.android.helpers.shakeInError
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.text.CurrencyFormattingTextWatcher
import com.coinninja.coinkeeper.util.DefaultCurrencies

class PaymentInputView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context,
        attrs,
        defStyleAttr
), CurrencyFormattingTextWatcher.Callback {

    var canSendMax = true
        set(value) {
            field = value
            invalidate()

        }

    var canToggleCurrencies = true
        set(value) {
            field = value
            invalidate()
        }

    var paymentHolder: PaymentHolder = PaymentHolder().also {
        it.defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
    }
        set(value) {
            field = value
            onPaymentHolderChanged()
        }

    internal val sendMax: Button
    val secondaryCurrency: TextView
    val primaryCurrency: EditText

    private var isSendingMax = false
    private var watcher: CurrencyFormattingTextWatcher = CurrencyFormattingTextWatcher()
    private var onSendMaxObserver: OnSendMaxObserver? = null
    private var sendMaxClearedObserver: OnSendMaxClearedObserver? = null
    var onZeroedObserver: OnZeroedObserver? = null
    var onValidEntryObserver: OnValidEntryObserver? = null
    private var defaultSecondaryFontColor: Int = 0
    private var cryptoFontColor: Int = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.merge_component_payment_input_view, this, true)
        sendMax = findViewById(R.id.send_max)
        secondaryCurrency = findViewById(R.id.secondary_currency)
        primaryCurrency = findViewById(R.id.primary_currency)
        defaultSecondaryFontColor = secondaryCurrency?.currentTextColor
                ?: context.resources.getColor(R.color.font_gray)

        cryptoFontColor = ResourcesCompat.getColor(resources, R.color.bitcoin_orange, context.theme)
        secondaryCurrency.visibility = View.GONE
        watcher.setCallback(this)
        primaryCurrency.addTextChangedListener(watcher)
        setOnClickListener { focusOnPrimary() }
        if (canSendMax)
            sendMax?.setOnClickListener { onSendMax() }
    }

    fun setOnSendMaxObserver(onSendMaxObserver: OnSendMaxObserver) {
        this.onSendMaxObserver = onSendMaxObserver
    }

    override fun invalidate() {
        super.invalidate()
        if (!canSendMax)
            sendMax.visibility = View.GONE
    }

    override fun onValid(currency: Currency) {
        try {
            paymentHolder.updateValue(currency)

            if (hasEvaluationCurrency())
                updateSecondaryCurrencyWith(paymentHolder.secondaryCurrency)
            onValidEntryObserver?.onValidEntry()
        } catch (e: FormatNotValidException) {
            val text = primaryCurrency.text.toString()
            onInvalid(text)
            primaryCurrency.setText(text.substring(0 until text.length - 1))
        }
    }

    override fun onInvalid(text: String) {
        primaryCurrency.shakeInError()
    }

    override fun onZeroed() {
        if (canSendMax)
            sendMax.visibility = View.VISIBLE

        clearSendMaxIfNecessary()
        onZeroedObserver?.onZeroed()
    }

    override fun onInput() {
        sendMax.visibility = View.GONE
        clearSendMaxIfNecessary()
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return if (direction == View.FOCUS_DOWN) {
            requestFocusIfZero()
        } else false
    }

    fun setOnSendMaxClearedObserver(sendMaxClearedObserver: OnSendMaxClearedObserver) {
        this.sendMaxClearedObserver = sendMaxClearedObserver
    }

    private fun clearSendMaxIfNecessary() {
        if (isSendingMax) {
            notifyOfClearedSendMax()
            isSendingMax = false
        }
    }

    private fun onSendMax() {
        if (onSendMaxObserver != null) {
            isSendingMax = true
            sendMax.visibility = View.GONE
            onSendMaxObserver?.onSendMax()
        }
    }

    private fun notifyOfClearedSendMax() {
        if (sendMaxClearedObserver != null && isSendingMax) {
            sendMaxClearedObserver?.onSendMaxCleared()
            isSendingMax = false
        }
    }

    private fun requestFocusIfZero(): Boolean {
        var focusRequested = false
        if (paymentHolder.primaryCurrency.toLong() > 0) {
            primaryCurrency.clearFocus()
        } else {
            focusRequested = focusOnPrimary()
        }

        return focusRequested
    }


    private fun togglePrimaryCurrencies() {
        paymentHolder.toggleCurrencies()
        onPaymentHolderChanged()
    }

    private fun focusOnPrimary(): Boolean {
        primaryCurrency.let {
            it.postDelayed({ Input.showKeyboard(it) }, 100)
            val requestedFocus = it.requestFocus()
            if (requestedFocus) {
                it.setSelection(it.text.length)
            }
        }
        return true
    }

    private fun onPaymentHolderChanged() {
        watcher.currency = paymentHolder.primaryCurrency

        setPrimaryCurrencyWithoutNotifying()

        if (hasEvaluationCurrency()) {
            updateSecondaryCurrencyWith(paymentHolder.secondaryCurrency)
            configureToggleCurrencyButton()
        }
        invalidateSymbol()
    }

    private fun setPrimaryCurrencyWithoutNotifying() {
        primaryCurrency.removeTextChangedListener(watcher)
        updatePrimaryCurrencyWith(paymentHolder.primaryCurrency)
        if (!paymentHolder.primaryCurrency.isZero) {
            sendMax.visibility = View.GONE
        }
        primaryCurrency.addTextChangedListener(watcher)
    }

    private fun updatePrimaryCurrencyWith(currency: Currency) {
        if (currency.isCrypto) {
            currency.currencyFormat = CryptoCurrency.NO_SYMBOL_FORMAT
        }

        if (currency.isZero) {
            if (currency.isCrypto) {
                primaryCurrency.setText(currency.toFormattedCurrency())
            } else {
                primaryCurrency.setText(String.format("%s0", currency.symbol))
            }
            onZeroed()
        } else {
            primaryCurrency.setText(currency.toFormattedCurrency())
        }
        primaryCurrency.setSelection((primaryCurrency.text ?: "").length)
    }

    @SuppressLint("SetTextI18n")
    private fun updateSecondaryCurrencyWith(value: Currency) {
        if (value.isCrypto) {
            if (value is BTCCurrency)
                value.currencyFormat = CryptoCurrency.NO_SYMBOL_FORMAT

            secondaryCurrency.setTextColor(cryptoFontColor)
        } else {
            secondaryCurrency.setTextColor(defaultSecondaryFontColor)
        }

        secondaryCurrency.visibility = View.VISIBLE
        secondaryCurrency.text = value.toFormattedCurrency()

    }

    private fun configureToggleCurrencyButton() {
        val toggleView = findViewById<View>(R.id.primary_currency_toggle)
        if (canToggleCurrencies) {
            toggleView.setOnClickListener { togglePrimaryCurrencies() }
            toggleView.visibility = View.VISIBLE
        } else {
            toggleView.visibility = View.GONE
        }
    }

    private fun invalidateSymbol() {
        clearCompoundDrawablesOn(primaryCurrency)
        clearCompoundDrawablesOn(secondaryCurrency)

        if (paymentHolder.crypto is BTCCurrency) {
            renderBTCIconOnCurrencyViewPair(context, paymentHolder.defaultCurrencies,
                    primaryCurrency, PRIMARY_SCALE, secondaryCurrency, SECONDARY_SCALE)

            if (!paymentHolder.primaryCurrency.isCrypto && !hasEvaluationCurrency()) {
                clearCompoundDrawablesOn(secondaryCurrency)
            }
        }
    }

    private fun hasEvaluationCurrency(): Boolean {
        return paymentHolder.evaluationCurrency.toLong() > 0
    }


    interface OnSendMaxObserver {
        fun onSendMax()
    }

    interface OnSendMaxClearedObserver {
        fun onSendMaxCleared()
    }

    interface OnValidEntryObserver {
        fun onValidEntry()
    }

    interface OnZeroedObserver {
        fun onZeroed()
    }

    companion object {

        const val SECONDARY_SCALE = .8
        const val PRIMARY_SCALE = 1.0
    }
}
