package com.coinninja.coinkeeper.twitter

import app.dropbit.twitter.Twitter
import app.dropbit.twitter.model.TwitterUser
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertNull
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times

class MyTwitterProfileTest {

    @Test
    fun `does not load twitter identity when not verified`() {
        val twitter = mock(Twitter::class.java)
        val myTwitterProfile = MyTwitterProfile(twitter)
        whenever(twitter.hasTwitterEnabled).thenReturn(false)

        runBlocking {
            val user = myTwitterProfile.loadMyProfile()
            assertNull(user)
        }

        verify(twitter, times(0)).me()
    }

    @Test
    fun `loads profile when one is not cached`() {
        val twitter = mock(Twitter::class.java)
        val myTwitterProfile = MyTwitterProfile(twitter)
        val twitterUser = mock(TwitterUser::class.java)
        whenever(twitter.hasTwitterEnabled).thenReturn(true)
        whenever(twitter.me()).thenReturn(twitterUser)
        myTwitterProfile.myUser = null

        runBlocking {
            val user = myTwitterProfile.loadMyProfile()
            assertThat(user, equalTo(twitterUser))
        }

        verify(twitter).me()
    }

    @Test
    fun `uses cached profile when already loaded`() {
        val twitter = mock(Twitter::class.java)
        val myTwitterProfile = MyTwitterProfile(twitter)
        val twitterUser = mock(TwitterUser::class.java)
        whenever(twitter.hasTwitterEnabled).thenReturn(true)
        myTwitterProfile.myUser = twitterUser

        runBlocking {
            val user = myTwitterProfile.loadMyProfile()
            assertThat(user, equalTo(twitterUser))
        }

        verify(twitter, times(0)).me()
    }
}