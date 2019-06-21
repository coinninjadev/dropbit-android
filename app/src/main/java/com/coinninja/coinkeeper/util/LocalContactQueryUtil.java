package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.model.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class LocalContactQueryUtil {

    private final CoinNinjaContactResolver contentResolver;

    @Inject
    public LocalContactQueryUtil(CoinNinjaContactResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public List<Contact> getContacts() {
        return contentResolver.getContacts();
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

        for (Contact contact : contentResolver.getContacts()) {
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
