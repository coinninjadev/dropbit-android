package com.coinninja.coinkeeper.model

import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.*
import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.TransactionData
import com.coinninja.coinkeeper.util.DefaultCurrencies

@Mockable
class PaymentHolder(
        evaluationCurrency: FiatCurrency = USDCurrency(0),
        var spendableBalance: BTCCurrency = BTCCurrency(0),
        var isSharingMemo: Boolean = true,
        var publicKey: String = "",
        var memo: String = "",
        var defaultCurrencies: DefaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
) {

    var evaluationCurrency: FiatCurrency = evaluationCurrency
        set(value) {
            field = value
            updateValue(primaryCurrency)

        }

    var transactionData: TransactionData = TransactionData(
            emptyArray(), 0, 0, 0,
            DerivationPath(49, 0, 0, 0, 0),
            ""
    )
        set(value) {
            if (hasPaymentAddress() && value.paymentAddress.isNullOrEmpty())
                value.paymentAddress = field.paymentAddress
            field = value
        }

    val btcCurrency: BTCCurrency
        get() = cryptoCurrency as BTCCurrency

    val primaryCurrency: Currency
        get() = defaultCurrencies.primaryCurrency

    val secondaryCurrency: Currency
        get() = defaultCurrencies.secondaryCurrency

    val fiat: Currency
        get() = defaultCurrencies.fiat

    val cryptoCurrency: CryptoCurrency
        get() = defaultCurrencies.crypto

    var paymentAddress: String
        get() = transactionData.paymentAddress ?: ""
        set(paymentAddress) {
            transactionData.paymentAddress = paymentAddress
        }


    fun updateValue(currency: Currency): Currency {
        if (currency.isCrypto != primaryCurrency.isCrypto) {
            toggleCurrencies()
        }

        val formattedAmount = currency.toFormattedString()
        val primaryCurrency = defaultCurrencies.primaryCurrency
        primaryCurrency.update(formattedAmount)
        val secondaryCurrency: Currency

        if (primaryCurrency.isCrypto) {
            secondaryCurrency = primaryCurrency.toUSD(evaluationCurrency)
        } else {
            secondaryCurrency = primaryCurrency.toBTC(evaluationCurrency)
        }

        defaultCurrencies.secondaryCurrency.update(secondaryCurrency.toFormattedString())
        return secondaryCurrency
    }

    fun toggleCurrencies() {
        defaultCurrencies = DefaultCurrencies(defaultCurrencies.secondaryCurrency, defaultCurrencies.primaryCurrency)
    }

    fun hasPaymentAddress(): Boolean {
        return !transactionData.paymentAddress.isNullOrEmpty()
    }

    fun hasPubKey(): Boolean {
        return publicKey.isNotEmpty()
    }

    fun clearPayment() {
        publicKey = ""
        clearTransactionData()
        paymentAddress = ""
        primaryCurrency.zero()
    }

    fun setMaxLimitForFiat() {
        USDCurrency.setMaxLimit(evaluationCurrency as USDCurrency?)
    }

    private fun clearTransactionData() {
        transactionData = TransactionData(
                emptyArray(), 0, 0, 0,
                DerivationPath(49, 0, 0, 0, 0),
                ""
        )
    }
}
