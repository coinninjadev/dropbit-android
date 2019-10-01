package com.coinninja.coinkeeper.model

import android.os.Parcel
import android.os.Parcelable
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.thunderdome.model.RequestInvoice
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.*
import com.coinninja.coinkeeper.util.DefaultCurrencies

@Mockable
class PaymentHolder(
        evaluationCurrency: FiatCurrency = USDCurrency(0),
        var spendableBalance: BTCCurrency = BTCCurrency(0),
        var isSharingMemo: Boolean = true,
        var publicKey: String = "",
        var memo: String = "",
        var defaultCurrencies: DefaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency()),
        var toUser: Identity? = null
) : Parcelable {


    var requestInvoice: RequestInvoice? = null
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

    @Suppress("LeakingThis")
    constructor(parcel: Parcel) : this(
            USDCurrency(parcel.readLong()),
            BTCCurrency(parcel.readLong()),
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readParcelable(DefaultCurrencies::class.java.classLoader)
                    ?: DefaultCurrencies(USDCurrency(), BTCCurrency())
    ) {
        transactionData = parcel.readParcelable(TransactionData::class.java.classLoader)
                ?: TransactionData()
        requestInvoice = parcel.readParcelable(RequestInvoice::class.java.classLoader)
        toUser = parcel.readParcelable(Identity::class.java.classLoader) ?: null
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
        requestInvoice = null
        updateValue(primaryCurrency)
    }

    fun setMaxLimitForFiat() {
        USDCurrency.setMaxLimit(evaluationCurrency as USDCurrency?)
    }


    fun clearTransactionData() {
        transactionData = TransactionData(
                emptyArray(), 0, 0, 0,
                DerivationPath(49, 0, 0, 0, 0),
                ""
        )
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(evaluationCurrency.toLong())
        parcel.writeParcelable(spendableBalance, flags)
        parcel.writeByte(if (isSharingMemo) 1 else 0)
        parcel.writeString(publicKey)
        parcel.writeString(memo)
        parcel.writeParcelable(defaultCurrencies, flags)
        parcel.writeParcelable(transactionData, flags)
        parcel.writeParcelable(requestInvoice, flags)
        parcel.writeParcelable(toUser, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PaymentHolder

        if (spendableBalance != other.spendableBalance) return false
        if (isSharingMemo != other.isSharingMemo) return false
        if (publicKey != other.publicKey) return false
        if (memo != other.memo) return false
        if (defaultCurrencies != other.defaultCurrencies) return false
        if (toUser != other.toUser) return false
        if (requestInvoice != other.requestInvoice) return false
        if (evaluationCurrency != other.evaluationCurrency) return false
        if (transactionData != other.transactionData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = spendableBalance.hashCode()
        result = 31 * result + isSharingMemo.hashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + memo.hashCode()
        result = 31 * result + defaultCurrencies.hashCode()
        result = 31 * result + (toUser?.hashCode() ?: 0)
        result = 31 * result + (requestInvoice?.hashCode() ?: 0)
        result = 31 * result + evaluationCurrency.hashCode()
        result = 31 * result + transactionData.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<PaymentHolder> {
        override fun createFromParcel(parcel: Parcel): PaymentHolder {
            return PaymentHolder(parcel)
        }

        override fun newArray(size: Int): Array<PaymentHolder?> {
            return arrayOfNulls(size)
        }
    }
}
