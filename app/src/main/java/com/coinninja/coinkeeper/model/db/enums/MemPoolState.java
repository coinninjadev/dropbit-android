package com.coinninja.coinkeeper.model.db.enums;

public enum MemPoolState {
    INIT(0),
    PENDING(1),
    ACKNOWLEDGE(2),
    MINED(3),
    FAILED_TO_BROADCAST(4),
    DOUBLE_SPEND(5),
    ORPHANED(6);

    private final int id;

    MemPoolState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
