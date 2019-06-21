package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.cn.service.CoinNinjaServiceCheck.DeverifiedCause;
import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNWallet;
import com.coinninja.coinkeeper.util.CNLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WalletVerificationServiceCheckTest {

    @Mock
    WalletHelper walletHelper;

    @Mock
    SignedCoinKeeperApiClient apiClient;

    @Mock
    CNLogger logger;

    private WalletVerificationServiceCheck serviceCheck;

    @Before
    public void setUp() {
        when(apiClient.verifyWallet()).thenReturn(Response.success(new CNWallet()));
        serviceCheck = new WalletVerificationServiceCheck(apiClient, walletHelper, logger);
    }

    @After
    public void tearDown() throws Exception {
        walletHelper = null;
        apiClient = null;
        logger = null;
        serviceCheck = null;
    }

    @Test
    public void provides_access_to_raw_message() throws CNServiceException {
        when(apiClient.verifyWallet()).thenReturn(
                Response.error(401,
                        ResponseBody.create(MediaType.parse("application/json"), "Resource could not be found")));

        serviceCheck.isVerified();

        assertThat(serviceCheck.getRaw(), equalTo("Resource could not be found"));
    }

    @Test
    public void deverification_cause_is_dropped() throws CNServiceException {
        when(apiClient.verifyWallet()).thenReturn(
                Response.error(401,
                        ResponseBody.create(MediaType.parse("application/json"), "Resource could not be found")));


        serviceCheck.isVerified();

        assertThat(serviceCheck.deverificaitonReason(), equalTo(DeverifiedCause.DROPPED));
    }

    @Test
    public void deverification_cause_null_when_active() throws CNServiceException {
        serviceCheck.isVerified();

        assertNull(serviceCheck.deverificaitonReason());
    }

    @Test
    public void performesDeverification() {
        serviceCheck.performDeverification();

        verify(walletHelper).removeCurrentCnRegistration();
    }

    @Test
    public void deregisters_wallet_when_not_authorized() throws CNServiceException {
        when(apiClient.verifyWallet()).thenReturn(
                Response.error(401,
                        ResponseBody.create(MediaType.parse("application/json"), "")));

        assertFalse(serviceCheck.isVerified());
    }


    @Test
    public void verifies_wallet_when_an_unverified_account() throws CNServiceException {
        serviceCheck.isVerified();

        verify(apiClient).verifyWallet();
    }

    @Test
    public void has_a_tag() {
        assertThat(serviceCheck.getTag(), equalTo(WalletVerificationServiceCheck.class.getName()));
    }

}