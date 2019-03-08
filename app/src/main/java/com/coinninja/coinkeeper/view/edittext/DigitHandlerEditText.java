package com.coinninja.coinkeeper.view.edittext;

import android.text.Editable;
import android.text.TextWatcher;

public class DigitHandlerEditText implements TextWatcher {

    private final PinEditText pinEditText;

    public DigitHandlerEditText(PinEditText pinEditText) {
        this.pinEditText = pinEditText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable input) {
        String inputStr = input.toString();

        if (inputStr.isEmpty()) {
            return;
        }

        int[] userPin = getPinWithIntegrityCheck(inputStr);

        if (userPin.length == 6) {
            pinEditText.onSixDigits();
        }
    }


    //Will Throw NumberFormatException if Integrity fails
    public int[] getPinWithIntegrityCheck(String inputStr) {
        if (inputStr.isEmpty()) {
            return new int[0];
        }

        int[] userPin = new int[inputStr.length()];

        try {
            char[] stringInputValue = inputStr.toCharArray();
            for (int i = 0; i < stringInputValue.length; i++) {
                int currentInputDigit = Integer.parseInt(String.valueOf(stringInputValue[i]));
                integrityCheck(currentInputDigit);// will throw NumberFormatException
                userPin[i] = currentInputDigit;
            }
        } catch (NumberFormatException exception) {
            System.out.println("Could not parse " + exception);
            throw exception;
        }
        return userPin;
    }


    private void integrityCheck(int currentInputDigit) {
        if (currentInputDigit < 0 || currentInputDigit >= 10) {
            throw new NumberFormatException("Input value error");
        }
    }
}
