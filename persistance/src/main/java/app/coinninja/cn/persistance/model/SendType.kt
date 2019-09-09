package app.coinninja.cn.persistance.model

enum class SendType constructor(val id: Int) {
    SENT(0),
    RECEIVED(10);

    companion object {
        fun from(id: Int?): SendType? = when (id) {
            0 -> SENT
            10 -> RECEIVED
            else -> null
        }
    }
}