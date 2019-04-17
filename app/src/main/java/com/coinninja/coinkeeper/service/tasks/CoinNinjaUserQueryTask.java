package com.coinninja.coinkeeper.service.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.LocalContactQueryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Response;

public class CoinNinjaUserQueryTask extends AsyncTask<Void, Void, CoinNinjaUserQueryTask.ContactsHolder> {

    private static final String TAG = CoinNinjaUserQueryTask.class.getSimpleName();
    private final SignedCoinKeeperApiClient client;
    private OnCompleteListener onCompleteListener;
    private final LocalContactQueryUtil localContactQueryUtil;
    private List<Contact> verifiedContacts;
    private List<Contact> unVerifiedContacts;

    @Inject
    CoinNinjaUserQueryTask(SignedCoinKeeperApiClient client, LocalContactQueryUtil localContactQueryUtil) {
        this.client = client;
        this.localContactQueryUtil = localContactQueryUtil;
    }

    private CoinNinjaUserQueryTask(SignedCoinKeeperApiClient client, LocalContactQueryUtil localContactQueryUtil, OnCompleteListener onCompleteListener) {
        this(client, localContactQueryUtil);
        setOnCompleteListener(onCompleteListener);
    }

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    @Override
    protected ContactsHolder doInBackground(Void... voids) {
        List<List<Contact>> chunks = localContactQueryUtil.getContactsInChunks(100);
        verifiedContacts = new ArrayList<>();
        unVerifiedContacts = new ArrayList<>();

        for (List<Contact> chunk : chunks) {
            fetchContactStateForChunk(chunk);
        }

        return new ContactsHolder(verifiedContacts, unVerifiedContacts);
    }

    @Override
    public CoinNinjaUserQueryTask clone() {
        return new CoinNinjaUserQueryTask(client, localContactQueryUtil, onCompleteListener);
    }

    @Override
    protected void onPostExecute(ContactsHolder contacts) {
        super.onPostExecute(contacts);
        if (null != onCompleteListener)
            onCompleteListener.onComplete(contacts.verifiedContacts, contacts.unVerifiedContacts);
    }

    private void fetchContactStateForChunk(List<Contact> chunk) {
        Map<String, String> results = fetchContactStatus(chunk);
        for (Contact contact : chunk) {
            String phoneHash = contact.getHash();
            if (results.containsKey(phoneHash) && "verified".equals(results.get(phoneHash))) {
                contact.setVerified(true);
                verifiedContacts.add(contact);
            } else {
                contact.setVerified(false);
                unVerifiedContacts.add(contact);
            }
        }
    }

    private Map<String, String> fetchContactStatus(List<Contact> contacts) {
        Response response = client.fetchContactStatus(contacts);
        Map<String, String> results = new HashMap<>();

        if (response.isSuccessful()) {
            results = (Map) response.body();
        } else {
            Log.d(TAG, "|---- Fetching verified contacts");
            Log.d(TAG, "|------ statusCode: " + String.valueOf(response.code()));
            Log.d(TAG, "|--------- message: " + response.body());
        }
        return results;
    }

    public interface OnCompleteListener {
        void onComplete(List<Contact> verifiedContacts, List<Contact> unVerifiedContacts);
    }

    static class ContactsHolder {
        final public List<Contact> verifiedContacts;
        final public List<Contact> unVerifiedContacts;

        public ContactsHolder(List<Contact> verifiedContacts, List<Contact> unVerifiedContacts) {
            this.verifiedContacts = verifiedContacts;
            this.unVerifiedContacts = unVerifiedContacts;
        }
    }
}
