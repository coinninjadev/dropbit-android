package com.coinninja.coinkeeper.view.edittext;

import android.text.Editable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DigitHandlerEditTextTest {
    @Mock
    PinEditText pinEditText;

    DigitHandlerEditText digitHandlerEditText;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        digitHandlerEditText = new DigitHandlerEditText(pinEditText);
    }

    @Test
    public void afterTextChanged_LessThan6Digits() throws Exception {
        String pin = "5492";
        Editable input = mock(Editable.class);
        when(input.toString()).thenReturn(pin);

        digitHandlerEditText.afterTextChanged(input);

        verify(pinEditText, never()).onSixDigits();
    }

    @Test
    public void afterTextChanged_6Digits() throws Exception {
        String pin = "549246";
        Editable input = mock(Editable.class);
        when(input.toString()).thenReturn(pin);

        digitHandlerEditText.afterTextChanged(input);

        verify(pinEditText).onSixDigits();
    }

    @Test
    public void afterTextChanged_6Digits_every_Digit() throws Exception {
        //every digit 1 2 3 4 5 6 7 8 9
        String pin = "012345";
        String pin2 = "678901";

        Editable input = mock(Editable.class);
        when(input.toString()).thenReturn(pin);

        Editable input2 = mock(Editable.class);
        when(input2.toString()).thenReturn(pin2);

        digitHandlerEditText.afterTextChanged(input);
        digitHandlerEditText.afterTextChanged(input2);


        verify(pinEditText, times(2)).onSixDigits();
    }

    @Test
    public void afterTextChanged_empty() throws Exception {
        String pin = "";
        Editable input = mock(Editable.class);
        when(input.toString()).thenReturn(pin);

        digitHandlerEditText.afterTextChanged(input);

        verify(pinEditText, never()).onSixDigits();
    }

    @Test
    public void afterTextChanged_NumberFormatException() {
        String pin = "er3564r5r";
        Editable input = mock(Editable.class);
        when(input.toString()).thenReturn(pin);

        try {
            digitHandlerEditText.afterTextChanged(input);
            fail();//if we got here then fail
        } catch (NumberFormatException exception) {
            assertNotNull(exception);
        }

        verify(pinEditText, never()).onSixDigits();
    }


    @Test
    public void getPinWithIntegrityCheck() throws Exception {
        String pin = "";

        digitHandlerEditText.getPinWithIntegrityCheck(pin);

        verify(pinEditText, never()).onSixDigits();

    }

}