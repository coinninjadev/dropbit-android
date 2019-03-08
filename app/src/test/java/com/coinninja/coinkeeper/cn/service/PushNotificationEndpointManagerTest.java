package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNDeviceEndpoint;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class PushNotificationEndpointManagerTest {
    private String CN_DEVICE_ID = "158a4cf9-0362-4636-8c68-ed7a98a7f345";
    private String TOKEN = "-push_token-";
    private String ENDPOINT_ID = "-- device_endpoint_id";
    private CNDeviceEndpoint cnDeviceEndpoint;

    @Mock
    CNLogger logger;

    @Mock
    PreferencesUtil preferencesUtil;

    @Mock
    SignedCoinKeeperApiClient apiClient;

    @Mock
    PushNotificationTokenManager pushNotificationTokenManager;

    @Mock
    PushNotificationDeviceManager pushNotificationDeviceManager;

    @Mock
    PushNotificationSubscriptionManager pushNotificationSubscriptionManager;

    @InjectMocks
    PushNotificationEndpointManager pushNotificationEndpointManager;

    @Before
    public void setUp() {
        when(pushNotificationTokenManager.getToken()).thenReturn(TOKEN);
        when(pushNotificationDeviceManager.getDeviceId()).thenReturn(CN_DEVICE_ID);

        cnDeviceEndpoint = new CNDeviceEndpoint();
        cnDeviceEndpoint.setId("-- device_endpoint_id");
        Response response = Response.success(cnDeviceEndpoint);
        when(apiClient.registerForPushEndpoint(CN_DEVICE_ID, TOKEN)).thenReturn(response);
    }

    @After
    public void tearDown() {
        CN_DEVICE_ID = null;
        TOKEN = null;
        ENDPOINT_ID = null;
        cnDeviceEndpoint = null;
        logger = null;
        preferencesUtil = null;
        apiClient = null;
        pushNotificationTokenManager = null;
        pushNotificationDeviceManager = null;
        pushNotificationSubscriptionManager = null;
        pushNotificationEndpointManager = null;
    }

    @Test
    public void removes_endpoint_locally() {
        pushNotificationEndpointManager.removeEndpoint();

        verify(preferencesUtil).removePreference(PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID);
    }

    @Test
    public void registers_device_endpoint_with_coinninja_using_cached_device_id() {
        pushNotificationEndpointManager.registersAsEndpoint(CN_DEVICE_ID);

        verify(apiClient).registerForPushEndpoint(CN_DEVICE_ID, TOKEN);
    }

    @Test
    public void saves_device_endpoint_once_created() {
        pushNotificationEndpointManager.registersAsEndpoint(CN_DEVICE_ID);

        verify(preferencesUtil).savePreference(
                PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, cnDeviceEndpoint.getID());
    }

    @Test
    public void log_error_when_fail_to_create_endpoint() {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("text"), "");
        Response response = Response.error(400, responseBody);
        when(apiClient.registerForPushEndpoint(any(), any())).thenReturn(response);

        pushNotificationEndpointManager.registersAsEndpoint(CN_DEVICE_ID);

        verify(logger).logError(PushNotificationEndpointManager.TAG, PushNotificationEndpointManager.DEVICE_ENDPOINT_ERROR_MESSAGE, response);
    }

    @Test
    public void saves_endpoint_when_registering_device_endpoint_with_device_id_and_token() {

        pushNotificationEndpointManager.registersAsEndpoint(CN_DEVICE_ID, TOKEN);

        verify(preferencesUtil).savePreference(PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID,
                cnDeviceEndpoint.getID());
    }

    @Test
    public void log_error_when_registering_device_endpoint_with_cached_values() {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("text"), "");
        Response response = Response.error(400, responseBody);
        when(apiClient.registerForPushEndpoint(any(), any())).thenReturn(response);

        pushNotificationEndpointManager.registersAsEndpoint(CN_DEVICE_ID, TOKEN);

        verify(logger).logError(PushNotificationEndpointManager.TAG, PushNotificationEndpointManager.DEVICE_ENDPOINT_ERROR_MESSAGE, response);
    }

    @Test
    public void saves_endpoint_when_registering_device_endpoint_with_cached_values() {
        pushNotificationEndpointManager.registersAsEndpoint();

        verify(apiClient).registerForPushEndpoint(CN_DEVICE_ID, TOKEN);
    }

    @Test
    public void subscribes_to_all_available_topics_when_registering_as_endpoint() {
        when(preferencesUtil.getString(PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, null)).thenReturn(ENDPOINT_ID);

        pushNotificationEndpointManager.registersAsEndpoint();

        verify(pushNotificationSubscriptionManager).subscribeToChannels(CN_DEVICE_ID, ENDPOINT_ID);
    }

    @Test
    public void does_not_register_a_device_endpoint_with_coinninja_when_device_id_not_cached() {
        when(pushNotificationDeviceManager.getDeviceId()).thenReturn(null);
        pushNotificationEndpointManager.registersAsEndpoint();


        when(pushNotificationDeviceManager.getDeviceId()).thenReturn("");
        pushNotificationEndpointManager.registersAsEndpoint();


        verify(apiClient, times(0)).registerForPushEndpoint(anyString(), anyString());
    }

    @Test
    public void does_not_register_a_device_endpoint_with_coinninja_when_token_not_cached() {
        when(pushNotificationTokenManager.getToken()).thenReturn(null);

        pushNotificationEndpointManager.registersAsEndpoint();


        when(pushNotificationTokenManager.getToken()).thenReturn("");
        pushNotificationEndpointManager.registersAsEndpoint();


        verify(apiClient, times(0)).registerForPushEndpoint(anyString(), anyString());
    }

    // ENDPOINT VERIFICATION


    @Test
    public void returns_true_when_endpoint_is_cached() {
        when(preferencesUtil.contains(PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID)).thenReturn(true);

        assertTrue(pushNotificationEndpointManager.hasEndpoint());
    }


    @Test
    public void returns_saved_endpoint() {
        String endpoint = "--endpoint";
        when(preferencesUtil.getString(
                PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, null))
                .thenReturn(endpoint);

        assertThat(pushNotificationEndpointManager.getEndpoint(), equalTo(endpoint));
    }


    @Test
    public void provides_list_of_endpoints_from_the_server() {
        List<CNDeviceEndpoint> endpoints = new ArrayList<>();
        endpoints.add(new CNDeviceEndpoint());
        when(apiClient.fetchRemoteEndpointsFor(CN_DEVICE_ID)).thenReturn(generateSuccessResponse(200, endpoints));


        assertThat(pushNotificationEndpointManager.fetchEndpoints(), equalTo(endpoints));
    }

    @Test
    public void provides_empty_list_of_endpoints_from_the_server_on_error() {
        List<CNDeviceEndpoint> endpoints = new ArrayList<>();
        Response<Object> response = Response.error(500, ResponseBody.create(MediaType.parse("plain/text"), ""));
        when(apiClient.fetchRemoteEndpointsFor(CN_DEVICE_ID)).thenReturn(
                response);

        assertThat(pushNotificationEndpointManager.fetchEndpoints(), equalTo(endpoints));
        verify(logger).logError(PushNotificationEndpointManager.TAG,
                PushNotificationEndpointManager.FETCH_DEVICE_ENDPOINT_ERROR_MESSAGE, response);
    }

    @Test
    public void logs_unregister_error() {
        String endpoint = "--endpoint";
        Response<Object> response = Response.error(500, ResponseBody.create(MediaType.parse("plain/text"), ""));
        when(apiClient.unRegisterDeviceEndpoint(CN_DEVICE_ID, endpoint)).thenReturn(response);

        pushNotificationEndpointManager.unRegister(endpoint);
        verify(logger).logError(PushNotificationEndpointManager.TAG,
                PushNotificationEndpointManager.UNREGISTER_DEVICE_ENDPOINT_ERROR_MESSAGE, response);
    }

    @Test
    public void unregisters_endpoint() {
        String endpoint = "--endpoint";
        when(preferencesUtil.getString(PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, null)).thenReturn(endpoint);
        when(apiClient.unRegisterDeviceEndpoint(CN_DEVICE_ID, endpoint)).thenReturn(generateSuccessResponse(200, ""));

        pushNotificationEndpointManager.unRegister();

        verify(apiClient).unRegisterDeviceEndpoint(CN_DEVICE_ID, endpoint);
    }

    @Test
    public void unregisters_provided_endpoint() {
        String endpoint = "--endpoint";
        when(apiClient.unRegisterDeviceEndpoint(CN_DEVICE_ID, endpoint)).thenReturn(generateSuccessResponse(200, ""));

        pushNotificationEndpointManager.unRegister(endpoint);

        verify(apiClient).unRegisterDeviceEndpoint(CN_DEVICE_ID, endpoint);
    }


    private Response generateSuccessResponse(int responseCode, Object responseData) {
        return Response.success(responseData, new okhttp3.Response.Builder()
                .code(responseCode)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }
}