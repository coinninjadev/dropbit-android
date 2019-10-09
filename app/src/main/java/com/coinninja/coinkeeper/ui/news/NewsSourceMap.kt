package com.coinninja.coinkeeper.ui.news

import com.coinninja.coinkeeper.R

class NewsSourceMap {

    fun provide(source: String?): Int? {
        when (source?.toLowerCase()?.trim()) {
            "cn" -> return R.drawable.cn
            "coinninja" -> return R.drawable.cn
            "ccn" -> return R.drawable.ccn
            "ambcrypto" -> return R.drawable.amb
            "cointelegraph" -> return R.drawable.cointelegraph
            "reddit" -> return R.drawable.reddit
            "coindesk" -> return R.drawable.coindesk
            "theblock" -> return R.drawable.the_block
            "bitcoinmagazine" -> return R.drawable.bitcoin_magazine
            else -> return null
        }
    }

}
