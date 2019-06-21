package com.coinninja.coinkeeper.model.db.enums;

import org.jetbrains.annotations.Nullable;

public enum AccountStatus {
    UNVERIFIED(0),
    PENDING_VERIFICATION(10),
    VERIFIED(100);

    public final int id;

    AccountStatus(int id) {
        this.id = id;
    }

    public static AccountStatus from(@Nullable String status) {
        if (status == null) return UNVERIFIED;

        switch (status.toLowerCase()) {
            case "verified":
                return VERIFIED;
            case "pending-verification":
                return PENDING_VERIFICATION;
        }
        return UNVERIFIED;
    }

    public static String asString(AccountStatus status) {
        switch (status) {
            case VERIFIED:
                return "verified";
            case PENDING_VERIFICATION:
                return "pending-verification";
            default:
                return "unverified";
        }
    }

    public int getId() {
        return id;
    }
}
