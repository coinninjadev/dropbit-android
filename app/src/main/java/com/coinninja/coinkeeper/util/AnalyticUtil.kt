package com.coinninja.coinkeeper.util

import android.app.Activity
import androidx.fragment.app.Fragment
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject
import javax.inject.Inject

@Mockable
class AnalyticUtil @Inject constructor(
        internal val analytics: MixpanelAPI
) : Analytics {

    override fun start(): Analytics {
        if (!analytics.people.isIdentified) {
            analytics.identify(analytics.distinctId)
            analytics.people.identify(analytics.distinctId)
            analytics.people.set(Analytics.PROPERTY_HAS_WALLET, false)
            analytics.people.set(Analytics.PROPERTY_PHONE_VERIFIED, false)
            analytics.people.set(Analytics.PROPERTY_HAS_WALLET_BACKUP, false)
            analytics.people.set(Analytics.PROPERTY_HAS_SENT_DROPBIT, false)
            analytics.people.set(Analytics.PROPERTY_HAS_SENT_ADDRESS, false)
            analytics.people.set(Analytics.PROPERTY_HAS_BTC_BALANCE, false)
            analytics.people.set(Analytics.PROPERTY_HAS_RECEIVED_ADDRESS, false)
            analytics.people.set(Analytics.PROPERTY_HAS_RECEIVED_DROPBIT, false)
        }
        return this
    }

    override fun onActivityStop(activity: Activity) {
        analytics.flush()
    }

    override fun trackFragmentStop(fragment: Fragment) {
        analytics.flush()
    }

    override fun trackEvent(event: String) {
        analytics.track(event)
    }


    override fun trackEvent(event: String, properties: JSONObject) {
        analytics.track(event, properties)
    }

    override fun trackButtonEvent(event: String) {
        analytics.track(event + Analytics.Companion.EVENT_BUTTON_SUFFIX)
    }

    override fun setUserProperty(propertyName: String, value: Boolean) {
        analytics.people.set(propertyName, value)
    }

    override fun setUserProperty(propertyName: String, value: String) {
        analytics.people.set(propertyName, value)
    }

    override fun setUserProperty(propertyName: String, value: Long) {
        analytics.people.set(propertyName, value)
    }

    override fun flush() {
        analytics.flush()
    }

}
