package com.coinninja.coinkeeper.model.db.enums

enum class AccountStatus constructor(val id: Int) {
    UNVERIFIED(0),
    PENDING_VERIFICATION(10),
    VERIFIED(100);

    companion object {

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
