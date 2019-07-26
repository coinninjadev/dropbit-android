package com.coinninja.coinkeeper.ui.payment.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.BaseFragment

class PayRequestScreenFragment : BaseFragment() {
    companion object {
        const val fragmentTag = "pay-request-screen"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_pay_request_screen, container, false)
        val fragment = PayRequestFragment()
        childFragmentManager.beginTransaction().apply {
            add(R.id.payment_request_screen_fragment_holder, fragment, fragmentTag)
            commit()
        }
        return view
    }

}
