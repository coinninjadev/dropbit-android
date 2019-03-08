package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.DateUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CNElasticSearchTest {
    private DateUtil dateUtil;

    @Before
    public void setUp() throws Exception {
        dateUtil = mock(DateUtil.class);
        when(dateUtil.format(anyLong())).thenReturn("2006-01-02T15:04:05Z07:00");
    }

    @Test
    public void test_VERSION_value_from_coin_ninja_api_elastic_search_example_matches_our_cn_elastic_object_test() {
        String buildConfigVersionName = "1.0.1";
        CNElasticSearch elasticSearch = new CNElasticSearch(buildConfigVersionName, dateUtil);


        JsonObject elasticSearchJson = (JsonObject) new JsonParser().parse(exampleElasticSearchBody);
        JsonObject cnElasticSearch = elasticSearch.toJson();
        String apiExampleResult = parseVersionCodeFromJson(elasticSearchJson);
        String cnElasticSearchResult = parseVersionCodeFromJson(cnElasticSearch);


        assertThat(apiExampleResult, equalTo(cnElasticSearchResult));
    }

    @Test
    public void test_SEMVER_value_from_coin_ninja_api_elastic_search_example_matches_our_cn_elastic_object_test() {
        String buildConfigVersionName = "1.0.1";
        CNElasticSearch elasticSearch = new CNElasticSearch(buildConfigVersionName, dateUtil);


        JsonObject elasticSearchJson = (JsonObject) new JsonParser().parse(exampleElasticSearchBody);
        JsonObject cnElasticSearch = elasticSearch.toJson();
        String apiExampleResult = parseSemverFromJson(elasticSearchJson);
        String cnElasticSearchResult = parseSemverFromJson(cnElasticSearch);


        assertThat(apiExampleResult, equalTo(cnElasticSearchResult));
    }

    @Test
    public void test_PUBLISHED_AT_value_from_coin_ninja_api_elastic_search_example_matches_our_cn_elastic_object_test() {
        String buildConfigVersionName = "1.0.1";
        String publishTime = "2006-01-02T15:04:05Z07:00";
        CNElasticSearch elasticSearch = new CNElasticSearch(buildConfigVersionName, dateUtil);
        elasticSearch.setPublishedAt(publishTime);


        JsonObject elasticSearchJson = (JsonObject) new JsonParser().parse(exampleElasticSearchBody);
        JsonObject cnElasticSearch = elasticSearch.toJson();
        String apiExampleResult = parsePublishTimeFromJson(elasticSearchJson);
        String cnElasticSearchResult = parsePublishTimeFromJson(cnElasticSearch);


        assertThat(apiExampleResult, equalTo(cnElasticSearchResult));
    }

    @Test
    public void test_PLATFORM_value_from_coin_ninja_api_elastic_search_example_matches_our_cn_elastic_object_test() {
        String buildConfigVersionName = "1.0.1";
        String publishTime = "2006-01-02T15:04:05Z07:00";
        CNElasticSearch elasticSearch = new CNElasticSearch(buildConfigVersionName, dateUtil);
        elasticSearch.setPublishedAt(publishTime);


        JsonObject elasticSearchJson = (JsonObject) new JsonParser().parse(exampleElasticSearchBody);
        JsonObject cnElasticSearch = elasticSearch.toJson();
        String[] apiExampleResult = parsePlatformFromJson(elasticSearchJson);
        String[] cnElasticSearchResult = parsePlatformFromJson(cnElasticSearch);


        assertThat(apiExampleResult[0], equalTo(cnElasticSearchResult[0]));
        assertThat(apiExampleResult[1], equalTo(cnElasticSearchResult[1]));
    }

    @Test
    public void test_PUBLISHED_AT_NULL_value_from_coin_ninja_api_elastic_search_example_matches_our_cn_elastic_object_test() {
        String buildConfigVersionName = "1.0.1";
        String publishTime = null;
        CNElasticSearch elasticSearch = new CNElasticSearch(buildConfigVersionName, dateUtil);
        elasticSearch.setPublishedAt(publishTime);

        JsonObject cnElasticSearch = elasticSearch.toJson();
        String cnElasticSearchResult = parsePublishTimeFromJson(cnElasticSearch);


        assertNull(cnElasticSearchResult);
    }

    private String parsePublishTimeFromJson(JsonObject elasticSearchJson) {
        JsonObject query = elasticSearchJson.getAsJsonObject("query");
        JsonObject range = query.getAsJsonObject("range");
        if (range == null) {
            return null;
        } else {
            JsonObject publishedAt = range.getAsJsonObject("published_at");
            JsonElement graterThanTime = publishedAt.get("gt");
            String publishTime = graterThanTime.getAsString();
            return publishTime;
        }
    }

    private String parseSemverFromJson(JsonObject elasticSearchJson) {
        JsonObject query = elasticSearchJson.getAsJsonObject("query");
        JsonObject script = query.getAsJsonObject("script");
        JsonObject innerScript = script.getAsJsonObject("script");
        JsonElement idElement = innerScript.get("id");
        String id = idElement.getAsString();
        return id;
    }

    private String[] parsePlatformFromJson(JsonObject elasticSearchJson) {
        JsonObject query = elasticSearchJson.getAsJsonObject("query");
        JsonObject terms = query.getAsJsonObject("terms");
        JsonArray platformJsonElement = terms.getAsJsonArray("platform");
        return new String[]{platformJsonElement.get(0).getAsString(), platformJsonElement.get(1).getAsString()};
    }

    private String parseVersionCodeFromJson(JsonObject elasticSearchJson) {
        JsonObject query = elasticSearchJson.getAsJsonObject("query");
        JsonObject script = query.getAsJsonObject("script");
        JsonObject innerScript = script.getAsJsonObject("script");
        JsonObject params = innerScript.getAsJsonObject("params");
        JsonElement versionJsonElement = params.get("version");
        String version = versionJsonElement.getAsString();
        return version;
    }


    String exampleElasticSearchBody = "{\n" +
            "  \"query\": {\n" +
            "    \"range\": {\n" +
            "      \"published_at\": {\n" +
            "        \"gt\": \"2006-01-02T15:04:05Z07:00\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"script\": {\n" +
            "      \"script\": {\n" +
            "        \"id\": \"semver\",\n" +
            "        \"params\": {\n" +
            "          \"version\": \"1.0.1\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"term\": {\n" +
            "      \"level\": \"warn\"\n" +
            "    },\n" +
            "    \"terms\": {\n" +
            "      \"platform\": [\n" +
            "        \"all\",\n" +
            "        \"android\"\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            "}";
}