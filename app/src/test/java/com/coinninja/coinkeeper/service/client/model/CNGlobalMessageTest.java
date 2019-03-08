package com.coinninja.coinkeeper.service.client.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CNGlobalMessageTest {

    @Test
    public void getPublished_at_takes_epoch_seconds_value_returned_from_server_and_turns_into_milliseconds() {
        long timeFromServer = 1539515702l;
        long expectedTimeMS = 1539515702000L;
        CNGlobalMessage globalMessage = new CNGlobalMessage();
        globalMessage.published_at = timeFromServer;


        long millisecondsTime = globalMessage.getPublished_at();


        assertThat(millisecondsTime, equalTo(expectedTimeMS));
    }

    @Test
    public void getCreated_at_at_takes_epoch_seconds_value_returned_from_server_and_turns_into_milliseconds() {
        long timeFromServer = 1539515702l;
        long expectedTimeMS = 1539515702000L;
        CNGlobalMessage globalMessage = new CNGlobalMessage();
        globalMessage.created_at = timeFromServer;


        long millisecondsTime = globalMessage.getCreated_at();


        assertThat(millisecondsTime, equalTo(expectedTimeMS));
    }

    @Test
    public void getUpdated_at_at_at_takes_epoch_seconds_value_returned_from_server_and_turns_into_milliseconds() {
        long timeFromServer = 1539515702l;
        long expectedTimeMS = 1539515702000L;
        CNGlobalMessage globalMessage = new CNGlobalMessage();
        globalMessage.updated_at = timeFromServer;


        long millisecondsTime = globalMessage.getUpdated_at();


        assertThat(millisecondsTime, equalTo(expectedTimeMS));
    }
}