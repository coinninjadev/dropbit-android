package com.coinninja.coinkeeper.util;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DateFormatUtilTest {
    private DateFormatUtil dateFormatServer;

    String serverTime_AsResponsesString = "2018-05-09T16:09:05.294Z";//this is GMT

    @Before
    public void setUp() throws Exception {
        dateFormatServer = new DateFormatUtil(DateFormatUtil.SERVER_DATE_FORMAT, false);
    }

    @Test
    public void data_responses_from_to_milliseconds() throws ParseException {
        long expectedMilliseconds = 1525882145294L;

        Long milliseconds = dateFormatServer.parseDateToMilliseconds(serverTime_AsResponsesString);

        assertThat(milliseconds, equalTo(expectedMilliseconds));
    }

    @Test
    public void data_responses_from_to_display_in_app() {
        String expectedDisplay = "May 9, 2018 04:09pm";

        Long milliseconds = dateFormatServer.parseDateToMilliseconds(serverTime_AsResponsesString);
        DateFormatUtil dateFormatDisplayToUsers = new DateFormatUtil(DateFormatUtil.DEFAULT_DATE_FORMAT, false);
        String showTime = dateFormatDisplayToUsers.formatTime(milliseconds);

        assertThat(showTime, equalTo(expectedDisplay));
    }
}