package com.coinninja.coinkeeper.model.db;

import com.coinninja.coinkeeper.model.db.enums.AccountStatus;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class AccountTest {

    @Test
    public void populateStatusFromCn() {
        Account account = new Account();
        account.populateStatus("pending-verification");
        assertThat(account.getStatus(), equalTo(AccountStatus.PENDING_VERIFICATION));
    }

    @Test
    public void populateStatusFromCn_verified() {
        Account account = new Account();
        account.populateStatus("verified");
        assertThat(account.getStatus(), equalTo(AccountStatus.PENDING_VERIFICATION));
    }

    @Test
    public void populateStatusFromCn_unverified() {
        Account account = new Account();
        account.populateStatus("bogus");
        assertThat(account.getStatus(), equalTo(AccountStatus.UNVERIFIED));
    }

}