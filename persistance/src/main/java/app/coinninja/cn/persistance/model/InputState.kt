package app.coinninja.cn.persistance.model

enum class InputState constructor(val id: Int) {
    PENDING(0),
    ACKNOWLEDGE(1),
    CANCELED(2);

    companion object {
        fun from(id: Int?): InputState? = when (id) {
            0 -> PENDING
            1 -> ACKNOWLEDGE
            2 -> CANCELED
            else -> null
        }
    }
}
