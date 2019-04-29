package com.coinninja.coinkeeper.cn.service.runner;

import android.net.Uri;

import com.coinninja.coinkeeper.model.db.enums.InternalNotificationPriority;
import com.coinninja.coinkeeper.model.db.enums.MessageLevel;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.service.client.CNElasticSearch;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNGlobalMessage;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CNGlobalMessagesRunnerTest {

    private PreferencesUtil mockPreferencesUtil;
    private SignedCoinKeeperApiClient mockClient;
    private InternalNotificationHelper mockNotificationHelper;
    private CNLogger mockCNLogger;

    private CNGlobalMessagesRunner cnGlobalMessagesRunner;
    private CNElasticSearch mockCNElasticSearch;


    @Before
    public void setUp() throws Exception {
        mockPreferencesUtil = mock(PreferencesUtil.class);
        mockClient = mock(SignedCoinKeeperApiClient.class);
        mockNotificationHelper = mock(InternalNotificationHelper.class);
        LocalBroadCastUtil mockLocalBroadCastUtil = mock(LocalBroadCastUtil.class);
        mockCNLogger = mock(CNLogger.class);

        mockCNElasticSearch = mock(CNElasticSearch.class);
        cnGlobalMessagesRunner = new CNGlobalMessagesRunner(mockPreferencesUtil, mockClient, mockNotificationHelper, mockLocalBroadCastUtil, mockCNLogger, mockCNElasticSearch);
    }

    @After
    public void tearDown() throws Exception {
        mockPreferencesUtil = null;
        mockClient = null;
        mockNotificationHelper = null;
        mockCNLogger = null;
        cnGlobalMessagesRunner = null;
        mockCNElasticSearch = null;
    }

    @Test
    public void use_saved_pref_value_to_query_for_servers_test() {
        long sampleLastSeenMessagePublishedTime = 1539035083519l;
        when(mockPreferencesUtil.getLong(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, 0)).thenReturn(sampleLastSeenMessagePublishedTime);
        setUpGoodResponse();

        cnGlobalMessagesRunner.run();

        verify(mockClient).getCNMessages(mockCNElasticSearch);
    }

    @Test
    public void use_no_pref_value_to_query_for_servers_when_pref_is_0_test() {
        long sampleLastSeenMessagePublishedTime = 0l;
        when(mockPreferencesUtil.getLong(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, 0)).thenReturn(sampleLastSeenMessagePublishedTime);
        setUpGoodResponse();

        cnGlobalMessagesRunner.run();

        verify(mockClient).getCNMessages(mockCNElasticSearch);
    }

    @Test
    public void add_all_messages_to_notification_test() {
        when(mockPreferencesUtil.getLong(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, 0)).thenReturn(0l);
        setUpGoodResponse();

        cnGlobalMessagesRunner.run();

        verify(mockNotificationHelper, times(2)).addNotifications("some =--- UUID", Uri.parse("https://coinninja.com/"), InternalNotificationPriority._0_HIGHEST, "some =--- message", MessageLevel.INFO);
    }

    @Test
    public void add_missing_http_to_url_from_server_test() {
        when(mockPreferencesUtil.getLong(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, 0)).thenReturn(0l);
        setUpGoodResponseMissing_http();

        cnGlobalMessagesRunner.run();

        verify(mockNotificationHelper).addNotifications("some =--- UUID", Uri.parse("https://coinninja.com/"), InternalNotificationPriority._0_HIGHEST, "some =--- message", MessageLevel.INFO);
    }

    @Test
    public void log_error_do_not_try_to_add_messages_test() {
        when(mockPreferencesUtil.getLong(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, 0)).thenReturn(0l);
        Response badResponse = getBadResponse(401);
        when(mockClient.getCNMessages(mockCNElasticSearch)).thenReturn(badResponse);

        cnGlobalMessagesRunner.run();

        verify(mockCNLogger).logError(CNGlobalMessagesRunner.TAG, "|---- Unable to get CN Global Messages", badResponse);
        verify(mockNotificationHelper, times(0)).addNotifications(any(), any(), any(), any(), any());
    }

    @Test
    public void save_latest_cn_messages_publish_time_to_shared_pref_test() {
        long sampleLastSeenMessagePublishedTime = 0l;
        when(mockPreferencesUtil.getLong(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, 0)).thenReturn(sampleLastSeenMessagePublishedTime);
        setUpGoodResponse();

        cnGlobalMessagesRunner.run();

        verify(mockPreferencesUtil).savePreference(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, 6);
    }

    @Test
    public void set_time_to_cn_elastic_search_test() {
        long sampleLastSeenMessagePublishedTime = 1000l;
        when(mockPreferencesUtil.getLong(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, 0)).thenReturn(sampleLastSeenMessagePublishedTime);
        setUpGoodResponse();

        cnGlobalMessagesRunner.run();

        verify(mockCNElasticSearch).setPublishedAt(sampleLastSeenMessagePublishedTime);
    }

    @Test
    public void do_not_set_time_to_cn_elastic_search_is_pref_is_0_test() {
        long sampleLastSeenMessagePublishedTime = 0l;
        setUpGoodResponse();
        when(mockPreferencesUtil.getLong(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_DEFAULT_TIME)).thenReturn(sampleLastSeenMessagePublishedTime);

        cnGlobalMessagesRunner.run();

        verify(mockCNElasticSearch, times(0)).setPublishedAt(sampleLastSeenMessagePublishedTime);
    }

    @Test
    public void save_latest_cn_messages_publish_time_to_shared_pref_only_if_higher_value_test() {
        long sampleLastSeenMessagePublishedTime = 100l;
        when(mockPreferencesUtil.getLong(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, 0)).thenReturn(sampleLastSeenMessagePublishedTime);
        setUpGoodResponse();

        cnGlobalMessagesRunner.run();

        verify(mockPreferencesUtil).savePreference(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, 100);
    }

    @Test
    public void priorities_for_server_level_0_test() {
        int sampleServerLevel = 0;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._0_HIGHEST));
    }

    @Test
    public void priorities_for_server_level_1_test() {
        int sampleServerLevel = 1;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._1));
    }

    @Test
    public void priorities_for_server_level_2_test() {
        int sampleServerLevel = 2;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._2));
    }

    @Test
    public void priorities_for_server_level_3_test() {

        int sampleServerLevel = 3;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._3));
    }

    @Test
    public void priorities_for_server_level_4_test() {

        int sampleServerLevel = 4;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._4));
    }

    @Test
    public void priorities_for_server_level_5_test() {
        int sampleServerLevel = 5;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._5));
    }

    @Test
    public void priorities_for_server_level_6_test() {
        int sampleServerLevel = 6;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._6));
    }

    @Test
    public void priorities_for_server_level_7_test() {
        int sampleServerLevel = 7;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._7));
    }

    @Test
    public void priorities_for_server_level_8_test() {
        int sampleServerLevel = 8;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._8));
    }

    @Test
    public void priorities_for_server_level_9_test() {
        int sampleServerLevel = 9;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._9));
    }

    @Test
    public void priorities_for_server_level_10_test() {
        int sampleServerLevel = 10;

        InternalNotificationPriority priority = cnGlobalMessagesRunner.getPriority(sampleServerLevel);

        assertThat(priority, equalTo(InternalNotificationPriority._10_LOWEST));
    }

    private void setUpGoodResponse() {
        String serverUUID = "some =--- UUID";
        String message = "some =--- message";
        String level = "info";
        List<CNGlobalMessage> data = get2GoodMessages(serverUUID, message, level, 6);
        when(mockClient.getCNMessages(mockCNElasticSearch)).thenReturn(getResponse(data));
    }

    private void setUpGoodResponseMissing_http() {
        String serverUUID = "some =--- UUID";
        String message = "some =--- message";
        String level = "info";

        List<CNGlobalMessage> cnGlobalMessages = new ArrayList<>();

        CNGlobalMessage globalMessage1 = mock(CNGlobalMessage.class);
        when(globalMessage1.getId()).thenReturn(serverUUID);
        when(globalMessage1.getBody()).thenReturn(message);
        when(globalMessage1.getLevel()).thenReturn(level);
        when(globalMessage1.getPublished_at()).thenReturn(6l);
        when(globalMessage1.getUrl()).thenReturn("coinninja.com");

        cnGlobalMessages.add(globalMessage1);

        when(mockClient.getCNMessages(mockCNElasticSearch)).thenReturn(getResponse(cnGlobalMessages));
    }


    private Response getResponse(List<CNGlobalMessage> responseData) {
        return Response.success(responseData, new okhttp3.Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }


    private Response getBadResponse(int code) {
        return Response.error(code, ResponseBody.create(MediaType.parse("application/json"),
                "[]"));
    }

    public List<CNGlobalMessage> get2GoodMessages(String serverUUID, String message, String level, long publishedAt) {
        List<CNGlobalMessage> cnGlobalMessages = new ArrayList<>();

        CNGlobalMessage globalMessage1 = mock(CNGlobalMessage.class);
        when(globalMessage1.getId()).thenReturn(serverUUID);
        when(globalMessage1.getBody()).thenReturn(message);
        when(globalMessage1.getLevel()).thenReturn(level);
        when(globalMessage1.getPublished_at()).thenReturn(publishedAt);
        when(globalMessage1.getUrl()).thenReturn("https://coinninja.com/");

        CNGlobalMessage globalMessage2 = mock(CNGlobalMessage.class);
        when(globalMessage2.getId()).thenReturn(serverUUID);
        when(globalMessage2.getBody()).thenReturn(message);
        when(globalMessage2.getLevel()).thenReturn(level);
        when(globalMessage2.getPublished_at()).thenReturn(publishedAt);
        when(globalMessage2.getUrl()).thenReturn("https://coinninja.com/");


        cnGlobalMessages.add(globalMessage1);
        cnGlobalMessages.add(globalMessage2);

        return cnGlobalMessages;
    }
}