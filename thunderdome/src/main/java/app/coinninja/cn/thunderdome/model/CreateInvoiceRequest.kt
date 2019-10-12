package app.coinninja.cn.thunderdome.model

import com.google.gson.annotations.SerializedName

data class CreateInvoiceRequest(
        @SerializedName("value")
        val amount: Long = 0,
        val memo: String = "",
        val expires: Long = 172800
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreateInvoiceRequest

        if (amount != other.amount) return false
        if (memo != other.memo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + (memo.hashCode())
        return result
    }
}
