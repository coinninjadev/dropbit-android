package com.coinninja.coinkeeper.model.dto

import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.service.client.model.InvitedContact
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CompletedInviteDTOTest {

    private fun createDto(): CompletedInviteDTO {
        val identity = Identity(IdentityType.PHONE, "+13305552222", "--hash--", "Joe Blow")
        val invitedContact = InvitedContact(
                "--cn-id--",
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "",
                "--hash--",
                "",
                ""
        )

        return CompletedInviteDTO(
                identity,
                340000L,
                100000L,
                100L,
                "--memo--",
                true, "",
                invitedContact)
    }

    @Test
    fun provides_access_to_cn_server_id_of_invite() {
        assertThat<String>(createDto().cnId, equalTo("--cn-id--"))
    }

    @Test
    fun false_when_no_server_id() {
        val dto = createDto()
        assertTrue(dto.hasCnId())

        dto.invitedContact!!.id = ""
        assertFalse(dto.hasCnId())
    }
}