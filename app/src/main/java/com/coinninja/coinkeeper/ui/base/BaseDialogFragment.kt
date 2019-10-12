package com.coinninja.coinkeeper.ui.base

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

open class BaseDialogFragment : DialogFragment(), HasSupportFragmentInjector {

    @Inject
    internal lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var activityNavigationUtil: ActivityNavigationUtil


    private var progressSpinner: AlertDialog? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment>? {
        return childFragmentInjector
    }

    fun showIndeterminantProgress() {
        activity?.let {
            progressSpinner = AlertDialogBuilder.buildIndefiniteProgress(it as AppCompatActivity)
        }
    }

    fun hideIndeterminantProgress() {
        progressSpinner?.let {
            it.dismiss()
            progressSpinner = null
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : View> findViewById(resourceId: Int): T? {
        return view?.findViewById<View>(resourceId) as T?
    }
}
