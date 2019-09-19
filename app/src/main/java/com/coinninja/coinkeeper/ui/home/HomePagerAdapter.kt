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
class HomePagerAdapter(fm: FragmentManager, behavior: Int, val isLightningLocked: Boolean = true) : FragmentStatePagerAdapter(fm, behavior) {
    companion object {
        const val numPages = 2
    }

    @Suppress("LeakingThis")
    private final val lightningFragment: Fragment = if (isLightningLocked) {
        LightningLockedFragment()
    } else {
        LightningHistoryFragment()
    }

    private val fragments = listOf(
            TransactionHistoryFragment(),
            lightningFragment)


    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = numPages

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
    }
}

