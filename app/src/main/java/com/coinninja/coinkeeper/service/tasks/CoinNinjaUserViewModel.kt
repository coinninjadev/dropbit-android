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

class CoinNinjaUserViewModel @Inject constructor(
        internal val client: SignedCoinKeeperApiClient,
        internal val localContactQueryUtil: LocalContactQueryUtil) : ViewModel() {

    var contacts: MutableLiveData<List<Contact>>? = null

    init {
        this.contacts = MutableLiveData()
    }

    fun load() {
        val chunks: List<List<Contact>> = localContactQueryUtil.getContactsInChunks(100)
        val mutableContacts: MutableList<Contact> = mutableListOf()

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

        val results: Map<String, String> = fetchContactStatus(chunk)
        for (contact in chunk) {
            val phoneHash: String = contact.hash;
            contact.isVerified = results.containsKey(phoneHash) && "verified" == results[phoneHash]
        }

        return chunk
    }

    private fun fetchContactStatus(contacts: List<Contact>): Map<String, String> {
        val response = client.fetchContactStatus(contacts)
        var results: Map<String, String> = HashMap()

        if (response.isSuccessful) results = response.body() as Map<String, String>
        return results
    }
}