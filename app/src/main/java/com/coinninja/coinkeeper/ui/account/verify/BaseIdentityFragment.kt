package com.coinninja.coinkeeper.ui.account.verify

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.coinninja.android.helpers.Resources
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.coinninja.coinkeeper.view.widget.AccountIdentityView

abstract class BaseIdentityFragment : BaseFragment() {

    private val onConfirmedNoticeListener = DialogInterface.OnClickListener { dialog, which ->
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                onDeVerify()
                dialog?.dismiss()
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                dialog?.dismiss()
            }
        }
    }

    abstract fun onVerify()
    abstract fun onDeVerify()

    abstract fun primaryWarning(): String
    abstract fun primaryMessage(): String
    abstract fun followUpMessage(): String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_identity_fragment, container, false)
    }

    internal fun getVerificationButton(): Button? = view?.findViewById(R.id.verify_button)

    internal fun getIdentityView(): AccountIdentityView? = view?.findViewById(R.id.verified_identity_view)

    internal fun getRemoveVerificationView(): Button? = view?.findViewById(R.id.remove_verification)


    internal fun configureVerified(identity: String) {
        getVerificationButton()?.apply {
            visibility = View.GONE
        }

        getIdentityView()?.apply {
            setVerifiedAccountName(identity)
            visibility = View.VISIBLE
        }

        getRemoveVerificationView()?.apply {
            visibility = View.VISIBLE
            setOnClickListener { v -> confirmDeVerification() }
        }
    }

    internal fun configureNotVerified() {
        getVerificationButton()?.apply {
            visibility = View.VISIBLE
        }

        getIdentityView()?.apply {
            verifiedAccountName = ""
            visibility = View.GONE
        }

        getRemoveVerificationView()?.apply {
            visibility = View.GONE
            setOnClickListener { }
        }
    }

    private fun confirmDeVerification() {
        activity?.supportFragmentManager?.let { fragmentManager ->
            val view = layoutInflater.inflate(R.layout.dialog_remove_identity, view as ViewGroup, false)
            view.findViewById<TextView>(R.id.warning).text = primaryWarning()
            view.findViewById<TextView>(R.id.message).text = primaryMessage()
            view.findViewById<Button>(R.id.ok).setOnClickListener { onConfirmedDeVerificationNotice() }
            GenericAlertDialog.newInstance(view, true, true)
                    .show(fragmentManager, "CONFIRM_DEVERIFICATION_NOTICE")
        }
    }

    private fun onConfirmedDeVerificationNotice() {
        var positiveButtonText = ""
        var negativeButtonText = ""

        context?.let {
            positiveButtonText = Resources.getString(it, R.string.deverification_dialog_are_you_sure_positive)
            negativeButtonText = Resources.getString(it, R.string.deverification_dialog_are_you_sure_negative)
        }

        activity?.supportFragmentManager?.let { fragmentManager ->
            (fragmentManager.findFragmentByTag("CONFIRM_DEVERIFICATION_NOTICE") as GenericAlertDialog).dismiss()
            GenericAlertDialog.newInstance(
                    followUpMessage(),
                    positiveButtonText,
                    negativeButtonText,
                    onConfirmedNoticeListener
            ).show(fragmentManager, "CONFIRM_DEVERIFICATION_CONFIRMATION_NOTICE")
        }
    }
}
