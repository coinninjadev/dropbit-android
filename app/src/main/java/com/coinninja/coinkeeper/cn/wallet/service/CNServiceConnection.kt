package com.coinninja.coinkeeper.cn.wallet.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import app.dropbit.annotations.Mockable

import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil

import javax.inject.Inject

@Mockable
class CNServiceConnection @Inject constructor(
        internal val localBroadCastUtil: LocalBroadCastUtil) : ServiceConnection {

    internal var cnWalletBinder: CNWalletBinder? = null

    val cnWalletServicesInterface: CNWalletServicesInterface? get() = cnWalletBinder?.service

    var isBounded: Boolean = false

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        cnWalletBinder = service as CNWalletBinder
        isBounded = true
        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_ON_SERVICE_CONNECTION_BOUNDED)
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        isBounded = false
    }
}
