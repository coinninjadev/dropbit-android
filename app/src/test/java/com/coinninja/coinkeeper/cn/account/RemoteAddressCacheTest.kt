package com.coinninja.coinkeeper.cn.account

import app.coinninja.cn.libbitcoin.model.DerivationPath
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.model.db.Address
import com.coinninja.coinkeeper.model.db.Wallet
import com.coinninja.coinkeeper.model.dto.AddressDTO
import com.coinninja.coinkeeper.service.client.model.AddAddressBodyRequest
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress
import com.nhaarman.mockitokotlin2.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Test
import org.mockito.Mockito.mock
import retrofit2.Response
import java.util.*

class RemoteAddressCacheTest {
    private var addressToPubKey: HashMap<String, AddressDTO> = HashMap()
    private val cachedAddresses: MutableList<CNWalletAddress> = ArrayList()

    private fun setupAddress(address: String, pubKey: String): AddressDTO {
        val newAddress: Address = mock(Address::class.java)
        whenever(newAddress.derivationPath).thenReturn(mock(DerivationPath::class.java))
        whenever(newAddress.address).thenReturn(address)
        return AddressDTO(newAddress, pubKey)
    }

    private fun createRemoteCache(): RemoteAddressCache = RemoteAddressCache(mock(), mock(), mock(), mock(), mock()).also {
        val wallet: Wallet = mock()
        addressToPubKey["-- addr 0"] = setupAddress("-- addr 0", "-- addr pub key 0")
        addressToPubKey["-- addr 1"] = setupAddress("-- addr 1", "-- addr pub key 1")
        addressToPubKey["-- addr 2"] = setupAddress("-- addr 2", "-- addr pub key 2")
        addressToPubKey["-- addr 3"] = setupAddress("-- addr 3", "-- addr pub key 3")
        addressToPubKey["-- addr 4"] = setupAddress("-- addr 4", "-- addr pub key 4")
        whenever(it.accountManager.unusedAddressesToPubKey(HDWalletWrapper.EXTERNAL, 5)).thenReturn(addressToPubKey)
        whenever(it.apiClient.cnWalletAddresses).thenReturn(Response.success(cachedAddresses))
        whenever(it.hdWalletWrapper.verificationKeyFor(wallet)).thenReturn("--verification-key--")
        whenever(it.walletHelper.segwitWallet).thenReturn(wallet)
        whenever(wallet.purpose).thenReturn(84)

        addressToPubKey.keys.forEachIndexed { index, addr ->
            val address = CNWalletAddress(address = addr, publicKey = addressToPubKey[addr]?.uncompressedPublicKey)
            if (index == 3) {
                address.publicKey = null
            }
            cachedAddresses.add(address)
        }
    }

    @After
    fun cleanup() {
        addressToPubKey.clear()
        cachedAddresses.clear()
    }

    @Test
    fun only_cache_addresses_when_verified() {
        val remoteCache = createRemoteCache()
        val response: Response<List<CNWalletAddress>> = Response.error(401, ResponseBody.create(MediaType.parse("text/html"), ""))
        whenever(remoteCache.apiClient.cnWalletAddresses).thenReturn(response)

        remoteCache.cacheAddresses()

        verify(remoteCache.apiClient, times(0)).removeAddress(any())
        verify(remoteCache.apiClient, times(0)).addAddress(any())
    }

    @Test
    fun sends_five_addresses_when_no_addresses_does_not_cache_generated_lnd__when_wallet_is_49() {
        val remoteCache = createRemoteCache()
        whenever(remoteCache.apiClient.cnWalletAddresses).thenReturn(Response.success(emptyList()))
        whenever(remoteCache.walletHelper.segwitWallet).thenReturn(null)

        remoteCache.cacheAddresses()

        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 0", pubKey = "-- addr pub key 0"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 1", pubKey = "-- addr pub key 1"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 2", pubKey = "-- addr pub key 2"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 3", pubKey = "-- addr pub key 3"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 4", pubKey = "-- addr pub key 4"))
        verify(remoteCache.apiClient, times(5)).addAddress(any())
    }

