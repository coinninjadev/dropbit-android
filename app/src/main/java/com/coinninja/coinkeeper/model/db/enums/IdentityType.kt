package com.coinninja.coinkeeper.model.db.enums

enum class IdentityType(val id: Int) {
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
