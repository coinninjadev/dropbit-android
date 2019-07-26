package com.coinninja.coinkeeper.ui.market

import android.os.Bundle
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity

class MarketScreenActivity : SecuredActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)
    }


}
