package com.coinninja.coinkeeper.model.dto;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

@RunWith(RobolectricTestRunner.class)
public class CompletedInviteDTOTest {

    private CompletedInviteDTO createDto() {
        Contact contact = new Contact(new PhoneNumber("+13305552222"), "Joe Blow", false);
        PendingInviteDTO pendingInviteDTO = new PendingInviteDTO(contact,
                340000L,
                100000L,
                100L,
                "--memo--",
                true
        );
        InvitedContact invitedContact = new InvitedContact(
                "--cn-id--",
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "",
                contact.getHash(),
                "",
                ""
        );

        return new CompletedInviteDTO(pendingInviteDTO, invitedContact);
    }

    @Test
    public void provides_access_to_cn_server_id_of_invite() {
        assertThat(createDto().getCnId(), equalTo("--cn-id--"));
    }

    @Test
    public void false_when_no_server_id() {
        CompletedInviteDTO dto = createDto();
        assertTrue(dto.hasCnId());

        dto.getInvitedContact().setId("");
        assertFalse(dto.hasCnId());
    }
}