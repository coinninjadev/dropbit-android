package com.coinninja.coinkeeper.ui.spending

import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.interactor.UserPreferences
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.LocationUtil
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter.TYPE
import javax.inject.Inject

class BuyBitcoinActivity : BaseActivity() {
    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var locationUtil: LocationUtil


    val gPayLink: View get() = findViewById(R.id.buy_with_g_pay)
    val copyBlockchainAddressButton: Button get() = findViewById(R.id.copy_blockchain_address)
    val findATM: Button get() = findViewById(R.id.buy_at_atm)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (locationUtil.hasGrantedPermission(requestCode, permissions, grantResults)) {
            onNavigateToMap(locationUtil.lastKnownLocation)
        } else {
            onNavigateToMap(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_bitcoin)
        copyBlockchainAddressButton.text = accountManager.nextReceiveAddress
        findATM.setOnClickListener {
            if (locationUtil.canReadLocation()) {
                onNavigateToMap(locationUtil.lastKnownLocation)
            } else {
                locationUtil.requestPermissionToAccessLocationFor(this)
            }
        }
        gPayLink.setOnClickListener { activityNavigationUtil.buyBitcoin(this, accountManager.nextReceiveAddress) }
    }

    private fun onNavigateToMap(location: Location?) {
        val parameters = HashMap<CoinNinjaParameter, String>()
        parameters[TYPE] = "atms"
        val event = Analytics.EVENT_BUY_BITCOIN_AT_ATM
        activityNavigationUtil.navigatesToMapWith(this, parameters, location, event)
    }
}