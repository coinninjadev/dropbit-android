package com.coinninja.coinkeeper.model.helpers;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SavePinTaskTest {
    @Mock
    private UserHelper userHelper;

    @InjectMocks
    private SavePinTask task;

    @After
    public void tearDown() {
        task = null;
        userHelper = null;
    }

    @Test
    public void canSaveProvidedPin() {
        String pin = "my secret pin";

        task.doInBackground(pin);

        verify(userHelper, times(1)).savePin(pin);

    }

}