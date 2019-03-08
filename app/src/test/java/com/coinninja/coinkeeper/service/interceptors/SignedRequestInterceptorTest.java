package com.coinninja.coinkeeper.service.interceptors;

import com.coinninja.coinkeeper.cn.wallet.DataSigner;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.util.DateUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SignedRequestInterceptorTest {

    public static final String SIGN_KEY = "02262233847a69026f8f3ae027af347f2501adf008fe4f6087d31a1d975fd41473";
    public static final String SIGNED_CONTENT = "3045022100aef1851655cd6e7ccc77afc3cd6c8f7a99de855571cea2dce9e94b17b392228f02206b37f35397018eb64d3f68995e6500d3c761c284d6a67a2509947da9137558d1";
    public static final String SIGNED_TIME_STAMP = "3044022035d8f2b8e269cc84d49ee40fb4ccbc16bcc68d845894e37fcc08da1993bebfb202202f1cd4ef2d260644de71035b196de577f4fde8d57db770a2d8697268b71b48c6";
    public static final String CN_WALLET_ID = "----wallet-id---";
    public static final String CURRENT_TIME = "2018-05-09T23:45:22Z";
    public static final String CN_USER_ID = "----USER-id---";
    public static final String CN_AUTH_DEVICE_UUID = "----96a5d785-c449-4fc2-a92f-9c7884b29b31---";

    @Mock
    DataSigner dataSigner;

    @Mock
    Interceptor.Chain chain;

    @Mock
    DateUtil dateUtil;

    @Mock
    Account account;

    Request origionalRequest;

    private SignedRequestInterceptor interceptor;
    private String content;


    @Before
    public void setUp() {
        content = "{ \"phoneNumber\": \"330-555-5555\",\"countryCode\": 1}";
        when(account.getCnWalletId()).thenReturn(CN_WALLET_ID);
        when(account.getCnUserId()).thenReturn(CN_USER_ID);
        interceptor = new SignedRequestInterceptor(dateUtil, dataSigner, CN_AUTH_DEVICE_UUID, account);
        setupRequest();
        when(dataSigner.getCoinNinjaVerificationKey()).thenReturn(SIGN_KEY);
        when(dataSigner.sign(any())).
                thenReturn(SIGNED_CONTENT);

        when(dateUtil.getCurrentTimeFormatted()).thenReturn(CURRENT_TIME);

        when(chain.request()).thenReturn(origionalRequest);
    }

    private void setupRequest() {
        RequestBody body = RequestBody.create(MediaType.parse("text/json"), content);
        origionalRequest = new Request.Builder().
                url("http://example.com").
                post(body).
                build();
    }

    @Test
    public void does_not_add_user_id_when_one_doesnt_exist() throws IOException {
        ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);
        when(account.getCnUserId()).thenReturn(null);
        interceptor.intercept(chain);

        verify(dataSigner).sign(content);
        verify(chain).proceed(argument.capture());

        Request request = argument.getValue();

        assertNull(request.headers().get(SignedRequestInterceptor.CN_AUTH_USER_ID));
    }


    @Test
    public void signs_timstamp_for_get_requests() {
        Request request = new Request.Builder().url("http://localhost:8080").method("GET", null).build();
        when(dataSigner.sign(CURRENT_TIME)).thenReturn(SIGNED_TIME_STAMP);

        request = interceptor.signRequest(request);

        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_SIG),
                equalTo(SIGNED_TIME_STAMP));
    }

    @Test
    public void signs_timestamp_as_content_when_body_of_post_is_empty() throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "");
        Request request = new Request.Builder().url("http://localhost:8080").method("POST", body).build();
        when(dataSigner.sign(CURRENT_TIME)).thenReturn(SIGNED_TIME_STAMP);

        request = interceptor.signRequest(request);

        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_SIG),
                equalTo(SIGNED_TIME_STAMP));

    }


    @Test
    public void adds_UUID_to_header_test() throws IOException {
        ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);

        interceptor.intercept(chain);

        verify(dataSigner).sign(content);
        verify(chain).proceed(argument.capture());

        Request request = argument.getValue();
        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_DEVICE_UUID), equalTo(CN_AUTH_DEVICE_UUID));
    }

    @Test
    public void does_not_add_wallet_id_when_one_doesnt_exist() throws IOException {
        ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);
        when(account.getCnWalletId()).thenReturn(null);

        interceptor.intercept(chain);

        verify(dataSigner).sign(content);
        verify(chain).proceed(argument.capture());

        Request request = argument.getValue();

        assertNull(request.headers().get(SignedRequestInterceptor.CN_AUTH_WALLET_ID));
    }

    @Test
    public void add_account_wallet_id_on_requests() throws IOException {

        ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);

        interceptor.intercept(chain);

        verify(dataSigner).sign(content);
        verify(chain).proceed(argument.capture());

        Request request = argument.getValue();
        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_WALLET_ID),
                equalTo(CN_WALLET_ID));

    }

    @Test
    public void adds_timestamp_to_request() throws IOException {
        ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);

        interceptor.intercept(chain);

        verify(dataSigner).sign(content);
        verify(chain).proceed(argument.capture());

        Request request = argument.getValue();
        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_TIMESTAMP),
                equalTo(CURRENT_TIME));

    }

    @Test
    public void adds_auth_signature() throws IOException {
        ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);

        interceptor.intercept(chain);

        verify(dataSigner).sign(content);
        verify(chain).proceed(argument.capture());

        Request request = argument.getValue();
        assertThat(request.headers().get(SignedRequestInterceptor.CN_AUTH_SIG),
                equalTo(SIGNED_CONTENT));

    }

    @Test
    public void proceeds_with_modified_request() throws IOException {
        ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);

        interceptor.intercept(chain);

        verify(chain).proceed(argument.capture());

        Request request = argument.getValue();
        assertNotNull(request);
    }
}