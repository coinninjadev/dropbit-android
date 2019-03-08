package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.CoinKeeperApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SavePinTaskTest {

    @Mock
    CoinKeeperApplication application;

    @Mock
    UserHelper userHelper;

    private SavePinTask task;

    @Before
    public void setUp() {
        task = new SavePinTask(application);
        when(application.getUser()).thenReturn(userHelper);
    }

    @Test
    public void canSaveProvidedPin() {
        String pin = "my secret pin";

        task.doInBackground(pin);

        verify(userHelper, times(1)).savePin(pin);

    }

}