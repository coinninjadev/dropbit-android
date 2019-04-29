package com.coinninja.coinkeeper.interactor;

import com.coinninja.coinkeeper.model.helpers.UserHelper;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import junitx.util.PrivateAccessor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PinInteractorTest {

    @Mock
    private UserHelper userHelper;

    private String pin = "foobar";

    @InjectMocks
    private PinInteractor pinInteractor;

    @After
    public void tearDown() {
        pin = null;
        userHelper = null;
    }

    @Test
    public void itSavesPinToStorage() {
        pinInteractor.savePin("foobar");

        verify(userHelper, times(1)).savePin("foobar");
    }

    @Test
    public void canRetrieveSavedPin() {
        when(userHelper.getPin()).thenReturn(pin);

        assertThat(pinInteractor.getSavedPin(), equalTo(pin));
    }

    @Test
    public void emptyPinsReturnNull() {
        when(userHelper.getPin()).thenReturn("");

        assertThat(pinInteractor.getSavedPin(), equalTo(null));
    }

    @Test
    public void canVerifySuppliedPinWithSavedPin() {
        when(userHelper.getPin()).thenReturn(pin);
        assertThat(pinInteractor.verifyPin(pin), equalTo(true));
    }

    @Test
    public void canVerifySuppliedPinWithSavedPinWithNull() {
        assertThat(pinInteractor.verifyPin(null), equalTo(false));
    }
}