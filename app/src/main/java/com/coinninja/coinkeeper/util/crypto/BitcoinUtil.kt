package com.coinninja.coinkeeper.util.crypto

import android.content.Context
import app.coinninja.cn.libbitcoin.AddressUtil
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.R.array
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext
import java.util.*
import javax.inject.Inject

@Mockable
class BitcoinUtil @Inject constructor(
        @ApplicationContext internal val context: Context,
        internal val addressUtil: AddressUtil
) {

    fun isValidBIP39Words(seedWords: Array<String>): Boolean {
        if (seedWords.size != 12) return false
        val words: List<String> = Arrays.asList(*context.resources.getStringArray(array.recovery_words))
        return words.containsAll(Arrays.asList<String>(*seedWords))
    }

    fun isValidBTCAddress(address: String): Boolean {
        return (addressUtil.isSegwit(address) || addressUtil.isBase58(address))
    }
}