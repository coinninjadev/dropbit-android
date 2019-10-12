package com.coinninja.coinkeeper.ui.payment.request

import android.os.Parcel
import android.os.Parcelable
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency

@Mockable
data class LndInvoiceRequest(
        val request: String,
        val btcCurrency: BTCCurrency,
        val memo: String) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            BTCCurrency(parcel.readLong()),
            parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(request)
        parcel.writeLong(btcCurrency.toLong())
        parcel.writeString(memo)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LndInvoiceRequest

        if (request != other.request) return false
        if (btcCurrency.toLong() != other.btcCurrency.toLong()) return false
        if (memo != other.memo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = request.hashCode()
        result = 31 * result + btcCurrency.hashCode()
        result = 31 * result + memo.hashCode()
        return result
    }


    companion object CREATOR : Parcelable.Creator<LndInvoiceRequest> {
        override fun createFromParcel(parcel: Parcel): LndInvoiceRequest {
            return LndInvoiceRequest(parcel)
        }

        override fun newArray(size: Int): Array<LndInvoiceRequest?> {
            return arrayOfNulls(size)
        }
    }


}
