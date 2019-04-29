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

import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class CNGlobalMessagesRunner implements Runnable {
    protected final static String TAG = CNGlobalMessagesRunner.class.getSimpleName();

    @Inject
    PreferencesUtil preferencesUtil;
    @Inject
    SignedCoinKeeperApiClient client;
    @Inject
    InternalNotificationHelper notificationHelper;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    CNLogger cnLogger;
    @Inject
    CNElasticSearch elasticSearch;


    @Inject
    public CNGlobalMessagesRunner(PreferencesUtil preferencesUtil, SignedCoinKeeperApiClient client, InternalNotificationHelper notificationHelper, LocalBroadCastUtil localBroadCastUtil, CNLogger cnLogger, CNElasticSearch elasticSearch) {
        this.preferencesUtil = preferencesUtil;
        this.client = client;
        this.notificationHelper = notificationHelper;
        this.localBroadCastUtil = localBroadCastUtil;
        this.cnLogger = cnLogger;
        this.elasticSearch = elasticSearch;
    }

    @Override
    public void run() {
        long lastMessageTime = preferencesUtil.getLong(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_DEFAULT_TIME);

        if (lastMessageTime != DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_DEFAULT_TIME) {
            elasticSearch.setPublishedAt(lastMessageTime);
        }

        Response response = client.getCNMessages(elasticSearch);

        if (!response.isSuccessful()) {
            logError(response);
            return;
        }

        List<CNGlobalMessage> cnGlobalMessages = (List<CNGlobalMessage>) response.body();

        if (cnGlobalMessages == null || cnGlobalMessages.isEmpty()) return;

        for (CNGlobalMessage cnGlobalMessage : cnGlobalMessages) {

            addMessageToNotification(cnGlobalMessage);

            long messagePublishedDate = cnGlobalMessage.getPublished_at();
            lastMessageTime = (messagePublishedDate > lastMessageTime) ? messagePublishedDate : lastMessageTime;
        }

        preferencesUtil.savePreference(DropbitIntents.PREFERENCES_LAST_CN_MESSAGES_TIME, lastMessageTime);
        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE);
    }

    private void addMessageToNotification(CNGlobalMessage cnGlobalMessage) {
        String serverUUID = cnGlobalMessage.getId();
        String message = cnGlobalMessage.getBody();
        MessageLevel level = convertMessageLevel(cnGlobalMessage.getLevel());
        InternalNotificationPriority priority = getPriority(cnGlobalMessage.getPriority());
        Uri clickAction = convertMessageUrl(cnGlobalMessage.getUrl());

        notificationHelper.addNotifications(serverUUID, clickAction, priority, message, level);
    }

    protected InternalNotificationPriority getPriority(int priority) {
        InternalNotificationPriority.PriorityConverter priorityConverter = new InternalNotificationPriority.PriorityConverter();
        return priorityConverter.convertToEntityProperty(priority);
    }

    private void logError(Response response) {
        String message = "|---- Unable to get CN Global Messages";
        cnLogger.logError(TAG, message, response);
    }

    private MessageLevel convertMessageLevel(String level) {
        MessageLevel.Converter converter = new MessageLevel.Converter();
        return converter.fromString(level);
    }

    private Uri convertMessageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        return Uri.parse(url);
    }
}
