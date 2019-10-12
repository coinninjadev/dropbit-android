package com.coinninja.coinkeeper.ui.account.verify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.ui.account.UserServerAddressesFragment
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.RemoteAddressLocalCache
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import javax.inject.Inject

class UserAccountVerificationActivity : BaseActivity() {

    val intentFilter = IntentFilter(DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_POPULATED)

    @Inject
    lateinit var dropbitAccountHelper: DropbitAccountHelper
    @Inject
    lateinit var preferencesUtil: PreferencesUtil
    @Inject
    lateinit var remoteAddressLocalCache: RemoteAddressLocalCache

    var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_POPULATED,
                DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_CLEARED -> invalidateCacheView()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_account_verification)
        intentFilter.addAction(DropbitIntents.ACTION_LOCAL_ADDRESS_CACHE_CLEARED)
    }

    override fun onStart() {
        super.onStart()
        localBroadCastUtil.registerReceiver(receiver, intentFilter)

    }

    override fun onStop() {
        super.onStop()
        localBroadCastUtil.unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()
        invalidateCacheView()
    }

    fun invalidateCacheView() {
        val view = findViewById<View>(R.id.view_dropbit_addresses)
        if (dropbitAccountHelper.hasVerifiedAccount) {
            view.setOnClickListener { showServerAddressFragment() }
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    private fun showServerAddressFragment() {
        val userServerAddressesFragment = UserServerAddressesFragment.newInstance(remoteAddressLocalCache.localRemoteAddressCache)
        userServerAddressesFragment.show(supportFragmentManager, UserServerAddressesFragment::class.java.simpleName)
    }
}
