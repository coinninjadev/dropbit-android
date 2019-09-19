package com.coinninja.coinkeeper.ui.home

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.PagerAdapter
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable

@Mockable
class HomePagerAdapterProvider constructor(val thunderDomeRepository: ThunderDomeRepository) {
    fun provide(fragmentManager: FragmentManager, state: Lifecycle.State): PagerAdapter? {
        return HomePagerAdapter(fragmentManager, state.ordinal, isLightningLocked = thunderDomeRepository.isLocked)
    }

}
