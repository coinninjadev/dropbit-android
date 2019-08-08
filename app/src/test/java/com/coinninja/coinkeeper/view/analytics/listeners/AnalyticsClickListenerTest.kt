package com.coinninja.coinkeeper.view.analytics.listeners

import android.view.View

import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.nhaarman.mockitokotlin2.*

import org.junit.Test

class AnalyticsClickListenerTest {

    private var view: View = mock()
    private val analytics: Analytics = mock()
    private fun createListener(): AnalyticsClickListener = AnalyticsClickListener(analytics)

    @Test
    fun track_EVENT_BUTTON_REQUEST_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_REQUEST
        val sampleEventID = R.id.request_btn
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_BUTTON_SCAN_QR_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SCAN_QR
        val sampleEventID = R.id.scan_btn
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_BUTTON_PAY_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_PAY
        val sampleEventID = R.id.send_btn
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_BUTTON_SETTINGS_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SETTINGS
        val sampleEventID = R.id.drawer_setting
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_BUTTON_SPEND_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SPEND
        val sampleEventID = R.id.drawer_where_to_buy
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_BUTTON_SUPPORT_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SUPPORT
        val sampleEventID = R.id.drawer_support
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_BUTTON_SEND_REQUEST_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SEND_REQUEST
        val sampleEventID = R.id.request_funds
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_BUTTON_CONTACTS_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_CONTACTS
        val sampleEventID = R.id.contacts_btn
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_BUTTON_SCAN_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SCAN
        val sampleEventID = R.id.twitter_contacts_button
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_BUTTON_PASTE_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_PASTE
        val sampleEventID = R.id.paste_address_btn
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_BUTTON_SHARE_TRANS_ID_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SHARE_TRANS_ID
        val sampleEventID = R.id.share_transaction
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun track_EVENT_CANCEL_DROPBIT_PRESSED_event_test() {
        val analyticsClickListener = createListener()
        val expectedAnalyticsEvent = Analytics.EVENT_CANCEL_DROPBIT_PRESSED
        val sampleEventID = R.id.button_cancel_dropbit
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent)
    }

    @Test
    fun if_rID_not_a_tracked_event_do_nothing() {
        val analyticsClickListener = createListener()
        val sampleEventID = -3221324
        setId(sampleEventID)

        analyticsClickListener.onClick(view)

        verify(analytics, times(0)).trackButtonEvent(any())
    }

    private fun setId(id: Int) {
        whenever(view.id).thenReturn(id)
    }
}