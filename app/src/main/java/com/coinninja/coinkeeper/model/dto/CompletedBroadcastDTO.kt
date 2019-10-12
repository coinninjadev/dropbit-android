package com.coinninja.coinkeeper.model.dto

import android.os.Parcelable
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.Identity
import kotlinx.android.parcel.Parcelize

@Mockable
@Parcelize
data class CompletedBroadcastDTO(
        var transactionData: TransactionData,
        var isMemoShared: Boolean = false,
        var memo: String? = "",
        var identity: Identity? = null,
        var publicKey: String? = null,
        var transactionId: String? = null) : Parcelable {


    constructor(broadcastActivityDTO: BroadcastTransactionDTO, transactionId: String) : this(
            broadcastActivityDTO.transactionData,
            broadcastActivityDTO.isMemoShared,
            broadcastActivityDTO.memo,
            broadcastActivityDTO.identity,
            broadcastActivityDTO.publicKey,
            transactionId)

    constructor(transactionData: TransactionData, transactionId: String, identity: Identity) : this(
            transactionData, false, "", identity, null, transactionId
    )


    fun shouldShareMemo(): Boolean {
        return (identity != null && !memo.isNullOrEmpty() && isMemoShared && !publicKey.isNullOrEmpty())
    }

    fun hasMemo(): Boolean {
        return !memo.isNullOrEmpty()
    }

    fun hasPublicKey(): Boolean {
        return !publicKey.isNullOrEmpty()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompletedBroadcastDTO

        if (transactionData != other.transactionData) return false
        if (isMemoShared != other.isMemoShared) return false
        if (memo != other.memo) return false
        if (identity != other.identity) return false
        if (publicKey != other.publicKey) return false
        if (transactionId != other.transactionId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transactionData.hashCode()
        result = 31 * result + isMemoShared.hashCode()
        result = 31 * result + (memo?.hashCode() ?: 0)
        result = 31 * result + (identity?.hashCode() ?: 0)
        result = 31 * result + (publicKey?.hashCode() ?: 0)
        result = 31 * result + (transactionId?.hashCode() ?: 0)
        return result
    }
}
