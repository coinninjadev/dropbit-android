package com.coinninja.coinkeeper.ui.base

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.coinninja.coinkeeper.util.analytics.Analytics
import dagger.android.support.DaggerFragment
import javax.inject.Inject

open class BaseFragment : DaggerFragment() {

    @Inject
    lateinit var analytics: Analytics

    val creationIntent: Intent get() = activity?.intent ?: Intent()

    @Suppress("UNCHECKED_CAST")
    fun <T : View> findViewById(resourceId: Int): T? {
        return view?.findViewById<View>(resourceId) as T?
    }

    protected fun forceDropKeyboard(view: View) {
        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    protected fun hideKeyboard(view: View) {
        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
