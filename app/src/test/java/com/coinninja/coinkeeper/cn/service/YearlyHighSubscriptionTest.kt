package com.coinninja.coinkeeper.cn.service

import com.coinninja.coinkeeper.cn.service.testData.YEARLY_HIGH_DATA
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times

class YearlyHighSubscriptionTest {
    val yearlyHighSubscription = YearlyHighSubscription(mock(PushNotificationServiceManager::class.java), mock(PreferencesUtil::class.java))

    @Nested
    @DisplayName("Given user is subscribed to the yearly high notifications")
    inner class SubscribedToYearlyHigh {

        @BeforeEach
        fun setup() {
            val subscriptionState = Gson().fromJson<CNSubscriptionState>(YEARLY_HIGH_DATA.SUBSCRIBED_TO_YEARLY_HIGH, CNSubscriptionState::class.java)
            whenever(yearlyHighSubscription.pushNotificationServiceManager.getSubscriptionState()).thenReturn(subscriptionState)
        }

        @Test
        @DisplayName("isSubscribed returns true")
        internal fun `isSubscribed returns true`() {
            assertThat(yearlyHighSubscription.isSubscribed()).isEqualTo(true)
        }

        @Test
        @DisplayName("can unsubscribeFrom from yearly high")
        internal fun `can unsubscribe from yearly high`() {
            yearlyHighSubscription.unsubscribe()

            verify(yearlyHighSubscription.pushNotificationServiceManager).unsubscribeFrom("--btc-high-topic-id--")
        }

        @Test
        @DisplayName("subscribing does nothing")
        internal fun `subscribing does nothing`() {
            yearlyHighSubscription.subscribe()

            verify(yearlyHighSubscription.pushNotificationServiceManager, times(0)).subscribeTo(any())
        }
    }

    @Nested
    @DisplayName("Given user is NOT subscribed to the yearly high notifications")
    inner class NotSubscribedToYearlyHigh {

        @BeforeEach
        fun setup() {
            val subscriptionState = Gson().fromJson(YEARLY_HIGH_DATA.NOT_SUBSCRIBED_TO_YEARLY_HIGH, CNSubscriptionState::class.java)
            whenever(yearlyHighSubscription.pushNotificationServiceManager.getSubscriptionState()).thenReturn(subscriptionState)
        }

        @Test
        @DisplayName("isSubscribed returns false")
        internal fun `isSubscribed returns false`() {
            assertThat(yearlyHighSubscription.isSubscribed()).isEqualTo(false)
        }

        @Test
        @DisplayName("unsubscribeFrom is a no-op")
        internal fun `unsubscribe is a no-op`() {
            yearlyHighSubscription.unsubscribe()

            verify(yearlyHighSubscription.pushNotificationServiceManager, times(0)).unsubscribeFrom(any())
            verify(yearlyHighSubscription.pushNotificationServiceManager, times(1)).getSubscriptionState()
        }

        @Test
        @DisplayName("subscribing delegates to push notification manager")
        internal fun `subscribing delegates to push notification manager`() {
            yearlyHighSubscription.subscribe()

            verify(yearlyHighSubscription.pushNotificationServiceManager).subscribeTo("--btc-high-topic-id--")
        }
    }

}