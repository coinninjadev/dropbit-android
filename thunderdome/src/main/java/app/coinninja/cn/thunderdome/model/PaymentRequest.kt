package app.coinninja.cn.thunderdome.model

data class PaymentRequest(
        val request: String,
        val value: Long,
        val estimate: Boolean? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PaymentRequest

        if (request != other.request) return false
        if (value != other.value) return false
        if (estimate != other.estimate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = request.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + (estimate?.hashCode() ?: 0)
        return result
    }
}
