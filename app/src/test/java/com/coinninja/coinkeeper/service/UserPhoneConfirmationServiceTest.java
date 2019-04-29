package com.coinninja.coinkeeper.service;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.service.runner.UserPhoneConfirmationRunner;
import com.coinninja.coinkeeper.util.DropbitIntents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class UserPhoneConfirmationServiceTest {

    private static final String CODE = "109283";

    @Mock
    private UserPhoneConfirmationRunner runner;

    private Intent intent;

    @InjectMocks
    UserPhoneConfirmationService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new UserPhoneConfirmationService();
        service.runner = runner;
        intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER_CODE, CODE);
    }

    @Test
    public void does_not_run_with_no_code() {
        service.onHandleIntent(new Intent());

        verify(runner, times(0)).setCode(CODE);
        verify(runner, times(0)).run();

    }

    @Test
    public void executes_runner_for_code() {
        service.onHandleIntent(intent);

        verify(runner).setCode(CODE);
        verify(runner).run();
    }
}