package com.coinninja.coinkeeper.model.dto

import android.os.Parcelable
import app.dropbit.annotations.Mockable
import com.coinninja.bindings.TransactionData
import com.coinninja.coinkeeper.model.Identity
import kotlinx.android.parcel.Parcelize

@Mockable
@Parcelize
data class BroadcastTransactionDTO(
        var transactionData: TransactionData,
        var isMemoShared: Boolean = false,
        var memo: String? = "",
        var identity: Identity? = null,
        var publicKey: String? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BroadcastTransactionDTO

        if (transactionData != other.transactionData) return false
        if (isMemoShared != other.isMemoShared) return false
        if (memo != other.memo) return false
        if (identity != other.identity) return false
        if (publicKey != other.publicKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transactionData.hashCode()
        result = 31 * result + isMemoShared.hashCode()
        result = 31 * result + (memo?.hashCode() ?: 0)
        result = 31 * result + (identity?.hashCode() ?: 0)
        result = 31 * result + (publicKey?.hashCode() ?: 0)
        return result
    }
}
