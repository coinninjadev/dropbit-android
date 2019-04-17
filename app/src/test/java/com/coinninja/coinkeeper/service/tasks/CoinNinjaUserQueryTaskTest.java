package com.coinninja.coinkeeper.service.tasks;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.LocalContactQueryUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CoinNinjaUserQueryTaskTest {

    @Mock
    private LocalContactQueryUtil localContactQueryUtil;

    @Mock
    private SignedCoinKeeperApiClient client;

    @Mock
    private CoinNinjaUserQueryTask.OnCompleteListener onCompleteListener;

    @InjectMocks
    private CoinNinjaUserQueryTask task;

    private Gson gson;

    private List<Contact> contacts;
    private List<Contact> verifiedContacts;
    private List<Contact> unverifiedContacts;
    private List<List<Contact>> hunks;

    @Mock
    private PhoneNumber phoneNumber;

    private static final String NAME = "Jane Doe";
    private static final String HASH = "--- phone-hash ";
    private List<JsonObject> responses;

    @After
    public void tearDown() {
        localContactQueryUtil = null;
        client = null;
        onCompleteListener = null;
        task = null;
        gson = null;
        contacts = null;
        verifiedContacts = null;
        unverifiedContacts = null;
        hunks = null;
        phoneNumber = null;
        responses = null;
    }

    @Before
    public void setup() {
        task.setOnCompleteListener(onCompleteListener);
        contacts = new ArrayList<>();
        verifiedContacts = new ArrayList<>();
        unverifiedContacts = new ArrayList<>();
        gson = new Gson();
    }

    @Test
    public void does_not_remove_duplicated_numbers_when_names_are_not_duplicated() {
        mockContacts(5, 2);
        contacts.remove(4);
        contacts.remove(3);
        Contact contact = unverifiedContacts.get(0);
        Contact c2 = new Contact(contact.getPhoneNumber(), contact.getDisplayName(), false);
        c2.setHash("--- phone-hash 2 --- ");
        Contact c3 = new Contact(contact.getPhoneNumber(), contact.getDisplayName(), false);
        c3.setDisplayName("SGT Barney");
        c3.setHash("--- phone-hash 2 --- ");
        contacts.add(3, c2);
        contacts.add(4, c3);
        unverifiedContacts.clear();
        unverifiedContacts.add(contact);
        unverifiedContacts.add(c3);
        responses.get(0).remove("--- phone-hash 3 --- ");
        responses.get(0).remove("--- phone-hash 4 --- ");
        responses.get(0).addProperty(c3.getHash(), "unverified");
        hunks.get(0).remove(4);
        hunks.get(0).remove(3);
        hunks.get(0).add(c3);
        when(client.fetchContactStatus(hunks.get(0))).thenReturn(Response.success(gson.fromJson(responses.get(0), Map.class)));

        CoinNinjaUserQueryTask.ContactsHolder holder = task.doInBackground();

        assertThat(holder.verifiedContacts, equalTo(verifiedContacts));
        assertThat(holder.unVerifiedContacts, equalTo(unverifiedContacts));
    }

    @Test
    public void removes_duplicated_contacts() {
        mockContacts(5, 2);
        contacts.remove(4);
        contacts.remove(3);
        Contact contact = unverifiedContacts.get(0);
        Contact c2 = new Contact(contact.getPhoneNumber(), contact.getDisplayName(), false);
        c2.setHash("--- phone-hash 2 --- ");
        Contact c3 = new Contact(contact.getPhoneNumber(), contact.getDisplayName(), false);
        c3.setHash("--- phone-hash 2 --- ");
        contacts.add(3, c2);
        contacts.add(4, c3);
        unverifiedContacts.clear();
        unverifiedContacts.add(contact);
        responses.get(0).remove("--- phone-hash 3 --- ");
        responses.get(0).remove("--- phone-hash 4 --- ");
        hunks.get(0).remove(4);
        hunks.get(0).remove(3);
        when(client.fetchContactStatus(hunks.get(0))).thenReturn(Response.success(gson.fromJson(responses.get(0), Map.class)));

        CoinNinjaUserQueryTask.ContactsHolder holder = task.doInBackground();

        assertThat(holder.verifiedContacts, equalTo(verifiedContacts));
        assertThat(holder.unVerifiedContacts, equalTo(unverifiedContacts));
    }

    @Test
    public void fetches_large_contact_lists_in_chunks() {
        int numberOfContacts = Math.round(100 * 2.5F);
        mockContacts(numberOfContacts, 7);

        CoinNinjaUserQueryTask.ContactsHolder holder = task.doInBackground();

        for (List<Contact> hunk : hunks) {
            verify(client).fetchContactStatus(hunk);
        }

        assertThat(holder.verifiedContacts, equalTo(verifiedContacts));
        assertThat(holder.unVerifiedContacts, equalTo(unverifiedContacts));
    }

    @Test
    public void provides_contacts_to_caller() {
        mockContacts(10, 7);
        CoinNinjaUserQueryTask.ContactsHolder holder = new CoinNinjaUserQueryTask.ContactsHolder(verifiedContacts, unverifiedContacts);

        task.onPostExecute(holder);

        verify(onCompleteListener).onComplete(verifiedContacts, unverifiedContacts);
    }

    @Test
    public void separate_verified_and_unverified_contact() {
        mockContacts(10, 1);

        CoinNinjaUserQueryTask.ContactsHolder holder = task.doInBackground();

        assertThat(holder.verifiedContacts, equalTo(verifiedContacts));
        assertThat(holder.unVerifiedContacts, equalTo(unverifiedContacts));
    }

    @Test
    public void fetches_contact_state_from_api_client() {
        mockContacts(10, 1);

        task.doInBackground();

        verify(client).fetchContactStatus(contacts);
    }

    @Test
    public void fetches_contacts_from_content_resolver() {
        mockContacts(1, 1);

        task.doInBackground();

        verify(localContactQueryUtil).getContactsInChunks(100);
    }

    private void mockContacts(int numberOfContacts, int numberOfVerifiedContacts) {
        contacts.clear();
        hunks = new ArrayList<>();
        verifiedContacts.clear();
        unverifiedContacts.clear();
        responses = new ArrayList<>();
        String hash;
        String status;
        Contact contact;
        int which = 0;

        responses.add(new JsonObject());
        hunks.add(new ArrayList<>());
        for (int i = 0; i < numberOfContacts; i++) {
            if (i != 0 && i % 25 == 0) {
                which += 1;
                responses.add(new JsonObject());
                hunks.add(new ArrayList<>());
            }
            hash = buildHashFor(i);
            contact = new Contact();
            contact.setDisplayName(NAME + " - " + String.valueOf(i));
            contact.setPhoneNumber(phoneNumber);
            contact.setHash(hash);
            contacts.add(contact);
            hunks.get(which).add(contact);

            status = i < numberOfVerifiedContacts ? "verified" : "unverified";
            responses.get(which).addProperty(hash, status);

            if (status.equals("verified")) {
                verifiedContacts.add(contact);
            } else {
                unverifiedContacts.add(contact);
            }
        }

        for (int i = 0; i < hunks.size(); i++) {
            when(client.fetchContactStatus(hunks.get(i))).thenReturn(
                    Response.success(gson.fromJson(responses.get(i).toString(), Map.class)));
        }

        when(localContactQueryUtil.getContactsInChunks(anyInt())).thenReturn(hunks);
    }

    @NonNull
    private String buildHashFor(int i) {
        return HASH + String.valueOf(i) + " --- ";
    }

}