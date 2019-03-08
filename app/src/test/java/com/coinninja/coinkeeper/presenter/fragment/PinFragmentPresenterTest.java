package com.coinninja.coinkeeper.presenter.fragment;

import com.coinninja.coinkeeper.interactor.PinEntryImpl;
import com.coinninja.coinkeeper.interactor.PinInteractor;
import com.coinninja.coinkeeper.interfaces.PinEntry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import junitx.util.PrivateAccessor;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PinFragmentPresenterTest {
    private PinFragmentPresenter presenter;
    private PinEntryImpl pinEntryModel;
    private PinFragmentPresenter.View view;

    @Mock
    private PinInteractor keyStoreInteractor;
    @Mock
    private PinInteractor auth;

    @Before
    public void setUp() throws Exception {
        pinEntryModel = new PinEntryImpl(auth);
        presenter = new PinFragmentPresenter(pinEntryModel);
        view = mock(PinFragmentPresenter.View.class);
    }

    @Test
    public void pinEntered_New() throws Exception {
        int[] validPin = {4, 3, 5, 9, 7, 8};
        String validPinHash = PinEntryImpl.HASH_PIN(validPin);

        enterPin(validPin);

        verify(view, never()).onMalformedPin(any());
        assertEquals(pinEntryModel.getPin_SavedInRam(), validPinHash);
        verify(view).showConfirmPin();
    }

    @Test
    public void pinEntered_New_Invalid() throws Exception {
        int[] invalidPin_ToShort = {4, 3, 5, 9};
        int[] invalidPin_ToLong = {4, 3, 5, 9, 4, 3, 3, 2, 5, 5};
        int[] invalidPin_null = null;
        int[] invalidPin_Not_single_digits = {4, 37, 55, 89, 5464, 3};


        enterPin(invalidPin_ToShort);
        presenter.pinEntered_New(invalidPin_ToLong);
        presenter.pinEntered_New(invalidPin_null);
        presenter.pinEntered_New(invalidPin_Not_single_digits);


        verify(view, times(4)).onMalformedPin(any());
        assertNull(pinEntryModel.getPin_SavedInRam());
        verify(view, never()).showConfirmPin();
    }

    @Test
    public void pinEntered_Confirm() throws Exception {
        PinInteractor pinInteractor = mock(PinInteractor.class);
        PrivateAccessor.setField(pinEntryModel, "pinInteractor", pinInteractor);
        int[] validPin = {4, 3, 5, 9, 7, 8};
        String validPinHash = PinEntryImpl.HASH_PIN(validPin);
        when(pinInteractor.verifyPin(validPinHash)).thenReturn(true);
        when(pinInteractor.getSavedPin()).thenReturn(validPinHash);


        presenter.attachView(view);
        enterPin(validPin);
        presenter.pinEntered_Confirm(validPin);

        assertEquals(pinEntryModel.getPin_SavedInRam(), validPinHash);
        verify(view).onPinConfirmedAndSaved(validPinHash);
    }

    @Test
    public void pinEntered_Confirm_Invalid() throws Exception {
        int[] validPin = {4, 3, 5, 9, 7, 8};
        int[] validPin_NonMatch = {1, 3, 7, 9, 3, 1};
        String validPinHash = PinEntryImpl.HASH_PIN(validPin);

        int oneLessThanMaxNumbersOfMismatches = PinEntryImpl.MAX_FAIL_PIN_COUNT - 1;//don't want to trigger a FATAL mismatch

        presenter.attachView(view);
        enterPin(validPin);
        for (int i = 0; i < oneLessThanMaxNumbersOfMismatches; i++) {
            presenter.pinEntered_Confirm(validPin_NonMatch);
        }

        assertEquals(pinEntryModel.getPin_SavedInRam(), validPinHash);
        verify(view, never()).onPinConfirmedAndSaved(validPinHash);
        verify(view, times(oneLessThanMaxNumbersOfMismatches)).onPinMismatch();
        verify(view, never()).onPinMismatchFATAL();
    }

    @Test
    public void pinEntered_Confirm_Invalid3Times() throws Exception {
        int[] validPin = {4, 3, 5, 9, 7, 8};
        int[] validPin_NonMatch = {1, 3, 7, 9, 3, 1};
        String validPinHash = PinEntryImpl.HASH_PIN(validPin);
        int maxNumbersOfMismatches = PinEntryImpl.MAX_FAIL_PIN_COUNT;

        presenter.attachView(view);
        enterPin(validPin);
        for (int i = 0; i < maxNumbersOfMismatches; i++) {
            presenter.pinEntered_Confirm(validPin_NonMatch);
        }

        assertEquals(pinEntryModel.getPin_SavedInRam(), validPinHash);
        verify(view, never()).onPinConfirmedAndSaved(validPinHash);
        verify(view, times(maxNumbersOfMismatches - 1)).onPinMismatch();
        verify(view).onPinMismatchFATAL();
    }

    @Test
    public void attachView() throws Exception {
        presenter.attachView(view);
        PinFragmentPresenter.View presenterView = (PinFragmentPresenter.View) PrivateAccessor.getField(presenter, "view");

        assertEquals(presenterView, view);
    }

    @Test
    public void onDestroyPinConfirm() throws Exception {
        presenter.attachView(view);
        presenter.onDestroyPinConfirm();

        verify(view).forceSoftKey();
    }

    @Test
    public void pinSave() throws Exception {
        PinInteractor keyStoreInteractor = mock(PinInteractor.class);
        int[] validPin = {4, 3, 5, 9, 7, 8};
        String validPinHash = PinEntryImpl.HASH_PIN(validPin);

        PinEntry mockPinEntryModel = mock(PinEntry.class);
        PinFragmentPresenter pinFragmentPresenter = new PinFragmentPresenter(mockPinEntryModel);
        when(mockPinEntryModel.isPinValid(validPin)).thenReturn(true);
        when(mockPinEntryModel.comparePins_WithFailCountDown(eq(validPinHash), any())).thenReturn(PinEntry.PinCompare.MATCH);
        when(keyStoreInteractor.verifyPin(validPinHash)).thenReturn(true);
        pinFragmentPresenter.attachView(view);

        pinFragmentPresenter.pinEntered_Confirm(validPin);

        verify(mockPinEntryModel).savePin_ToKeystore(validPinHash);
    }

    private void enterPin(int[] validPin) {
        presenter.attachView(view);
        presenter.pinEntered_New(validPin);
    }
}