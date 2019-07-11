package com.coinninja.coinkeeper.ui.home

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.ui.market.MarketScreenFragment
import com.coinninja.coinkeeper.ui.payment.request.PayRequestScreenFragment
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryFragment

@Mockable
class HomePagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
    companion object {
        const val numPages = 3
    }

    private val fragments = listOf<Fragment>(MarketScreenFragment(), TransactionHistoryFragment(), PayRequestScreenFragment())

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return fragments[0]
            1 -> return fragments[1]
            else -> return fragments[2]
        }
    }

    override fun getCount(): Int = numPages

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
    }
}

