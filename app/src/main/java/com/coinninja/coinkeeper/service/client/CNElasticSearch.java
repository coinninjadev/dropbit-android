package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.di.interfaces.BuildVersionName;
import com.coinninja.coinkeeper.util.DateUtil;
import com.coinninja.coinkeeper.util.Intents;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.inject.Inject;

public class CNElasticSearch {
    private final DateUtil dateUtil;

    String appVersion;
    String[] platforms;
    String publishedAt;

    @Inject
    public CNElasticSearch(@BuildVersionName String buildConfigVersionName, DateUtil dateUtil) {
        appVersion = buildConfigVersionName;
        platforms = new String[]{Intents.CN_API_ELASTIC_SEARCH_PLATFORM_ALL, Intents.CN_API_ELASTIC_SEARCH_PLATFORM_ANDROID};
        this.dateUtil = dateUtil;
    }

    public void setPublishedAt(Long publishedAtMills) {
        long graterThanMills = publishedAtMills + 1000l;
        setPublishedAt(dateUtil.format(graterThanMills));
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        JsonObject query = new JsonObject();

        addVersion(query);
        addPublishedTime(query);
        addPlatform(query);


        json.add(Intents.CN_API_ELASTIC_SEARCH_QUERY, query);
        return json;
    }

    private void addVersion(JsonObject query) {
        JsonObject script = new JsonObject();
        JsonObject script2 = new JsonObject();
        JsonObject params = new JsonObject();


        String id = Intents.CN_API_ELASTIC_SEARCH_SEMVER;

        params.addProperty(Intents.CN_API_ELASTIC_SEARCH_VERSION, removeVersionNameSuffix(appVersion));
        script2.add(Intents.CN_API_ELASTIC_SEARCH_PARAMS, params);
        script2.addProperty(Intents.CN_API_ELASTIC_SEARCH_ID, id);
        script.add(Intents.CN_API_ELASTIC_SEARCH_SCRIPT, script2);
        query.add(Intents.CN_API_ELASTIC_SEARCH_SCRIPT, script);
    }

    private void addPublishedTime(JsonObject query) {
        if (publishedAt == null || publishedAt.isEmpty()) return;

        JsonObject publishedTime = new JsonObject();
        JsonObject range = new JsonObject();

        publishedTime.addProperty(Intents.CN_API_ELASTIC_SEARCH_GREATER_THAN, publishedAt);
        range.add(Intents.CN_API_ELASTIC_SEARCH_PUBLISH_TIME, publishedTime);
        query.add(Intents.CN_API_ELASTIC_SEARCH_RANGE, range);
    }

    private void addPlatform(JsonObject query) {
        JsonObject terms = new JsonObject();
        JsonArray platformItems = new JsonArray();

        for (String devicePlatform : platforms) {
            platformItems.add(devicePlatform);
        }

        terms.add(Intents.CN_API_ELASTIC_SEARCH_PLATFORM, platformItems);
        query.add(Intents.CN_API_ELASTIC_SEARCH_TERMS, terms);
    }

    private String removeVersionNameSuffix(String appVersion) {
        appVersion = appVersion.replace("-staging", "");
        return appVersion;
    }
}
