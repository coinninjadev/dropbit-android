package app.coinninja.cn.thunderdome.model

import app.coinninja.cn.persistance.model.LedgerDirection
import app.coinninja.cn.persistance.model.LedgerStatus
import app.coinninja.cn.persistance.model.LedgerType
import app.coinninja.cn.persistance.model.LightningInvoice
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.util.asDateOrNull
import com.google.gson.annotations.SerializedName

@Mockable
data class LedgerInvoice(
        val id: String = "",

        @SerializedName("account_id")
        val accountId: String = "",

        @SerializedName("created_at")
        val createdAt: String = "",

        @SerializedName("updated_at")
        val updatedAt: String = "",

        @SerializedName("expires_at")
        val expiresAt: String? = null,
        val status: String = "",
        val type: String = "",
        val direction: String = "",
        val generated: Boolean = false,
        val value: Long = 0,

        @SerializedName("network_fee")
        val networkFee: Long = 0,

        @SerializedName("processing_fee")
        val processingFee: Long = 0,

        @SerializedName("add_index")
        val addIndex: Int? = null,

        val memo: String? = null,
        val request: String? = null,
        val error: String? = null,
        val hidden: Boolean? = null
) {

    val networkFeeCurrency: BTCCurrency get() = BTCCurrency(networkFee)
    val processingFeeCurrency: BTCCurrency get() = BTCCurrency(processingFee)

    fun toLightningLedger(): LightningInvoice {
        return LightningInvoice(
                serverId = id,
                createdAt = createdAt.asDateOrNull(),
                updatedAt = updatedAt.asDateOrNull(),
                expiresAt = expiresAt?.asDateOrNull(),
                status = LedgerStatus.from(status),
                type = LedgerType.from(type),
                direction = LedgerDirection.from(direction),
                isGenerated = generated,
                value = BTCCurrency(value),
                networkFee = BTCCurrency(networkFee),
                processingFee = BTCCurrency(processingFee),
                addIndex = addIndex,
                memo = memo ?: "",
                request = request ?: "",
                error = error ?: "",
                isHidden = hidden ?: false
        )
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LedgerInvoice

        if (id != other.id) return false
        if (accountId != other.accountId) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (expiresAt != other.expiresAt) return false
        if (status != other.status) return false
        if (type != other.type) return false
        if (direction != other.direction) return false
        if (generated != other.generated) return false
        if (value.toLong() != other.value.toLong()) return false
        if (networkFee.toLong() != other.networkFee.toLong()) return false
        if (processingFee.toLong() != other.processingFee.toLong()) return false
        if (addIndex != other.addIndex) return false
        if (memo != other.memo) return false
        if (request != other.request) return false
        if (error != other.error) return false
        if (hidden != other.hidden) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + accountId.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + (expiresAt?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + generated.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + networkFee.hashCode()
        result = 31 * result + processingFee.hashCode()
        result = 31 * result + (addIndex ?: 0)
        result = 31 * result + memo.hashCode()
        result = 31 * result + request.hashCode()
        result = 31 * result + error.hashCode()
        result = 31 * result + hidden.hashCode()
        return result
    }


}