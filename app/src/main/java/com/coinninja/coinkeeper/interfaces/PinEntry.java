package com.coinninja.coinkeeper.interfaces;

public interface PinEntry {
    boolean hasExistingPin();

    boolean isPinValid(int[] pin);

    void savePin_InRAM(String pinHashed);

    String getPin_SavedInRam();

    void savePin(String pinHashed);

    String getSavedPin();

    PinCompare comparePins_WithFailCountDown(String firstPinHashed, String secondPinHashed);

    PinCompare comparePins(String firstPinHashed, String secondPinHashed);

    void clean();

    enum PinCompare {
        MATCH, NON_MATCH, NON_MATCH_FATAL
    }
}
