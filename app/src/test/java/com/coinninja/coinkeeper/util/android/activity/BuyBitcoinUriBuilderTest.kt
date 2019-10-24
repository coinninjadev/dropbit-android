package com.coinninja.coinkeeper.util.android.activity

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BuyBitcoinUriBuilderTest {

    @Test
    fun environ_test() {
        assertThat(BuyBitcoinUriBuilder(false)
                .build("--address--").toString())
                .isEqualTo("https://test.coinninja.net/buybitcoin/quickbuy?address=--address--")
    }

    @Test
    fun environ_prod() {
        assertThat(BuyBitcoinUriBuilder()
                .build("--address--").toString())
                .isEqualTo("https://coinninja.com/buybitcoin/quickbuy?address=--address--")
    }


}