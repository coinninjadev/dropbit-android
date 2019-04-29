package com.coinninja.coinkeeper.model.db.enums;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BTCStateTest {

    @Test
    public void state_from_server_status() {
        assertThat(BTCState.from("expired"), equalTo(BTCState.EXPIRED));
        assertThat(BTCState.from("new"), equalTo(BTCState.UNFULFILLED));
        assertThat(BTCState.from("canceled"), equalTo(BTCState.CANCELED));
        assertThat(BTCState.from("completed"), equalTo(BTCState.FULFILLED));
        assertThat(BTCState.from("COMPLETED"), equalTo(BTCState.FULFILLED));
        assertThat(BTCState.from("tomfoolery"), equalTo(BTCState.UNFULFILLED));
    }
}