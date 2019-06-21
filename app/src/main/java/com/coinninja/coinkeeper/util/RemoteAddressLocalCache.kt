package com.coinninja.coinkeeper.util

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.dto.AddressDTO
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import com.google.gson.Gson
import java.util.*
import javax.inject.Inject

@Mockable
class RemoteAddressLocalCache @Inject
internal constructor(internal val preferencesUtil: PreferencesUtil, internal val localBroadCastUtil: LocalBroadCastUtil) {

    var localRemoteAddressCache: List<AddressDTO>?
        get() {
            val remoteAddressString = preferencesUtil.getString(LOCAL_ADDRESS_CACHE_KEY, "[]")
            val addresses = Gson().fromJson(remoteAddressString, Array<AddressDTO>::class.java)
            return Arrays.asList(*addresses)
        }
        set(remoteAddressCache) {
            if (remoteAddressCache.isNullOrEmpty()) {
                preferencesUtil.removePreference(LOCAL_ADDRESS_CACHE_KEY)
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_CLEARED)
            } else {
                val json = Gson().toJson(remoteAddressCache)
                preferencesUtil.savePreference(LOCAL_ADDRESS_CACHE_KEY, json)
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_POPULATED)
            }
        }

    companion object {
        val LOCAL_ADDRESS_CACHE_KEY = "all-local-address-cache-key"
    }

}
