package com.coinninja.coinkeeper.util

import android.Manifest
import android.content.ContentResolver
import android.provider.ContactsContract
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.util.android.PermissionsUtil
import java.util.*
import javax.inject.Inject

@Mockable
class CoinNinjaContactResolver @Inject constructor(
        internal val permissionsUtil: PermissionsUtil,
        internal val contentResolver: ContentResolver) {

    fun getContacts(): List<Contact> {
        if (!permissionsUtil.hasPermission(Manifest.permission.READ_CONTACTS)) {
            return ArrayList()
        }
        val contacts = ArrayList<Contact>()
        val managedCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER), null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        var number: String?

        if (managedCursor?.moveToFirst() == true) {
            do {
                number = managedCursor.getString(
                        managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                if (null != number) {
                    if (!PhoneNumber(number).isValid) { continue; }
                    val contact = Contact()
                    contact.displayName = managedCursor.getString(
                            managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    contact.setPhoneNumber(number)
                    contacts.add(contact)
                }
            } while (managedCursor.moveToNext())
        }

        managedCursor?.close()

        return contacts
    }
}