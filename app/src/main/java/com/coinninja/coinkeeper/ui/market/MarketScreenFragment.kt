package com.coinninja.coinkeeper.ui.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.ui.news.MarketNewsFragment

class MarketScreenFragment : BaseFragment() {
    companion object {
        const val marketChartsFragmentTag = "MarketChartsFragment"
        const val marketNewsFragmentTag = "MarketNewsFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_market_screen, container, false)
        childFragmentManager.beginTransaction().apply {
            add(R.id.market_charts, MarketChartsFragment(), marketChartsFragmentTag)
            add(R.id.market_news, MarketNewsFragment(), marketNewsFragmentTag)
            commit()
        }

        return view
    }

}