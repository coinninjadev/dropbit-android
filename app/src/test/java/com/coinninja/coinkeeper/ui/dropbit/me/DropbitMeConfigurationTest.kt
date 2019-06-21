package com.coinninja.coinkeeper.ui.dropbit.me

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.twitter.MyTwitterProfile
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class DropbitMeConfigurationTest {

    fun createConfiguration(): DropbitMeConfiguration {
        return DropbitMeConfiguration(
                Uri.parse("https://dropbit.me"),
                mock(DropbitAccountHelper::class.java), mock(MyTwitterProfile::class.java))
    }

    @Test
    fun `schedules showing because of newly verified account`() {
        val configuration = createConfiguration()

        configuration.setInitialVerification()

        assertTrue(configuration.shouldShowWhenPossible())
        assertTrue(configuration.isNewlyVerified)
    }

    @Test
    fun `schedules showing when when available from other sources`() {
        val configuration = createConfiguration()

        configuration.showWhenPossible()

        assertTrue(configuration.shouldShowWhenPossible())
        assertFalse(configuration.isNewlyVerified)
    }

    @Test
    fun `acknowledging state clears state`() {
        val configuration = createConfiguration()

        configuration.showWhenPossible()
        configuration.acknowledge()
        assertFalse(configuration.shouldShowWhenPossible())
        assertFalse(configuration.isNewlyVerified)

        configuration.setInitialVerification()
        configuration.acknowledge()
        assertFalse(configuration.shouldShowWhenPossible())
        assertFalse(configuration.isNewlyVerified)

    }

    @Test
    fun `proxies account verification state`() {
        val configuration = createConfiguration()

        whenever(configuration.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true).thenReturn(false)

        assertTrue(configuration.hasVerifiedAccount())
        assertFalse(configuration.hasVerifiedAccount())
    }

    @Test
    fun `notifies observer that showing requested`() {
        val observer = mock(OnViewDropBitMeViewRequestedObserver::class.java)
        val configuration = createConfiguration()

        configuration.setOnViewDropBitMeViewRequestedObserver(observer)
        configuration.showWhenPossible()

        verify(observer).onShowDropBitMeRequested()
    }

    @Test(expected = Test.None::class)
    fun `does not notify with no observer`() {
        val configuration = createConfiguration()

        configuration.showWhenPossible()
    }

    @Test
    fun `provides access to dropbit uri`() {
        val twitterIdentity = DropbitMeIdentity()
        twitterIdentity.handle = "--twitter-identity--"
        val phoneIdentity = DropbitMeIdentity()
        phoneIdentity.handle = "--phone-identity--"
        val configuration = createConfiguration()
        whenever(configuration.dropbitAccountHelper.preferredIdentity).thenReturn(twitterIdentity).thenReturn(phoneIdentity)

        assertThat(configuration.shareUrl, equalTo("https://dropbit.me/--twitter-identity--"))
        assertThat(configuration.shareUrl, equalTo("https://dropbit.me/--phone-identity--"))
    }

    @Test
    fun `true when disabled`() {
        val configuration = createConfiguration()

        whenever(configuration.dropbitAccountHelper.hasPrivateAccount).thenReturn(true).thenReturn(false)

        assertTrue(configuration.isDisabled)
        assertFalse(configuration.isDisabled)
    }
}