    @Test
    fun sends_five_addresses_when_no_addresses_cached_plus_generated_lnd__when_wallet_84() {
        val remoteCache = createRemoteCache()
        whenever(remoteCache.apiClient.cnWalletAddresses).thenReturn(Response.success(emptyList()))

        remoteCache.cacheAddresses()

        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 0", pubKey = "-- addr pub key 0"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 1", pubKey = "-- addr pub key 1"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 2", pubKey = "-- addr pub key 2"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 3", pubKey = "-- addr pub key 3"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 4", pubKey = "-- addr pub key 4"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "generate", pubKey = "--verification-key--", addressType = "lightning"))
    }

    @Test
    fun does_not_cache_lightning_when_it_already_is_cached() {
        val remoteCache = createRemoteCache()
        whenever(remoteCache.apiClient.cnWalletAddresses).thenReturn(
                Response.success(listOf(CNWalletAddress(address = "generate", publicKey = "--verification-key--"))))

        remoteCache.cacheAddresses()

        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 0", pubKey = "-- addr pub key 0"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 1", pubKey = "-- addr pub key 1"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 2", pubKey = "-- addr pub key 2"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 3", pubKey = "-- addr pub key 3"))
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 4", pubKey = "-- addr pub key 4"))
        verify(remoteCache.apiClient, times(0)).addAddress(AddAddressBodyRequest(address = "generate", pubKey = "--verification-key--", addressType = "lightning"))
    }

    @Test
    fun removes_generated_wallet_address_when_pubkeys_do_not_match() {
        val remoteCache = createRemoteCache()
        cachedAddresses.add(CNWalletAddress(address = "generate", publicKey = "--wrong-verification-key--"))

        remoteCache.cacheAddresses()

        verify(remoteCache.apiClient).removeAddress("generate")
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "generate", pubKey = "--verification-key--", addressType = "lightning"))
    }

    @Test
    fun removes_addresses_that_do_not_align_with_unused_block__does_not_remove_lightning() {
        val remoteCache = createRemoteCache()
        cachedAddresses[4].address = "-- addr 5"

        remoteCache.cacheAddresses()

        verify(remoteCache.apiClient).removeAddress("-- addr 5")
    }

    @Test
    fun removes_addresses_that_do_not_align_with_unused_block() {
        val remoteCache = createRemoteCache()
        cachedAddresses.add(CNWalletAddress(address = "generate", publicKey = "--verification-key--"))

        remoteCache.cacheAddresses()

        verify(remoteCache.apiClient, times(0)).removeAddress("generate")
    }

    @Test
    fun removes_addresses_that_do_not_have_a_public_key() {
        val remoteCache = createRemoteCache()
        remoteCache.cacheAddresses()
        verify(remoteCache.apiClient).removeAddress("-- addr 3")
    }

    @Test
    fun adds_address_when_lists_do_not_match() {
        val remoteCache = createRemoteCache()
        cachedAddresses.removeAt(3)
        remoteCache.cacheAddresses()
        verify(remoteCache.apiClient).addAddress(AddAddressBodyRequest(address = "-- addr 3", pubKey = "-- addr pub key 3"))
    }

    @Test
    fun removes_all_cached_addresses__locally__remote() {
        val remoteCache = createRemoteCache()

        remoteCache.removeAll()

        verify(remoteCache.apiClient).removeAddress("-- addr 0")
        verify(remoteCache.apiClient).removeAddress("-- addr 1")
        verify(remoteCache.apiClient).removeAddress("-- addr 2")
        verify(remoteCache.apiClient).removeAddress("-- addr 3")
        verify(remoteCache.apiClient).removeAddress("-- addr 4")
        verify(remoteCache.remoteAddressLocalCache).localRemoteAddressCache = emptyList()
    }
}