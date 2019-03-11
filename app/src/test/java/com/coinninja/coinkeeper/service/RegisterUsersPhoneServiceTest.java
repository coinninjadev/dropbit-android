package com.coinninja.coinkeeper.service;

import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.runner.RegisterPhoneNumberRunnable;
import com.coinninja.coinkeeper.util.Intents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class RegisterUsersPhoneServiceTest {

    public static final String NATIONAL_NUMBER = "3305555555";
    public static final int COUNTRY_CODE = 1;

    RegisterPhoneNumberRunnable runner;
    private RegisterUsersPhoneService service;
    private PhoneNumber phoneNumber;
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.application.getApplicationContext();
        runner = mock(RegisterPhoneNumberRunnable.class);

        service = new RegisterUsersPhoneService();
        service.setRunner(runner);


        phoneNumber = new PhoneNumber(COUNTRY_CODE, NATIONAL_NUMBER);
    }

    @Test
    public void executes_underlining_runner_on_handle_intent() {
        Intent intent = new Intent(context, RegisterUsersPhoneService.class);
        intent.putExtra(Intents.EXTRA_PHONE_NUMBER, phoneNumber);
        ArgumentCaptor<CNPhoneNumber> argumentCaptor = ArgumentCaptor.forClass(CNPhoneNumber.class);

        service.onHandleIntent(intent);

        verify(runner).setCNPhoneNumber(argumentCaptor.capture());
        verify(runner).run();

        CNPhoneNumber number = argumentCaptor.getValue();
        assertThat(number.getPhoneNumber(), equalTo("3305555555"));
    }

}