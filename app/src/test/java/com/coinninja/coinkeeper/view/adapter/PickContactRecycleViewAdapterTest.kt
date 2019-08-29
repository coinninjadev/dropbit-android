package com.coinninja.coinkeeper.view.adapter;

import android.view.View
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.ui.util.OnItemClickListener
import com.coinninja.coinkeeper.view.adapter.PickContactRecycleViewAdapter.Companion.CONTACT
import com.coinninja.coinkeeper.view.adapter.PickContactRecycleViewAdapter.Companion.UNVERIFIED_HEADER
import junit.framework.Assert.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class PickContactRecycleViewAdapterTest {

    fun setup(): PickContactRecycleViewAdapter {
        val adapter = PickContactRecycleViewAdapter()
        val onItemClickListener = mock(OnItemClickListener::class.java)
        val onClickListener = mock(View.OnClickListener::class.java)
        adapter.setupOnClickListeners(onItemClickListener, onClickListener)
        return adapter
    }

    private fun createContact(phoneNumber: PhoneNumber = PhoneNumber("+12223334444"), isVerified: Boolean = false, name: String = "Jimmy Bob"): Contact {
        val contact = Contact();
        contact.phoneNumber = phoneNumber
        contact.displayName = name
        contact.isVerified = isVerified
        return contact
    }

    private fun createContactList(number: Int, isVerified: Boolean = false): MutableList<Contact> {
        val contacts: MutableList<Contact> = mutableListOf()
        for (i in 1..number) {
            contacts.add(createContact(isVerified = isVerified))
        }

        return contacts
    }

    @Test
    fun `returns correct count of items`() {
        val adapter = setup()

        adapter.setContacts(createContactList(10))

        assertThat(adapter.getContacts().size, equalTo(10))
    }

    @Test
    fun `unverified header index set correctly`() {
        val adapter = setup()
        adapter.setContacts(createContactList(5, true) + createContactList(5, false))
        assertThat(adapter.unVerifiedHeaderIndex, equalTo(5))
    }

    @Test
    fun `verify get item returns correct item`() {
        val adapter = setup()
        val contacts = createContactList(5, true) + createContactList(5, false)
        adapter.setContacts(contacts)

        assertThat(adapter.getItemAt(0)!!::class.java.simpleName, equalTo(Contact::class.java.simpleName))
        assertNull(adapter.getItemAt(5))
    }

    @Test
    fun `verify click listeners`() {
        val adapter = setup()
        val context = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()
        val viewGroup = LinearLayout(context)
        val viewHolder = adapter.onCreateViewHolder(viewGroup, CONTACT)
        viewHolder.bindTo(createContact(), 0)
        viewHolder.view.performClick()

        verify(adapter.clickListener)!!.onItemClick(viewHolder.view, 0)
    }

    @Test
    fun `test contact search`() {
        val adapter = setup()
        val displayName = "Searchy McSearch"
        val searchContact = createContact(name = displayName)
        val contacts = createContactList(5) + searchContact
        adapter.setContacts(contacts)
        adapter.filter.filter("Search")

        assertThat(adapter.getContacts().size, equalTo(1))
        assertThat(adapter.getItemAt(1)!!.displayName, equalTo(displayName))
    }

    @Test
    fun `test contact search with empty search`() {
        val adapter = setup()
        val contacts = createContactList(5)
        adapter.setContacts(contacts)
        adapter.filter.filter("")

        assertThat(adapter.getContacts().size, equalTo(5))
    }

    @Test
    fun `verify get item view type returns correct item`() {
        val adapter = setup()
        val contacts = createContactList(5, true) + createContactList(5, false)
        adapter.setContacts(contacts)

        assertThat(adapter.getItemViewType(1), equalTo(CONTACT))
        assertThat(adapter.getItemViewType(5), equalTo(UNVERIFIED_HEADER))
        assertThat(adapter.getItemViewType(0), equalTo(CONTACT))
    }

    @Test
    fun empty_list() {
        val adapter = setup()
        val contacts = createContactList(1, true) + createContactList(1)
        adapter.setContacts(contacts)
        assertThat(adapter.getContacts().size, equalTo(2))

        adapter.filter.filter("foo")

        assertThat(adapter.getContacts().size, equalTo(0))
        assertNull(adapter.getItemAt(0))

        val parent = LinearLayout(ApplicationProvider.getApplicationContext())
        val holder = adapter.onCreateViewHolder(parent, CONTACT)
        adapter.onBindViewHolder(holder, 1)
    }
}