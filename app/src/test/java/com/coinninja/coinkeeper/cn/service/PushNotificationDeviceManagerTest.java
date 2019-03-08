package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNDevice;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class PushNotificationDeviceManagerTest {
    private String CN_DEVICE_ID = "158a4cf9-0362-4636-8c68-ed7a98a7f345";

    private String JSON = "{\n" +
            "  \"id\": \"158a4cf9-0362-4636-8c68-ed7a98a7f345\",\n" +
            "  \"created_at\": 1531921356,\n" +
            "  \"updated_at\": 1531921355,\n" +
            "  \"application\": \"DropBit\",\n" +
            "  \"platform\": \"android\",\n" +
            "  \"uuid\": \"998207d6-5b1e-47c9-84e9-895f52a1b455\"\n" +
            "}";

    public String UUID = "-uuid-";
    private Gson gson = new Gson();
    CNDevice device = gson.fromJson(JSON, CNDevice.class);

    @Mock
    CNLogger logger;

    @Mock
    PreferencesUtil preferencesUtil;

    @Mock
    SignedCoinKeeperApiClient apiClient;

    @InjectMocks
    PushNotificationDeviceManager pushNotificationDeviceManager;

    @Before
    public void setUp() {
        when(apiClient.createCNDevice(UUID)).thenReturn(Response.success(device));
    }

    @After
    public void tearDown() {
        CN_DEVICE_ID = null;
        JSON = null;
        UUID = null;
        gson = null;
        device = null;
        logger = null;
        preferencesUtil = null;
        apiClient = null;
        pushNotificationDeviceManager = null;
    }

    @Test
    public void saves_server_device_id() {
        String id = "158a4cf9-0362-4636-8c68-ed7a98a7f345";
        CNDevice device = new CNDevice();
        device.setId(id);

        pushNotificationDeviceManager.saveDevice(device);

        verify(preferencesUtil).
                savePreference(PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID, id);
    }

    @Test
    public void provides_device_id() {
        when(preferencesUtil.getString(
                PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID, null))
                .thenReturn(CN_DEVICE_ID);

        assertThat(pushNotificationDeviceManager.getDeviceId(), equalTo(CN_DEVICE_ID));
    }

    @Test
    public void registers_device_with_coin_ninja() {
        pushNotificationDeviceManager.createDevice(UUID);

        verify(apiClient).createCNDevice(UUID);
    }

    @Test
    public void saves_device_registration_on_success_code_of_200() {
        when(apiClient.createCNDevice(UUID)).thenReturn(generateSuccessResponse(200, device));

        pushNotificationDeviceManager.createDevice(UUID);

        verify(preferencesUtil).savePreference(
                PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID,
                CN_DEVICE_ID);
    }

    @Test
    public void saves_device_registration_on_success_code_of_201() {
        when(apiClient.createCNDevice(UUID)).thenReturn(generateSuccessResponse(201, device));

        pushNotificationDeviceManager.createDevice(UUID);

        verify(preferencesUtil).savePreference(
                PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID,
                CN_DEVICE_ID);
    }

    @Test
    public void returns_true_when_device_created() {
        when(apiClient.createCNDevice(UUID)).thenReturn(generateSuccessResponse(201, device));

        assertTrue(pushNotificationDeviceManager.createDevice(UUID));
    }

    @Test
    public void logs_unsuccessful_responses() {
        ResponseBody body = ResponseBody.create(MediaType.parse("text"), "der");
        Response response = Response.error(500, body);
        when(apiClient.createCNDevice(UUID)).thenReturn(response);

        pushNotificationDeviceManager.createDevice(UUID);

        verify(preferencesUtil, times(0)).savePreference(
                eq(PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID), anyString());
        verify(logger).logError(PushNotificationDeviceManager.TAG,
                PushNotificationDeviceManager.DEVICE_REGISTRATION_ERROR_MESSAGE, response);
        assertFalse(pushNotificationDeviceManager.createDevice(UUID));
    }

    private Response generateSuccessResponse(int responseCode, Object responseData) {
        return Response.success(responseData, new okhttp3.Response.Builder()
                .code(responseCode)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }

    @Test
    public void removes_cn_device_locally() {

        pushNotificationDeviceManager.removeCNDevice();

        verify(preferencesUtil).removePreference(PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID);
    }
}