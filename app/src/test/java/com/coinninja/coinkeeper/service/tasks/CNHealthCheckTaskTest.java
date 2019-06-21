package com.coinninja.coinkeeper.service.tasks;

import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import retrofit2.Response;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CNHealthCheckTaskTest {

    @Mock
    private CNHealthCheckTask.HealthCheckCallback callback;

    @Mock
    private CoinKeeperApiClient apiClient;

    @InjectMocks
    private CNHealthCheckTask task;

    @Before
    public void setUp() {
        when(apiClient.checkHealth()).thenReturn(Response.success("OK"));
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
