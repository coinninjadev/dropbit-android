package com.coinninja.coinkeeper.ui.lightning.locked

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.BaseFragment

class LightningLockedFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lightning_locked, container, false)
    }

}
