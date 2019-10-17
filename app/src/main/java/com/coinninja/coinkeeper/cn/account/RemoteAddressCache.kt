package com.coinninja.coinkeeper.cn.account

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.model.dto.AddressDTO
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.AddAddressBodyRequest
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress
import com.coinninja.coinkeeper.util.RemoteAddressLocalCache
import retrofit2.Response
import java.util.*
import javax.inject.Inject

@Mockable
@Suppress("UNCHECKED_CAST")
class RemoteAddressCache @Inject internal constructor(
        internal val hdWalletWrapper: HDWalletWrapper,
        internal val apiClient: SignedCoinKeeperApiClient,
        internal val accountManager: AccountManager,
        internal val walletHelper: WalletHelper,
        internal val remoteAddressLocalCache: RemoteAddressLocalCache
) {
    fun cacheAddresses() {
        val response: Response<*> = apiClient.cnWalletAddresses
        if (response.isSuccessful) {
            val cachedAddresses: List<CNWalletAddress> = response.body() as List<CNWalletAddress>
            val addressStringToDTO: HashMap<String, AddressDTO> = accountManager
                    .unusedAddressesToPubKey(HDWalletWrapper.EXTERNAL, NUM_ADDRESSES_TO_CACHE)
            removeUsedAddresses(cachedAddresses, addressStringToDTO)
            removeServerAddressesWithoutPublicKey(cachedAddresses)
            sendAddresses(cachedAddresses, addressStringToDTO)
            addLightning(cachedAddresses)
        }
    }

    private fun addLightning(cachedAddresses: List<CNWalletAddress>) {
        var hasLightning = false
        walletHelper.segwitWallet?.let { wallet ->
            cachedAddresses.forEach { walletAddress ->
                if (walletAddress.address == "generate" && walletAddress.publicKey == hdWalletWrapper.verificationKeyFor(wallet)) {
                    hasLightning = true
                } else if (walletAddress.address == "generate") {
                    apiClient.removeAddress("generate")
                }
            }

            if (!hasLightning && wallet.purpose == 84) {
                apiClient.addAddress(AddAddressBodyRequest(pubKey = hdWalletWrapper.verificationKeyFor(wallet), addressType = "lightning"))
            }
        }

    }

    private fun cacheUnusedAddressesLocally(unusedAddresses: List<AddressDTO>) {
        remoteAddressLocalCache.localRemoteAddressCache = unusedAddresses
    }

    private fun removeServerAddressesWithoutPublicKey(cachedAddresses: List<CNWalletAddress>) {
        for (address in cachedAddresses) {
            if (address.publicKey.isNullOrEmpty()) {
                apiClient.removeAddress(address.address)
            }
        }
    }

    private fun removeUsedAddresses(cachedAddresses: List<CNWalletAddress>, localCache: HashMap<String, AddressDTO>) {
        for (walletAddress in cachedAddresses) {
            if (walletAddress.address == "generate") continue
            if (!localCache.keys.contains(walletAddress.address))
                apiClient.removeAddress(walletAddress.address)
        }
    }

    private fun sendAddresses(cachedAddresses: List<CNWalletAddress>, addressDTOHashMap: HashMap<String, AddressDTO>) {
        val cachedAddrs: MutableList<String> = mutableListOf()

        cachedAddresses.forEach { walletAddress ->
            cachedAddrs.add(walletAddress.address)
        }

        addressDTOHashMap.values.forEach { dto ->
            if (!cachedAddrs.contains(dto.address))
                apiClient.addAddress(AddAddressBodyRequest(address = dto.address, pubKey = dto.uncompressedPublicKey))
        }

        cacheUnusedAddressesLocally(ArrayList(addressDTOHashMap.values))
    }

    fun removeAll() {
        apiClient.cnWalletAddresses.let { response ->
            if (response.isSuccessful) {
                response.body()?.let { remoteAddresses ->
                    remoteAddresses.forEach {
                        apiClient.removeAddress(it.address)
                    }
                }

                remoteAddressLocalCache.localRemoteAddressCache = emptyList()
            }
        }
    }

    companion object {
        private const val NUM_ADDRESSES_TO_CACHE = 5
    }

}