package com.coinninja.coinkeeper.ui.account.verify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import com.coinninja.android.helpers.Resources.getDrawable
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import javax.inject.Inject

class PhoneIdentityFragment : BaseIdentityFragment() {

    override fun primaryWarning(): String =
            context?.getString(R.string.deverification_dialog_pending_dropbit_canceled_warning_message)
                    ?: ""

    override fun primaryMessage(): String =
            context?.getString(R.string.deverification_dialog_pending_dropbit_canceled_message)
                    ?: ""

    override fun followUpMessage(): String =
            context?.getString(R.string.deverification_message_are_you_sure) ?: ""

    @Inject
    lateinit var dropbitAccountHelper: DropbitAccountHelper


    @Inject
    lateinit var localBroadCastUtil: LocalBroadCastUtil

    @Inject
    lateinit var serviceWorkUtil: ServiceWorkUtil

    @Inject
    lateinit var activityNavigationUtil: ActivityNavigationUtil

    internal val intentFilter = IntentFilter(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED)

    internal val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED -> {
                    analytics.setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, false)
                    configureNotVerified()
                    activity?.let {
                        if (it is UserAccountVerificationActivity) {
                            it.invalidateCacheView()
                        }
                    }
                }
                DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED -> Toast.makeText(context,
                        getString(R.string.deverification_phone_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentFilter.addAction(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED)
    }

    override fun onStop() {
        super.onStop()
        localBroadCastUtil.unregisterReceiver(receiver)
    }

    override fun onVerify() {
        context?.let {
            analytics.setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, true)
            activityNavigationUtil.navigateToRegisterPhone(it)
        }
    }

    override fun onDeVerify() {
        localBroadCastUtil.registerReceiver(receiver, intentFilter)
        serviceWorkUtil.deVerifyPhoneNumber()
    }

    override fun onStart() {
        super.onStart()
        configureForPhone()
    }

    override fun onResume() {
        super.onResume()
        if (dropbitAccountHelper.isPhoneVerified) {
            val identity = dropbitAccountHelper.phoneIdentity()
            if (identity != null) {
                configureVerified(PhoneNumber(identity.identity).toNationalDisplayText())
            }
        }
    }

    private fun configureForPhone() {
        getVerificationButton()?.apply {
            text = getString(R.string.verify_phone_button_title)
            setCompoundDrawables(getDrawable(context, R.drawable.ic_phone), null, null, null)
            background = getDrawable(context, R.drawable.cta_button)
            setOnClickListener { onVerify() }
        }

        getIdentityView()?.apply {
            setImage(R.drawable.ic_phone)
        }
    }
}


