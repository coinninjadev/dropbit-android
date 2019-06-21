package com.coinninja.coinkeeper.model.dto

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingInviteDTOTest {

    @Test
    fun false_when_no_memo() {
        val pendingInviteDTO = PendingInviteDTO(Identity(
                IdentityType.PHONE, "+13305551111", "", ""),
                0L, 0L, 0L, "", false, "")
        assertFalse(pendingInviteDTO.hasMemo())

        pendingInviteDTO.memo = "--memo--"
        assertTrue(pendingInviteDTO.hasMemo())
        pendingInviteDTO.memo = ""
        assertFalse(pendingInviteDTO.hasMemo())
    }


}