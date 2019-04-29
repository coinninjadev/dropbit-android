package com.coinninja.coinkeeper.util;

import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DateUtilTest {

    private DateUtil dateUtil;

    @Before
    public void setUp() {
        dateUtil = new DateUtil();
    }

    @Test
    public void formats_time_appropriately() {
        long millis = 1525909522900L;

        assertThat(dateUtil.format(millis), equalTo("2018-05-09T23:45:22Z"));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void RFC3339_TO_EPOCH_TIME_test() {
        long expectedMilliseconds = 1525882145294L;

        String serverTime_AsResponsesString = "2018-05-09T16:09:05.294Z";

        long time = DateUtil.RFC3339_TO_EPOCH_TIME(serverTime_AsResponsesString);

        assertThat(time, equalTo(expectedMilliseconds));
    }

    @Test
    public void RFC3339_TO_EPOCH_TIME_back_test() throws Exception {
        long expectedMilliseconds = 1530680365000L;

        String serverTime_AsResponsesString = "2018-07-04T04:59:25Z";

        long time = DateUtil.RFC3339_TO_EPOCH_TIME(serverTime_AsResponsesString);

        assertThat(time, equalTo(expectedMilliseconds));
    }
}