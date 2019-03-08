package com.coinninja.coinkeeper.service.tasks;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import retrofit2.Response;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CNHealthCheckTaskTest {

    @Mock
    CNHealthCheckTask.HealthCheckCallback callback;

    @Mock
    CoinKeeperApplication application;

    @Mock
    CoinKeeperApiClient apiClient;

    private CNHealthCheckTask task;

    @Before
    public void setUp() {
        when(application.getAPIClient()).thenReturn(apiClient);
        when(apiClient.checkHealth()).thenReturn(Response.success("OK"));
        task = CNHealthCheckTask.newInstance(application, callback);
    }

    @Test
    public void conducts_health_check() {
        Boolean isSuccessful = task.doInBackground();

        verify(apiClient).checkHealth();

        assertTrue(isSuccessful);
    }

    @Test
    public void successful_checks_notify_of_success() {
        task.onPostExecute(true);

        verify(callback).onHealthSuccess();
    }

    @Test
    public void failed_checks_inform_callback() {
        task.onPostExecute(false);

        verify(callback).onHealthFail();
    }

    @Test
    public void inits_instance() {
        assertNotNull(task);
        assert (task instanceof CNHealthCheckTask);
    }
}
