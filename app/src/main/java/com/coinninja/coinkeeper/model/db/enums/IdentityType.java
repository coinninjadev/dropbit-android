package com.coinninja.coinkeeper.model.db.enums;

public enum IdentityType {
    UNKNOWN(-1),
    PHONE(0),
    TWITTER(1);
    private final int id;

    IdentityType(int id) {
        this.id = id;
    }

    public static IdentityType from(String type) {
        if (type == null) return UNKNOWN;

        switch (type.toLowerCase()) {
            case "phone":
                return PHONE;
            case "twitter":
                return TWITTER;
            default:
                return UNKNOWN;
        }

    }

    public int getId() {
        return id;
    }

    public String asString() {
        switch (this) {
            case PHONE:
                return "phone";
            case TWITTER:
                return "twitter";
            default:
                return "unknown";
        }
    }
}
