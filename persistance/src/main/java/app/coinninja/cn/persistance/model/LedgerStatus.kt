package app.coinninja.cn.persistance.model

enum class LedgerStatus private constructor(val id: Int) {
    PENDING(1),
    COMPLETED(2),
    EXPIRED(3),
    FAILED(4);

    companion object {

        fun from(status: Int?): LedgerStatus? = status?.let {
            return when (status) {
                1 -> PENDING
                2 -> COMPLETED
                3 -> EXPIRED
                4 -> FAILED
                else -> null
            }
        }

        fun from(status: String?): LedgerStatus? {
            return when (status) {
                "completed" -> COMPLETED
                "expired" -> EXPIRED
                "failed" -> FAILED
                "pending" -> PENDING
                else -> null
            }
        }
    }
}
