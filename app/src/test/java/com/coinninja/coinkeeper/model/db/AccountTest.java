package com.coinninja.coinkeeper.model.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountTest {

    @Test
    public void populateStatusFromCn() {
        Account account = new Account();
        account.populateStatus("pending-verification");
        assertThat(account.getStatus(), equalTo(Account.Status.PENDING_VERIFICATION));
    }

    @Test
    public void populateStatusFromCn_verified() {
        Account account = new Account();
        account.populateStatus("verified");
        assertThat(account.getStatus(), equalTo(Account.Status.PENDING_VERIFICATION));
    }

    @Test
    public void populateStatusFromCn_unverified() {
        Account account = new Account();
        account.populateStatus("bogus");
        assertThat(account.getStatus(), equalTo(Account.Status.UNVERIFIED));
    }

}