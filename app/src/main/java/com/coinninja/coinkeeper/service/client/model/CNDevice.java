package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

import static com.coinninja.coinkeeper.util.Intents.CN_API_CREATE_DEVICE_APPLICATION_KEY;
import static com.coinninja.coinkeeper.util.Intents.CN_API_CREATE_DEVICE_PLATFORM_ANDROID;
import static com.coinninja.coinkeeper.util.Intents.CN_API_CREATE_DEVICE_PLATFORM_IOS;
import static com.coinninja.coinkeeper.util.Intents.CN_API_CREATE_DEVICE_PLATFORM_KEY;

public class CNDevice {
    String id;

    @SerializedName("created_at")
    Long createdDate;

    @SerializedName("updated_at")
    Long updatedDate;

    @SerializedName(CN_API_CREATE_DEVICE_APPLICATION_KEY)
    String applicationName;

    @SerializedName(CN_API_CREATE_DEVICE_PLATFORM_KEY)
    String devicePlatform;

    String uuid;

    public String getId() {
        return id;
    }

    public Long getCreatedDate() {
        return createdDate * 1000;
    }

    public Long getUpdatedDate() {
        return updatedDate * 1000;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public DevicePlatform getPlatform() {
        return DevicePlatform.fromString(devicePlatform);
    }

    public String getUuid() {
        return uuid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public enum DevicePlatform {
        IOS(CN_API_CREATE_DEVICE_PLATFORM_IOS), ANDROID(CN_API_CREATE_DEVICE_PLATFORM_ANDROID);

        private final String platformKey;

        DevicePlatform(String platformKey) {
            this.platformKey = platformKey;
        }

        public static DevicePlatform fromString(String platform) {
            if (platform == null || platform.isEmpty()) return null;

            for (DevicePlatform devicePlatform : DevicePlatform.values()) {
                if (devicePlatform.platformKey.contentEquals(platform)) {
                    return devicePlatform;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return platformKey;
        }
    }
}
