package com.coinninja.coinkeeper.service

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.twitter.Twitter
import app.dropbit.twitter.model.TwitterUser
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.UserIdentity
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.UserIdentityHelper
import com.coinninja.coinkeeper.util.LocalContactQueryUtil
import com.coinninja.coinkeeper.util.android.PermissionsUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class ContactLookupServiceTest {


    private fun createContactLookupService(): ContactLookupService {
        val service = Robolectric.setupService(ContactLookupService::class.java)
        service.permissionsUtil = mock(PermissionsUtil::class.java)
        service.localContactQueryUtil = mock(LocalContactQueryUtil::class.java)
        service.userIdentityHelper = mock(UserIdentityHelper::class.java)
        service.twitter = mock(Twitter::class.java)
        service.dropbitAccountHelper = mock(DropbitAccountHelper::class.java)
        return service
    }

    @Test
    fun compare() {
        val service = ContactLookupService()
        val i18nPhone = PhoneNumber("+12345678901")

        assertFalse(service.compare(null, i18nPhone))
        assertFalse(service.compare(PhoneNumber(""), i18nPhone))
        assertFalse(service.compare(PhoneNumber(null as Phonenumber.PhoneNumber?), i18nPhone))
        assertFalse(service.compare(PhoneNumber(1, "6668901"), i18nPhone))
        assertFalse(service.compare(PhoneNumber(1, "567890"), i18nPhone))

        assertTrue(service.compare(i18nPhone, i18nPhone))
        assertTrue(service.compare(PhoneNumber(1, "2345678901"), i18nPhone))
        assertTrue(service.compare(PhoneNumber(1, "5678901"), i18nPhone))
    }

    @Test
    fun `updates user identities that are incomplete`() {
        val service = createContactLookupService()
        val contacts: List<Contact> = listOf(
                Contact(PhoneNumber("+13305551111"), "Human 1", false),
                Contact(PhoneNumber("+13305550000"), "Human 2", false)
        )
        whenever(service.localContactQueryUtil.contacts).thenReturn(contacts)
        val namelessUser = mock(UserIdentity::class.java)
        whenever(namelessUser.identity).thenReturn("+13305550000")
        val identities: List<UserIdentity> = listOf(
                namelessUser
        )
        whenever(service.userIdentityHelper.namelessPhoneIdentities).thenReturn(identities)

        service.updatePhoneIdentities()

        verify(namelessUser).displayName = "Human 2"
        verify(namelessUser).update()
    }

    @Test
    fun `fetches twitter identities saved`() {
        GlobalScope.launch {
            val service = createContactLookupService()
            val identities = listOf(mock(UserIdentity::class.java), mock(UserIdentity::class.java))
            val profile1 = mock(TwitterUser::class.java)
            whenever(service.permissionsUtil.hasPermission(ArgumentMatchers.any())).thenReturn(false)
            whenever(service.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
            whenever(profile1.displayScreenName()).thenReturn("profile 1 display name")
            whenever(profile1.profileImage).thenReturn("http://profile/image/1_normal")

            val profile2 = mock(TwitterUser::class.java)
            whenever(profile2.displayScreenName()).thenReturn("profile 2 display name")
            whenever(profile2.profileImage).thenReturn("http://profile/image/2_normal")

            whenever(identities[0].identity).thenReturn("12345")
            whenever(identities[0].handle).thenReturn("handle-1")
            whenever(identities[1].identity).thenReturn("56789")
            whenever(identities[1].handle).thenReturn("handle-2")
            whenever(service.userIdentityHelper.twitterIdentities).thenReturn(identities)

            whenever(service.twitter.getUser("12345".toLong(), "handle-1")).thenReturn(profile1)
            whenever(service.twitter.getUser("56789".toLong(), "handle-2")).thenReturn(profile2)
            service.onHandleWork(Intent())

            verify(identities[0]).avatar = "http://profile/image/1_bigger"
            verify(identities[0]).displayName = "profile 1 display name"
            verify(identities[0]).update()

            verify(identities[1]).displayName = "profile 2 display name"
            verify(identities[1]).avatar = "http://profile/image/2_bigger"
            verify(identities[1]).update()
        }
    }
}