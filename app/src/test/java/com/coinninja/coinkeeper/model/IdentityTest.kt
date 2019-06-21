package com.coinninja.coinkeeper.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IdentityTest {

    @Test
    fun `provides supplied hash when not null`() {
        val identity = Identity(IdentityType.PHONE, "+13305551111", "--hash--")

        assertThat(identity.hashForType, equalTo("--hash--"))
    }

    @Test
    fun `hashes phone number identity when null`() {
        val identity = Identity(IdentityType.PHONE, "+13305551111")

        assertThat(identity.hashForType, equalTo("710c3ec37d3bbab4d9b140656ea8ab28d14bad091e12b912dc73d0fbcd78664d"))
    }

    @Test
    fun `returns twitter identity for hash`() {
        val identity = Identity(IdentityType.TWITTER, "10293941201292")

        assertThat(identity.hashForType, equalTo(identity.value))
    }
}