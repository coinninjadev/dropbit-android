package com.coinninja.coinkeeper.util;

import org.junit.Before;
import org.junit.Test;

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
}