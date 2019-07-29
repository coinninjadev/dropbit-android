package com.coinninja.coinkeeper.ui.home

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryFragment

@Mockable
class HomePagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
    companion object {
        const val numPages = 1
    }

    private val fragments = listOf<Fragment>(TransactionHistoryFragment())

    override fun getItem(position: Int): Fragment = fragments[0]

    override fun getCount(): Int = numPages

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
    }
}

