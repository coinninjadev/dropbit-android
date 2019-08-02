package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.CNLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserVerificationServiceCheckTest {
    @Mock
    private SignedCoinKeeperApiClient apiClient;

    @Mock
    private CNLogger logger;

    @Mock
    private CNWalletManager cnWalletManager;

    @InjectMocks
    private UserVerificationServiceCheck serviceCheck;

    @Before
    public void setUp() {
        when(apiClient.verifyAccount()).thenReturn(Response.success(new CNUserAccount()));
    }

    @After
    public void tearDown() {
        apiClient = null;
        logger = null;
        serviceCheck = null;
        cnWalletManager = null;
    }

    @Test
    public void sets_raw_error_on_self() throws CNServiceException {
        when(apiClient.verifyAccount()).thenReturn(
                Response.error(401,
                        ResponseBody.create(MediaType.parse("application/json"), "{ \"message\" : \"device_uuid mismatch\"")));


        serviceCheck.isVerified();

        assertThat(serviceCheck.getRaw(), equalTo("{ \"message\" : \"device_uuid mismatch\""));

    }

    @Test
    public void deverification_cause_is_mismatched_on_mismatch_response() throws CNServiceException {
        when(apiClient.verifyAccount()).thenReturn(
                Response.error(401,
                        ResponseBody.create(MediaType.parse("application/json"), "{ \"message\" : \"device_uuid mismatch\"")));


        serviceCheck.isVerified();

        assertThat(serviceCheck.deverificaitonReason(), equalTo(CoinNinjaServiceCheck.DeverifiedCause.MISMATCH));
    }

    @Test
    public void deverification_cause_is_dropped() throws CNServiceException {
        when(apiClient.verifyAccount()).thenReturn(
                Response.error(401,
                        ResponseBody.create(MediaType.parse("application/json"), "")));


        serviceCheck.isVerified();

        assertThat(serviceCheck.deverificaitonReason(), equalTo(CoinNinjaServiceCheck.DeverifiedCause.DROPPED));
    }

    @Test
    public void deverification_cause_null_when_active() throws CNServiceException {
        serviceCheck.isVerified();

        assertNull(serviceCheck.deverificaitonReason());
    }

    @Test
    public void performsDeverification() {
        serviceCheck.performDeverification();

        verify(cnWalletManager).deVerifyAccount();
    }

    @Test
    public void verifying_wallet_does_not_log_success() throws CNServiceException {
        assertTrue(serviceCheck.isVerified());

        verify(logger, times(0)).logError(anyString(), anyString(),
                any(Response.class));
    }

    @Test
    public void deregister_wallet_when_not_authorized() throws CNServiceException {
        when(apiClient.verifyAccount()).thenReturn(
                Response.error(401,
                        ResponseBody.create(MediaType.parse("application/json"), "")));

        assertFalse(serviceCheck.isVerified());
    }


    @Test
    public void verifies_wallet_when_an_unverified_account() throws CNServiceException {
        serviceCheck.isVerified();

        verify(apiClient).verifyAccount();
    }
}