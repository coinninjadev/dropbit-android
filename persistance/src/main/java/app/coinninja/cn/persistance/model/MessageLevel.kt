package app.coinninja.cn.persistance.model

enum class MessageLevel constructor(val id: Int, val value: String) {
    INFO(0, "info"),
    SUCCESS(1, "success"),
    WARN(2, "warn"),
    ERROR(3, "error");

    companion object {
        fun from(id: Int?): MessageLevel? = when(id) {
            0 -> INFO
            1 -> SUCCESS
            2 -> WARN
            3 -> ERROR
            else -> null
        }
    }
}