package app.coinninja.cn.persistance.model

enum class IdentityType constructor(val id: Int) {
    UNKNOWN(-1),
    PHONE(0),
    TWITTER(1);

    fun asString(): String {
        when (this) {
            PHONE -> return "phone"
            TWITTER -> return "twitter"
            else -> return "unknown"
        }
    }

    companion object {
        fun from(id: Int?): IdentityType? = when (id) {
            0 -> PHONE
            1 -> TWITTER
            else -> UNKNOWN
        }

        fun from(type: String?): IdentityType {
            if (type == null) return UNKNOWN

            when (type.toLowerCase()) {
                "phone" -> return PHONE
                "twitter" -> return TWITTER
                else -> return UNKNOWN
            }

        }
    }
}