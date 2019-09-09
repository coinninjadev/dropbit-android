package app.coinninja.cn.persistance.model

enum class BTCState constructor(val id: Int) {
    UNFULFILLED(0),
    FULFILLED(1),
    CANCELED(2),
    EXPIRED(3),
    UNACKNOWLEDGED(4);

    companion object {
        fun from(id: Int?): BTCState? = when (id) {
            0 -> UNFULFILLED
            1 -> FULFILLED
            2 -> CANCELED
            3 -> EXPIRED
            4 -> UNACKNOWLEDGED
            else -> null
        }
    }
}