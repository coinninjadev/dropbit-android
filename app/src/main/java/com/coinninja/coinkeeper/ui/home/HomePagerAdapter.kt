package com.coinninja.coinkeeper.ui.home

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.ui.lightning.history.LightningHistoryFragment
import com.coinninja.coinkeeper.ui.lightning.locked.LightningLockedFragment
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryFragment

@Mockable
class HomePagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
    companion object {
        const val numPages = 2
    }

    var isLightningLocked = true
    private var fragments = listOf(TransactionHistoryFragment(), LightningHistoryFragment(), LightningLockedFragment())

    override fun getItem(position: Int): Fragment {
        return fragments[if (position == 1 && isLightningLocked) 2 else position]
    }

    override fun getCount(): Int = numPages

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
    }
}

