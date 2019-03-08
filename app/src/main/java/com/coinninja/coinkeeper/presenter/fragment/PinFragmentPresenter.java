package com.coinninja.coinkeeper.presenter.fragment;

import com.coinninja.coinkeeper.interactor.PinEntryImpl;
import com.coinninja.coinkeeper.interfaces.PinEntry;

public class PinFragmentPresenter {
    final private PinEntry pinEntryModel;
    private View view;

    public static PinFragmentPresenter newInstance(PinEntry pinEntry) {
        return new PinFragmentPresenter(pinEntry);
    }

    public PinFragmentPresenter(PinEntry pinEntryModel) {
        this.pinEntryModel = pinEntryModel;
    }

    public void pinEntered_New(int[] pin) {
        if (pinEntryModel.isPinValid(pin)) {
            String pinHashed = PinEntryImpl.HASH_PIN(pin);
            pinEntryModel.savePin_InRAM(pinHashed);
            view.showConfirmPin();
        } else {
            view.onMalformedPin("Error malformed pin!");
        }
    }

    public void pinEntered_Confirm(int[] pin) throws Exception {
        if (pinEntryModel.isPinValid(pin)) {
            String pinHASHED = PinEntryImpl.HASH_PIN(pin);
            PinEntry.PinCompare failedComparisonCount = pinEntryModel.comparePins_WithFailCountDown(pinHASHED, pinEntryModel.getPin_SavedInRam());
            switch (failedComparisonCount) {
                case MATCH:
                    pinEntryModel.savePin_ToKeystore(pinHASHED);
                    view.onPinConfirmedAndSaved(pinHASHED);
                    break;
                case NON_MATCH:
                    view.onPinMismatch();
                    break;
                case NON_MATCH_FATAL:
                    view.onPinMismatchFATAL();
                    break;
            }

        } else {
            view.onMalformedPin("Error malformed pin!");
        }
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void onDestroyPinConfirm() {
        clearPin();
        view.forceSoftKey();
    }

    public void clearPin() {
        pinEntryModel.clean();
    }

    public interface View {
        void showConfirmPin();

        void forceSoftKey();

        void onPinConfirmedAndSaved(String userPinHashed);

        void onMalformedPin(String msg);

        void onPinMismatch();

        void onPinMismatchFATAL();
    }
}
