package app.coinninja.cn.persistance.model

enum class SendType constructor(val id: Int) {
    BLOCKCHAIN_SENT(0),
    BLOCKCHAIN_RECEIVED(10),
    LIGHTNING_SENT(20),
    LIGHTNING_RECEIVED(30);

    companion object {
        fun from(id: Int?): SendType? = when (id) {
            0 -> BLOCKCHAIN_SENT
            10 -> BLOCKCHAIN_RECEIVED
            20 -> LIGHTNING_SENT
            30 -> LIGHTNING_RECEIVED
            else -> null
        }
    }
}