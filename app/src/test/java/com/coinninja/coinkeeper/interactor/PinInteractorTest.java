package com.coinninja.coinkeeper.interactor;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.model.helpers.SavePinTask;
import com.coinninja.coinkeeper.model.helpers.UserHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private PinInteractor pinInteractor;

    @Mock
    private SavePinTask savePinTask;

    @Mock
    CoinKeeperApplication application;

    @Mock
    UserHelper userHelper;

    String pin = "foobar";


    @Before
    public void setUp() {
        when(application.getApplicationContext()).thenReturn(application);
        when(application.getUser()).thenReturn(userHelper);
        pinInteractor = new PinInteractor(application);
    }


    @Test
    public void itSavesPinToStorage() throws NoSuchFieldException {
        PrivateAccessor.setField(pinInteractor, "savePinTask", savePinTask);

        pinInteractor.savePin("foobar");

        verify(savePinTask, times(1)).execute("foobar");
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