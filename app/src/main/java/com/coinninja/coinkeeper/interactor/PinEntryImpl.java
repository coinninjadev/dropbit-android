package com.coinninja.coinkeeper.interactor;


import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.util.Hasher;

public class PinEntryImpl implements PinEntry {
    public static final int MAX_FAIL_PIN_COUNT = 3;

    private final PinInteractor pinInteractor;
    private String inRamPin_hashed;
    private int failedComparisonCount = 0;

    public PinEntryImpl(PinInteractor keyStoreInteractor) {
        pinInteractor = keyStoreInteractor;
    }

    @Override
    public boolean hasExistingPin() {
        String userPinHash = getSavedPin();
        return userPinHash != null && !userPinHash.isEmpty();
    }

    @Override
    public boolean isPinValid(int[] pin) {
        if (pin == null || pin.length != 6) {
            return false;
        }

        for (int pinDigit : pin) {
            if (pinDigit < 0 || pinDigit > 9) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void savePin_InRAM(String newPin_hashed) {
        inRamPin_hashed = newPin_hashed;
    }

    @Override
    public String getPin_SavedInRam() {
        return inRamPin_hashed;
    }

    @Override
    public PinCompare comparePins_WithFailCountDown(String pin1Hashed, String pin2Hashed) {
        if (comparePins(pin1Hashed, pin2Hashed) == PinCompare.MATCH) {
            return PinCompare.MATCH;
        }

        failedComparisonCount++;

        if (failedComparisonCount >= MAX_FAIL_PIN_COUNT) {
            return PinCompare.NON_MATCH_FATAL;
        }

        return PinCompare.NON_MATCH;
    }

    @Override
    public PinCompare comparePins(String pin1Hashed, String pin2Hashed) {
        if (pin1Hashed.contentEquals(pin2Hashed)) {
            return PinCompare.MATCH;
        }

        return PinCompare.NON_MATCH;
    }

    @Override
    public void savePin_ToKeystore(String savePinHashed) {
        pinInteractor.savePin(savePinHashed);
    }

    @Override
    public String getSavedPin() {
        try {
            return pinInteractor.getSavedPin();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load saved pin");
        }
    }

    @Override
    public void clean() {
        inRamPin_hashed = "";
        inRamPin_hashed = null;
        failedComparisonCount = 0;
    }

    public static String HASH_PIN(int[] newPin) {
        String pin = "";
        for (int i : newPin) {
            pin += String.valueOf(i);
        }
        return new Hasher().hash(pin);
    }

}
