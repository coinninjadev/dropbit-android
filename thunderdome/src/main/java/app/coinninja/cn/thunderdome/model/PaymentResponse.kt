package app.coinninja.cn.thunderdome.model

import app.dropbit.annotations.Mockable

@Mockable
data class PaymentResponse(
        val result: LedgerInvoice
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PaymentResponse

        if (result != other.result) return false

        return true
    }

    override fun hashCode(): Int {
        return result.hashCode()
    }
}
