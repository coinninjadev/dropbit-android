package com.coinninja.coinkeeper.util.android.activity

import android.net.Uri
import app.dropbit.annotations.Mockable

@Mockable
class BuyBitcoinUriBuilder constructor(val isProduction: Boolean = true) {
    fun build(address: String): Uri {
        return (
                Uri.parse(if (isProduction)
                    "https://coinninja.com/buybitcoin/quickbuy"
                else
                    "https://test.coinninja.net/buybitcoin/quickbuy"
                ))
                .buildUpon()
                .appendQueryParameter("address", address)
                .build()
    }

}
