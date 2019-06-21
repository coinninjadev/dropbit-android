package com.coinninja.coinkeeper.service.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coinninja.coinkeeper.model.Contact
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.util.LocalContactQueryUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CoinNinjaUserViewModel : ViewModel {

    private val client: SignedCoinKeeperApiClient
    private val localContactQueryUtil: LocalContactQueryUtil

    var contacts: MutableLiveData<List<Contact>>? = null

    @Inject
    constructor(client: SignedCoinKeeperApiClient, localContactQueryUtil: LocalContactQueryUtil) {
        this.client = client
        this.localContactQueryUtil = localContactQueryUtil
        this.contacts = MutableLiveData()
    }

    fun load() {
        var chunks: List<List<Contact>> = localContactQueryUtil.getContactsInChunks(100)
        var mutableContacts: List<Contact> = mutableListOf()

        GlobalScope.launch {
            for (chunk in chunks) {
                mutableContacts += fetchContactStateForChunk(chunk)
            }

            withContext(Dispatchers.Main) {
                contacts?.value = mutableContacts
            }
        }
    }

    private fun fetchContactStateForChunk(chunk: List<Contact>): List<Contact> {
        if (chunk.size == 0) {
            return chunk; }

        var results: Map<String, String> = fetchContactStatus(chunk)
        for (contact in chunk) {
            var phoneHash: String = contact.hash;
            contact.isVerified = results.containsKey(phoneHash) && "verified" == results[phoneHash]
        }

        return chunk
    }

    private fun fetchContactStatus(contacts: List<Contact>): Map<String, String> {
        var response = client.fetchContactStatus(contacts)
        var results: Map<String, String> = HashMap()

        if (response.isSuccessful) results = response.body() as Map<String, String>
        return results
    }
}