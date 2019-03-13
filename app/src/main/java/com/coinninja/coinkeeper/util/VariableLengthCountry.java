package com.coinninja.coinkeeper.util;

public enum VariableLengthCountry {
    UNNECESSARY(0, ""),
    MEXICO(52, "1"),
    ARGENTINA(54, "9"),
    SAN_MARINO(378, "549");

    private final int value;
    private String opt;

    VariableLengthCountry(final int newValue, String opt) {
        value = newValue;
        this.opt = opt;
    }

    public String getValue() { return String.valueOf(value); }

    public static VariableLengthCountry from(int countryCode) {
        switch(countryCode) {
            case 52:
                return MEXICO;
            case 54:
                return ARGENTINA;
            case 378:
                return SAN_MARINO;
        }

        return UNNECESSARY;
    }

    public String getOptionalNationalPrefix() {
        return opt;
    }
}
