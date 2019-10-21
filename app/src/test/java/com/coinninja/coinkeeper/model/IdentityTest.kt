package com.coinninja.coinkeeper.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IdentityTest {

    @Test
    fun provides_supplied_hash_when_not_null() {
        val identity = Identity(IdentityType.PHONE, "+13305551111", "--hash--")

        assertThat(identity.hashForType).isEqualTo("--hash--")
    }

    @Test
    fun hashes_phone_number_identity_when_null() {
        val identity = Identity(IdentityType.PHONE, "+13305551111")

        assertThat(identity.hashForType).isEqualTo("710c3ec37d3bbab4d9b140656ea8ab28d14bad091e12b912dc73d0fbcd78664d")
    }

    @Test
    fun returns_twitter_identity_for_hash() {
        val identity = Identity(IdentityType.TWITTER, "10293941201292")

        assertThat(identity.hashForType).isEqualTo(identity.value)
    }

    @Test
    fun returns_twitter_handle_correctly() {
        var identity = Identity(IdentityType.TWITTER, "10293941201292", handle = "@JOE")

        assertThat(identity.displayableHandle).isEqualTo("@JOE")

        identity = Identity(IdentityType.TWITTER, "10293941201292", handle = "JOE")
        assertThat(identity.displayableHandle).isEqualTo("@JOE")
    }
}