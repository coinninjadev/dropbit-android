package app.coinninja.cn.persistance.model

enum class LedgerDirection(val id: Int) {
    IN(0), OUT(1);

    companion object {
        fun from(id: Int): LedgerDirection = when (id) {
            0 -> IN
            else -> OUT
        }

        fun from(direction: String): LedgerDirection? = when (direction) {
            "in" -> IN
            "out" -> OUT
            else -> null
        }
    }

}
