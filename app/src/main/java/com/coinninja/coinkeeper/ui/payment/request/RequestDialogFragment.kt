package com.coinninja.coinkeeper.ui.payment.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment

class RequestDialogFragment : BaseBottomDialogFragment() {
    companion object {
        const val fragmentTag = "request-dialog-pay-request-screen"
    }

    override fun onStart() {
        super.onStart()
        view?.findViewById<View>(R.id.close)?.setOnClickListener { dismiss() }
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.fragment_request_dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        val fragment = PayRequestFragment()
        childFragmentManager.beginTransaction().apply {
            add(R.id.request_fragment_frame, fragment, fragmentTag)
            commit()
        }
        return root
    }
}
