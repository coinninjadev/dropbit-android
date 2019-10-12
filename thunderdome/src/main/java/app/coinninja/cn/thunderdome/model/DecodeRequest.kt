package app.coinninja.cn.thunderdome.model

data class DecodeRequest(
        val request: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecodeRequest

        if (request != other.request) return false

        return true
    }

    override fun hashCode(): Int {
        return request.hashCode()
    }
}
