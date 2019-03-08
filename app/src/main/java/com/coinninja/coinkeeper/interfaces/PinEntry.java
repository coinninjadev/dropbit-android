package com.coinninja.coinkeeper.interfaces;

public interface PinEntry {
    boolean hasExistingPin();

    boolean isPinValid(int[] pin);

    void savePin_InRAM(String pinHASHED);

    String getPin_SavedInRam();

    void savePin_ToKeystore(String pinHASHED);

    String getSavedPin();

    PinCompare comparePins_WithFailCountDown(String pin1HASHED, String pin2HASHED);

    PinCompare comparePins(String pin1HASHED, String pin2HASHED);


    void clean();

    enum PinCompare {
        MATCH, NON_MATCH, NON_MATCH_FATAL
    }
}
