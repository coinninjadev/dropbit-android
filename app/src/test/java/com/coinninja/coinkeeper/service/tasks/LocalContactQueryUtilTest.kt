package com.coinninja.coinkeeper.service.tasks

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.util.CoinNinjaContactResolver
import com.coinninja.coinkeeper.util.LocalContactQueryUtil
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class LocalContactQueryUtilTest {

    fun setup(coinNinjaContentResolver: CoinNinjaContactResolver): LocalContactQueryUtil {
        return LocalContactQueryUtil(coinNinjaContentResolver)
    }

    private fun createContact(phoneNumber: PhoneNumber = PhoneNumber("+12223334444"), name: String = "Jimmy Bob"): Contact {
        var contact = Contact()
        contact.phoneNumber = phoneNumber
        contact.displayName = name
        contact.isVerified = false
        return contact
    }

    private fun createContactList(number: Int): List<Contact> {
        val contacts: MutableList<Contact> = mutableListOf()
        for (i in 1..number) {
            contacts.add(createContact(name = "Jimmy Bob$i"))
        }

        return contacts
    }

    @Test
    fun removes_duplicated_contacts() {
        val contentResolver = mock(CoinNinjaContactResolver::class.java)
        whenever(contentResolver.getContacts()).thenReturn(createContactList(100))
        val queryUtil = setup(contentResolver)
        queryUtil.getContactsInChunks(100)
        assert(queryUtil.getContactsInChunks(100).size == 1)
    }

    @Test
    fun fetches_large_contact_lists_in_chunks() {
        val contentResolver = mock(CoinNinjaContactResolver::class.java)
        whenever(contentResolver.getContacts()).thenReturn(createContactList(400))
        val queryUtil = setup(contentResolver)
        assert(queryUtil.getContactsInChunks(100).size == 4)
    }

    @Test
    fun fetches_contacts_from_content_resolver() {
        val contentResolver = mock(CoinNinjaContactResolver::class.java)
        whenever(contentResolver.getContacts()).thenReturn(createContactList(100))
        val queryUtil = setup(contentResolver)
        queryUtil.getContactsInChunks(100)
        verify(contentResolver).getContacts()
    }
}