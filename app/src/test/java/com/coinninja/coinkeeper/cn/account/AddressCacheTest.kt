package com.coinninja.coinkeeper.cn.account

import app.coinninja.cn.libbitcoin.HDWallet
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.MetaAddress
import com.coinninja.coinkeeper.model.db.Wallet
import com.coinninja.coinkeeper.wallet.data.TestData.EXTERNAL_ADDRESSES
import com.nhaarman.mockitokotlin2.*
import org.junit.Test

class AddressCacheTest {
    private fun createAddressCache(): AddressCache {
        val addressCache = AddressCache(mock(), mock(), mock(), 10)
        val wallet: Wallet = mock()
        whenever(wallet.purpose).thenReturn(49)
        whenever(wallet.coinType).thenReturn(0)
        whenever(wallet.accountIndex).thenReturn(0)
        whenever(addressCache.walletHelper.primaryWallet).thenReturn(wallet)
        return addressCache
    }

    @Test
    fun requests_current_index_plus_padding() {
        val addressCache = createAddressCache()
        val externalIndex = 22
        val internalIndex = 2
        whenever(addressCache.walletHelper.currentExternalIndex).thenReturn(externalIndex)
        whenever(addressCache.walletHelper.currentInternalIndex).thenReturn(internalIndex)
        whenever(addressCache.hdWallet.fillBlock(any(), any(), any(), any(), any(), any())).thenReturn(emptyArray())

        addressCache.cacheAddressesFor(HDWallet.EXTERNAL)
        verify(addressCache.hdWallet).fillBlock(49, 0, 0, HDWallet.EXTERNAL, 0, externalIndex + numAddressesToCache)
        addressCache.cacheAddressesFor(HDWallet.INTERNAL)
        verify(addressCache.hdWallet).fillBlock(49, 0, 0, HDWallet.INTERNAL, 0, internalIndex + numAddressesToCache)
    }

    @Test
    fun caches_addresses_generated_by_hd_wallet() {
        val addressCache = createAddressCache()
        val externalIndex = 5
        whenever(addressCache.walletHelper.currentExternalIndex).thenReturn(externalIndex)

        val addresses: Array<MetaAddress> = createMockAddresses(0, 15)
        whenever(addressCache.hdWallet.fillBlock(49, 0, 0, HDWallet.EXTERNAL, 0, 15)).thenReturn(addresses)
        addressCache.cacheAddressesFor(HDWallet.EXTERNAL)

        addresses.forEach {
            verify(addressCache.addressHelper).saveAddress(it)
        }

        verify(addressCache.addressHelper, times(15)).saveAddress(any())
    }

    @Test
    fun creates_internal_cache_only_when_necessary() {
        val addressCache = createAddressCache()
        val internalIndex = 2
        whenever(addressCache.walletHelper.currentInternalIndex).thenReturn(internalIndex)
        whenever(addressCache.addressHelper.getAddressCountFor(HDWallet.INTERNAL)).thenReturn(12)
        addressCache.cacheAddressesFor(HDWallet.INTERNAL)
        verify(addressCache.hdWallet, times(0)).fillBlock(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun creates_external_cache_only_when_necessary() {
        val addressCache = createAddressCache()
        val externalIndex = 22
        whenever(addressCache.walletHelper.currentExternalIndex).thenReturn(externalIndex)
        whenever(addressCache.addressHelper.getAddressCountFor(HDWallet.EXTERNAL)).thenReturn(32)
        addressCache.cacheAddressesFor(HDWallet.EXTERNAL)
        verify(addressCache.hdWallet, times(0)).fillBlock(any(), any(), any(), any(), any(), any())
    }

    private fun createMockAddresses(start: Int, end: Int): Array<MetaAddress> {
        val addresses: Array<String> = EXTERNAL_ADDRESSES.copyOfRange(start, end)
        val metaAddresses = mutableListOf<MetaAddress>()
        addresses.forEachIndexed { index, address ->
            metaAddresses.add(MetaAddress(address, "-- pubkey ${index} --", DerivationPath(49, 0, 0, 0, index)))
        }
        return metaAddresses.toTypedArray()
    }

    companion object {
        private const val numAddressesToCache = 10
    }
}