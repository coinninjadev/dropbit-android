package com.coinninja.coinkeeper.util;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class LocalContactQueryUtil {

    private final ContentResolver contentResolver;
    private PermissionsUtil permissionsUtil;

    @Inject
    public LocalContactQueryUtil(ContentResolver contentResolver, PermissionsUtil permissionsUtil) {
        this.contentResolver = contentResolver;
        this.permissionsUtil = permissionsUtil;
    }

    public List<Contact> getContacts() {
        if (!permissionsUtil.hasPermission(Manifest.permission.READ_CONTACTS)) { return new ArrayList<>(); }
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor managedCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                }, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        String number;

        if (managedCursor.moveToFirst()) {
            do {
                number = managedCursor.getString(
                        managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if (null != number) {
                    Contact contact = new Contact();
                    contact.setDisplayName(managedCursor.getString(
                            managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    contact.setPhoneNumber(number);
                    contacts.add(contact);
                }
            } while (managedCursor.moveToNext());
        }

        managedCursor.close();

        return contacts;
    }

    public List<List<Contact>> getContactsInChunks(Integer chunkSize) {
        List<List<Contact>> chunks = new ArrayList<>();
        chunks.add(new ArrayList<>());
        List<Contact> contacts = getCleanedContacts();
        int which = 0;

        for (int i = 0; i < contacts.size(); i++) {
            if (i != 0 && i % chunkSize == 0) {
                which += 1;
                chunks.add(new ArrayList<>());
            }

            chunks.get(which).add(contacts.get(i));
        }
        return chunks;
    }

    private List<Contact> getCleanedContacts() {
        Map<String, List<Contact>> map = new HashMap<>();
        List<Contact> contacts = new ArrayList<>();

        boolean duplicated = false;
        String hash;

        for (Contact contact : getContacts()) {
            hash = contact.getHash();
            if (map.containsKey(hash)) {
                for (Contact c : map.get(hash)) {
                    if (null != c.getDisplayName() && null != contact.getDisplayName() &&
                            c.getDisplayName().equals(contact.getDisplayName())) {
                        duplicated = true;
                    }

                }

                if (!duplicated) {
                    map.get(hash).add(contact);
                }
            } else {
                map.put(hash, new ArrayList<>());
                map.get(hash).add(contact);
            }

            if (!duplicated) {
                contacts.add(contact);
            }

            duplicated = false;
        }

        return contacts;
    }

}
