package app.coinninja.cn.persistance.model

enum class LedgerType constructor(val id: Int) {
    BTC(0),
    LIGHTNING(1);

    companion object {
        fun from(id: Int): LedgerType? = when (id) {
            0 -> BTC
            else -> LIGHTNING
        }

        fun from(type: String): LedgerType? = when (type) {
            "btc" -> BTC
            "lightning" -> LIGHTNING
            else -> null
        }
    }

}
