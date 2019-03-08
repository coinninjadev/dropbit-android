package com.coinninja.coinkeeper.service;

import android.content.Intent;

import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.runner.ResendPhoneVerificationRunner;
import com.coinninja.coinkeeper.util.Intents;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ResendPhoneVerificationServiceTest {

    ResendPhoneVerificationService service;

    ResendPhoneVerificationRunner runner;
    PhoneNumber phoneNumber;
    private Intent intent;

    @Before
    public void setUp() {
        runner = mock(ResendPhoneVerificationRunner.class);
        service = Robolectric.setupService(ResendPhoneVerificationService.class);
        phoneNumber = new PhoneNumber(1, "3305555555");

        intent = new Intent(service, ResendPhoneVerificationService.class);
        intent.putExtra(Intents.EXTRA_PHONE_NUMBER, phoneNumber);
        service.setRunner(runner);
    }

    @Test
    public void gives_runner_phone_number_to_work_with() {
        ArgumentCaptor<CNPhoneNumber> captor = ArgumentCaptor.forClass(CNPhoneNumber.class);
        service.onHandleIntent(intent);

        verify(runner).setCNPhoneNumber(captor.capture());
        CNPhoneNumber number = captor.getValue();
        assertThat(number.getPhoneNumber(), equalTo(String.valueOf(phoneNumber.getNationalNumber())));
    }

    @Test
    public void runs_resend_verificaiton() {
        service.onHandleIntent(intent);

        verify(runner).run();
    }
}