package com.coinninja.coinkeeper.model.db.enums;

public enum BTCState {
    UNFULFILLED(0),
    FULFILLED(1),
    CANCELED(2),
    EXPIRED(3),
    UNACKNOWLEDGED(4);

    private final int id;

    BTCState(int id) {
        this.id = id;
    }

    public static BTCState from(String status) {
        status = status.toLowerCase();
        if ("expired".equals(status))
            return EXPIRED;
        else if ("completed".equals(status))
            return FULFILLED;
        else if ("canceled".equals(status))
            return CANCELED;
        else if ("new".equals(status))
            return UNFULFILLED;
        else
            return UNFULFILLED;
    }

    public int getId() {
        return id;
    }
}
