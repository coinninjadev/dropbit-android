package app.coinninja.cn.persistance.model

enum class AccountStatus constructor(val id: Int) {
    UNVERIFIED(0),
    PENDING_VERIFICATION(10),
    VERIFIED(100);

    companion object {
        fun from(id: Int?): AccountStatus? = when (id) {
            0 -> UNVERIFIED
            10 -> PENDING_VERIFICATION
            100 -> VERIFIED
            else -> null
        }

        fun from(status: String?): AccountStatus {
            if (status == null) return UNVERIFIED

            when (status.toLowerCase()) {
                "verified" -> return VERIFIED
                "pending-verification" -> return PENDING_VERIFICATION
            }
            return UNVERIFIED
        }

        fun asString(status: AccountStatus): String {
            when (status) {
                VERIFIED -> return "verified"
                PENDING_VERIFICATION -> return "pending-verification"
                else -> return "unverified"
            }
        }
    }
}
