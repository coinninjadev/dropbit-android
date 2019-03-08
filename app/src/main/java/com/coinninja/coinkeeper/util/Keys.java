package com.coinninja.coinkeeper.util;

public enum Keys {
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    ZERO("0"),
    DOT("."),
    BACK("-1"),
    CLEAR("-11");

    private String value;


    Keys(String value) {
        this.value = value;
    }


    public String getValue() {
        return value;
    }
}
