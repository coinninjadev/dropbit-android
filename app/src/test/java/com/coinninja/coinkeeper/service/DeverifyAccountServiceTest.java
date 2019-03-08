package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.Intents;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DeverifyAccountServiceTest {

    private DeverifyAccountService service;

    @Before
    public void setUp() {
        service = Robolectric.setupService(DeverifyAccountService.class);
        when(service.apiClient.resetWallet()).thenReturn(Response.success(200, null));
    }

    @After
    public void tearDown() {
        service = null;
    }

    @Test
    public void resets_wallet_remotely() {
        service.onHandleIntent(null);

        verify(service.apiClient).resetWallet();
    }

    @Test
    public void communicates_that_account_deverification_has_completed() {
        service.onHandleIntent(null);

        verify(service.localBroadCastUtil).sendBroadcast(Intents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED);
    }

    @Test
    public void resets_users_verification_status_locally() {
        service.onHandleIntent(null);

        verify(service.cnWalletManager).deverifyAccount();
    }

    @Test
    public void does_not_deverify_locally_when_remote_deverification_fails() {
        when(service.apiClient.resetWallet()).thenReturn(Response.error(500, ResponseBody.create(MediaType.parse("plain/text"), "")));

        service.onHandleIntent(null);

        verify(service.cnWalletManager, times(0)).deverifyAccount();
        verify(service.localBroadCastUtil, times(0)).sendBroadcast(Intents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED);
    }

    @Test
    public void communicates_that_deverification_failed() {
        when(service.apiClient.resetWallet()).thenReturn(Response.error(500, ResponseBody.create(MediaType.parse("plain/text"), "")));

        service.onHandleIntent(null);

        verify(service.localBroadCastUtil).sendBroadcast(Intents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED);
    }

    @Test
    public void logs_failure_to_console() {
        Response<Object> response = Response.error(500, ResponseBody.create(MediaType.parse("plain/text"), ""));
        when(service.apiClient.resetWallet()).thenReturn(response);
        service.logger = mock(CNLogger.class);

        service.onHandleIntent(null);

        verify(service.logger).logError(anyString(), anyString(), eq(response));
    }

}