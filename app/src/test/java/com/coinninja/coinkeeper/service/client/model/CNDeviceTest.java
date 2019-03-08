package com.coinninja.coinkeeper.service.client.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CNDeviceTest {

    @Test
    public void convert_string_to_DevicePlatform_enum_for_android_test() {
        CNDevice cnDevice = new CNDevice();
        cnDevice.devicePlatform = "android";

        CNDevice.DevicePlatform platForm = cnDevice.getPlatform();

        assertThat(platForm, equalTo(CNDevice.DevicePlatform.ANDROID));
    }

    @Test
    public void convert_string_to_DevicePlatform_enum_for_ios_test() {
        CNDevice cnDevice = new CNDevice();
        cnDevice.devicePlatform = "ios";

        CNDevice.DevicePlatform platForm = cnDevice.getPlatform();

        assertThat(platForm, equalTo(CNDevice.DevicePlatform.IOS));
    }


    @Test
    public void when_device_platform_string_is_null_return_null() {
        CNDevice cnDevice = new CNDevice();
        cnDevice.devicePlatform = null;

        CNDevice.DevicePlatform platForm = cnDevice.getPlatform();

        assertThat(platForm, equalTo(null));
    }


    @Test
    public void when_device_platform_string_is_of_unknown_type_return_null() {
        CNDevice cnDevice = new CNDevice();
        cnDevice.devicePlatform = "windows phone";

        CNDevice.DevicePlatform platForm = cnDevice.getPlatform();

        assertThat(platForm, equalTo(null));
    }
}