package com.coinninja.coinkeeper.twitter

import app.dropbit.twitter.model.TwitterUser
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertNull
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class MyTwitterProfileTest {

    private fun createProfile(): MyTwitterProfile {
        val profile = MyTwitterProfile(mock(), mock())
        whenever(profile.twitter.hasTwitterEnabled).thenReturn(true)
        return profile
    }

    @Test
    fun does_not_load_twitter_identity_when_not_verified() {
        runBlocking {
            val profile = createProfile()
            whenever(profile.dropbitAccountHelper.isTwitterVerified).thenReturn(false)

            val user = profile.loadMyProfile()
            assertNull(user)

            verify(profile.twitter, times(0)).me()
        }
    }

    @Test
    fun loads_profile_when_one_is_not_cached() {
        runBlocking {
            val twitterUser = mock<TwitterUser>()
            val profile = createProfile()
            whenever(profile.twitter.me()).thenReturn(twitterUser)
            whenever(profile.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
            profile.myUser = null

            val user = profile.loadMyProfile()
            assertThat(user, equalTo(twitterUser))

            verify(profile.twitter).me()
        }
    }

    @Test
    fun uses_cached_profile_when_already_loaded() {
        runBlocking {
            val profile = createProfile()
            val twitterUser = mock<TwitterUser>()
            whenever(profile.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
            profile.myUser = twitterUser

            val user = profile.loadMyProfile()
            assertThat(user, equalTo(twitterUser))

            verify(profile.twitter, times(0)).me()
        }
    }
}