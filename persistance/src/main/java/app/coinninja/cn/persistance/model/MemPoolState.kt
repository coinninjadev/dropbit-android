package app.coinninja.cn.persistance.model

enum class MemPoolState constructor(val id: Int) {
    INIT(0),
    PENDING(1),
    ACKNOWLEDGE(2),
    MINED(3),
    FAILED_TO_BROADCAST(4),
    DOUBLE_SPEND(5),
    ORPHANED(6);


    companion object {
        fun from(id: Int?): MemPoolState? = when (id) {
            0 -> INIT
            1 -> PENDING
            2 -> ACKNOWLEDGE
            3 -> MINED
            4 -> FAILED_TO_BROADCAST
            5 -> DOUBLE_SPEND
            6 -> ORPHANED
            else -> null
        }
    }

}
