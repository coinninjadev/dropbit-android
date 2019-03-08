package com.coinninja.coinkeeper.model.db.enums;

public enum Type {
    SENT(0),
    RECEIVED(10);

    private final int id;

    Type(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
