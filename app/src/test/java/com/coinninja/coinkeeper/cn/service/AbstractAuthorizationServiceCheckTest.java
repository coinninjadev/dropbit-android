package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;
import com.coinninja.coinkeeper.util.CNLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AbstractAuthorizationServiceCheckTest {
    @Mock
    CNLogger logger;

    private ImplServiceCheck serviceCheck;

    @Before
    public void setUp() {
        serviceCheck = new ImplServiceCheck(logger);
    }

    @After
    public void tearDown() throws Exception {
        logger = null;
        serviceCheck = null;
    }

    @Test()
    public void will_log_errors() {
        Response bad_gateway = Response.error(502, ResponseBody.create(MediaType.parse("application/json"), "Bad Gateway"));

        try {
            serviceCheck.handleResponse(bad_gateway);
        } catch (CNServiceException ex) {
        }


        verify(logger).logError(eq(serviceCheck.getTag()), anyString(), eq(bad_gateway));
    }

    @Test(expected = CNServiceException.class)
    public void will_throw_remote_failures__bad_gateway() throws CNServiceException {

        Response bad_gateway = Response.error(502, ResponseBody.create(MediaType.parse("application/json"), "Bad Gateway"));

        serviceCheck.handleResponse(bad_gateway);
    }

    @Test(expected = CNServiceException.class)
    public void will_throw_local_failures__bad_request() throws CNServiceException {

        Response bad_request = Response.error(400,
                ResponseBody.create(MediaType.parse("application/json"), "Bad Request"));

        serviceCheck.handleResponse(bad_request);
    }

    class ImplServiceCheck extends AbstractAuthorizationServiceCheck {

        private CNLogger logger;

        public ImplServiceCheck(CNLogger logger) {
            this.logger = logger;
        }

        @Override
        CNLogger getLogger() {
            return logger;
        }

        @Override
        String getTag() {
            return getClass().getName();
        }

        @Override
        void setRaw(String string) {

        }

    }
}