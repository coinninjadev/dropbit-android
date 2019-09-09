package com.coinninja.coinkeeper.ui.home

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.ui.lightning.history.LightningHistoryFragment
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryFragment

@Mockable
class HomePagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
    companion object {
        const val numPages = 2
    }

    private val fragments = listOf(TransactionHistoryFragment(), LightningHistoryFragment())

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = numPages

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
    }
}

