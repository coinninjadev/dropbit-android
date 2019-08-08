package com.coinninja.coinkeeper.view.analytics.listeners

import android.view.View
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.util.analytics.Analytics
import java.util.*

@Mockable
class AnalyticsClickListener(
        internal val analytics: Analytics
) : View.OnClickListener {

    override fun onClick(view: View) {
        val rID = view.id
        val event = getButtonEvent(rID) ?: return

        analytics.trackButtonEvent(event)
    }

    private fun getButtonEvent(rID: Int): String? {
        val event = BUTTON_ANALYTICS_EVENT_MAP[rID]
        return if (event == null || event.isEmpty()) {
            null
        } else {
            event
        }
    }

    companion object {
        private val BUTTON_ANALYTICS_EVENT_MAP: HashMap<Int, String> = HashMap()

        init {
            BUTTON_ANALYTICS_EVENT_MAP[R.id.request_btn] = Analytics.EVENT_BUTTON_REQUEST
            BUTTON_ANALYTICS_EVENT_MAP[R.id.scan_btn] = Analytics.EVENT_BUTTON_SCAN_QR
            BUTTON_ANALYTICS_EVENT_MAP[R.id.send_btn] = Analytics.EVENT_BUTTON_PAY
            BUTTON_ANALYTICS_EVENT_MAP[R.id.drawer_setting] = Analytics.EVENT_BUTTON_SETTINGS
            BUTTON_ANALYTICS_EVENT_MAP[R.id.drawer_where_to_buy] = Analytics.EVENT_BUTTON_SPEND
            BUTTON_ANALYTICS_EVENT_MAP[R.id.drawer_support] = Analytics.EVENT_BUTTON_SUPPORT
            BUTTON_ANALYTICS_EVENT_MAP[R.id.request_funds] = Analytics.EVENT_BUTTON_SEND_REQUEST
            BUTTON_ANALYTICS_EVENT_MAP[R.id.contacts_btn] = Analytics.EVENT_BUTTON_CONTACTS
            BUTTON_ANALYTICS_EVENT_MAP[R.id.twitter_contacts_button] = Analytics.EVENT_BUTTON_SCAN
            BUTTON_ANALYTICS_EVENT_MAP[R.id.paste_address_btn] = Analytics.EVENT_BUTTON_PASTE
            BUTTON_ANALYTICS_EVENT_MAP[R.id.share_transaction] = Analytics.EVENT_BUTTON_SHARE_TRANS_ID
            BUTTON_ANALYTICS_EVENT_MAP[R.id.button_cancel_dropbit] = Analytics.EVENT_CANCEL_DROPBIT_PRESSED
        }
    }
}
