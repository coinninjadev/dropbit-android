package app.coinninja.cn.persistance.model

enum class OutPutState constructor(val id: Int) {
    PENDING(0),
    ACKNOWLEDGE(1),
    CANCELED(2);

    companion object {
        fun from(id: Int?): OutPutState? = when (id) {
            0 -> PENDING
            1 -> ACKNOWLEDGE
            2 -> CANCELED
            else -> null
        }
    }
}
