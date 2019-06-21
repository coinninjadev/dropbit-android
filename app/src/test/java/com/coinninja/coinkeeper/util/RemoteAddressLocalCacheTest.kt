package com.coinninja.coinkeeper.util

import com.coinninja.bindings.DerivationPath
import com.coinninja.coinkeeper.model.dto.AddressDTO
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Test
import org.mockito.Mockito.*
import java.util.*

class RemoteAddressLocalCacheTest {

    private var addressWrappers: List<AddressDTO>? = ArrayList()

    private var remoteAddressLocalCache: RemoteAddressLocalCache = RemoteAddressLocalCache(mock(PreferencesUtil::class.java), mock(LocalBroadCastUtil::class.java))

    @After
    fun tearDown() {
        addressWrappers = null
    }


    @Test
    fun retrieves_addressees_from_local_storage() {
        val path = DerivationPath(49, 0, 0, 1, 10)
        val address = AddressDTO("-- address--",
                AddressDTO.toDerivationPathString(path),
                "-pub-key-")
        val cachedAddresses = ArrayList<AddressDTO>()
        cachedAddresses.add(address)
        whenever(remoteAddressLocalCache.preferencesUtil.getString(RemoteAddressLocalCache.LOCAL_ADDRESS_CACHE_KEY, "[]"))
                .thenReturn("[{\"address\":\"-- address--\"," +
                        "\"derivationPath\":\"M/49/0/0/1/10\"," +
                        "\"uncompressedPublicKey\":\"-pub-key-\"}]")


        assertThat(remoteAddressLocalCache.localRemoteAddressCache,
                equalTo<List<AddressDTO>>(cachedAddresses))
    }

    @Test
    fun returns_empty_array_when_no_addresses_saved_locally() {
        whenever(remoteAddressLocalCache.preferencesUtil
                .getString(RemoteAddressLocalCache.LOCAL_ADDRESS_CACHE_KEY, "[]"))
                .thenReturn("[]")

        assertThat(remoteAddressLocalCache.localRemoteAddressCache,
                equalTo(ArrayList()))
    }

    @Test
    fun saves_addresses_to_shared_preferences() {
        val path = DerivationPath(49, 0, 0, 1, 10)
        val address = AddressDTO("-- address--",
                AddressDTO.toDerivationPathString(path),
                "-pub-key-")
        val addressesToCache = ArrayList<AddressDTO>()
        addressesToCache.add(address)

        remoteAddressLocalCache.localRemoteAddressCache = addressesToCache

        verify(remoteAddressLocalCache.preferencesUtil).savePreference(
                RemoteAddressLocalCache.LOCAL_ADDRESS_CACHE_KEY, "[{\"address\":\"-- address--\"," +
                "\"derivationPath\":\"M/49/0/0/1/10\"," +
                "\"uncompressedPublicKey\":\"-pub-key-\"}]")
        verify(remoteAddressLocalCache.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_POPULATED)
    }

    @Test
    fun accepts_null_or_empty_addresses_to_save() {
        remoteAddressLocalCache.localRemoteAddressCache = null
        remoteAddressLocalCache.localRemoteAddressCache = ArrayList()

        verify(remoteAddressLocalCache.preferencesUtil, times(2))
                .removePreference(RemoteAddressLocalCache.LOCAL_ADDRESS_CACHE_KEY)

        verify(remoteAddressLocalCache.localBroadCastUtil, times(2))
                .sendBroadcast(DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_CLEARED)
    }
}