package com.coinninja.coinkeeper.model.dto;

import com.coinninja.coinkeeper.service.client.model.Contact;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class PendingInviteDTOTest {

    @Test
    public void false_when_no_memo() {
        PendingInviteDTO pendingInviteDTO = new PendingInviteDTO(new Contact(), 0L, 0L, 0L, "", false);
        assertFalse(pendingInviteDTO.hasMemo());

        pendingInviteDTO.setMemo("--memo--");
        assertTrue(pendingInviteDTO.hasMemo());
    }


}