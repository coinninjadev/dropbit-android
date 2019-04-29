package com.coinninja.coinkeeper.interactor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PinEntryImplTest {
    private PinEntryImpl pinEntry;
    private PinInteractor keyStoreInteractor;

    @Before
    public void setUp() throws Exception {
        keyStoreInteractor = mock(PinInteractor.class);
        pinEntry = new PinEntryImpl(keyStoreInteractor);
    }

    @Test
    public void hasExistingPin() throws Exception {
        boolean hasPin = pinEntry.hasExistingPin();
        assertFalse(hasPin);
    }

    @Test
    public void getPin_SavedInKeystore() throws Exception {
        int[] userPin = new int[]{4, 2, 9, 7, 6, 3};
        String userPinHASH = PinEntryImpl.HASH_PIN(userPin);
        when(keyStoreInteractor.getSavedPin()).thenReturn(userPinHASH);

        String pin = pinEntry.getSavedPin();

        assertTrue(pin.contains(userPinHASH));
    }

    @Test
    public void savePin_ToKeystore() throws Exception {
        int[] userPin = new int[]{4, 2, 9, 7, 6, 3};
        String userPinHASH = PinEntryImpl.HASH_PIN(userPin);

        when(keyStoreInteractor.getSavedPin()).thenReturn(userPinHASH);
        pinEntry.savePin(userPinHASH);

        verify(keyStoreInteractor).savePin(eq(userPinHASH));
    }

}