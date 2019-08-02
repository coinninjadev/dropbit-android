package com.coinninja.coinkeeper.util

import androidx.appcompat.app.AppCompatActivity
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.analytics.AnalyticsBalanceRange
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.json.JSONException
import org.json.JSONObject
import org.junit.Test

class AnalyticUtilTest {

    private fun createUtil(): AnalyticUtil {
        val util = AnalyticUtil(mock())
        whenever(util.analytics.people).thenReturn(mock())
        whenever(util.analytics.people.isIdentified).thenReturn(true)
        whenever(util.analytics.distinctId).thenReturn(UUID)
        return util
    }

    @Test
    @Throws(JSONException::class)
    fun exposes_tracking_events_with_properties() {
        val util = createUtil()
        val props = JSONObject()
        props.put("Foo", "bar")

        util.trackEvent("eventName", props)

        verify(util.analytics).track("eventName", props)
    }

    @Test
    fun exposes_flush() {
        val util = createUtil()
        util.flush()

        verify(util.analytics).flush()
    }

    @Test
    fun sets_boolean_property_on_user() {
        val util = createUtil()
        util.setUserProperty(Analytics.PROPERTY_HAS_WALLET, true)

        verify(util.analytics.people).set(Analytics.PROPERTY_HAS_WALLET, true)
    }

    @Test
    fun only_identifies_once() {
        val util = createUtil()
        util.start()
        verify(util.analytics, times(0)).identify(UUID)
        verify(util.analytics.people, times(0)).identify(UUID)
        verify(util.analytics.people, times(0)).set(Analytics.PROPERTY_HAS_WALLET, false)
        verify(util.analytics.people, times(0)).set(Analytics.PROPERTY_HAS_BTC_BALANCE, false)
        verify(util.analytics.people, times(0)).set(Analytics.PROPERTY_HAS_RECEIVED_ADDRESS, false)
        verify(util.analytics.people, times(0)).set(Analytics.PROPERTY_HAS_RECEIVED_DROPBIT, false)
        verify(util.analytics.people, times(0)).set(Analytics.PROPERTY_HAS_SENT_ADDRESS, false)
        verify(util.analytics.people, times(0)).set(Analytics.PROPERTY_HAS_SENT_DROPBIT, false)
        verify(util.analytics.people, times(0)).set(Analytics.PROPERTY_HAS_WALLET_BACKUP, false)
        verify(util.analytics.people, times(0)).set(Analytics.PROPERTY_PHONE_VERIFIED, false)
    }

    @Test
    fun starting_assigns_uuid_on_session() {
        val util = createUtil()
        whenever(util.analytics.people.isIdentified).thenReturn(false)

        util.start()

        verify(util.analytics).identify(UUID)
        verify(util.analytics.people).identify(UUID)
        verify(util.analytics.people).set(Analytics.PROPERTY_HAS_WALLET, false)
    }

    @Test
    fun flushes_when_activity_reports_done() {
        val util = createUtil()
        util.onActivityStop(AppCompatActivity())
        verify(util.analytics).flush()
    }

    @Test
    fun tracks_events_without_properties() {
        val util = createUtil()
        util.trackEvent(Analytics.EVENT_BROADCAST_COMPLETE)

        verify(util.analytics).track(Analytics.EVENT_BROADCAST_COMPLETE)
    }

    @Test
    fun track_button_events_test() {
        val util = createUtil()
        util.trackButtonEvent(Analytics.EVENT_BUTTON_HISTORY)

        verify(util.analytics).track(Analytics.EVENT_BUTTON_HISTORY + Analytics.EVENT_BUTTON_SUFFIX)
    }

    @Test
    fun track_button_events_add_suffix_to_each_button_event_test() {
        val util = createUtil()
        val expectedButtonSuffix = "Btn"

        util.trackButtonEvent("Some random button event")

        verify(util.analytics).track("Some random button event$expectedButtonSuffix")
    }

    @Test
    fun proxies_boolean_properties() {
        val util = createUtil()

        util.setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, true)

        verify(util.analytics.people).set(Analytics.PROPERTY_HAS_BTC_BALANCE, true)
    }

    @Test
    fun proxies_string_properties() {
        val util = createUtil()

        util.setUserProperty(Analytics.PROPERTY_RELATIVE_WALLET_RANGE, AnalyticsBalanceRange.UNDER_CENTI_BTC.label)

        verify(util.analytics.people).set(Analytics.PROPERTY_RELATIVE_WALLET_RANGE, AnalyticsBalanceRange.UNDER_CENTI_BTC.label)
    }

    companion object {
        val UUID = "UUID -- 585-4558-2563-215dfghg-fhf-"
    }
}