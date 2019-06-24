package com.coinninja.coinkeeper.cn.service

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import javax.inject.Inject

@Mockable
class YearlyHighSubscription @Inject constructor(
        val pushNotificationServiceManager: PushNotificationServiceManager,
        val preferencesUtil: PreferencesUtil) {

    fun isSubscribed(): Boolean {
        var subscribed = false
        val subscriptionState = pushNotificationServiceManager.getSubscriptionState()

        val btcHighId = topicIdFromState(subscriptionState) ?: ""
        subscriptionState?.let {
            if (btcHighId.isNotEmpty()) {
                it.subscriptions.forEach { subscription ->
                    if (subscription.ownerId == btcHighId)
                        subscribed = true
                }
            }
        }

        return subscribed
    }

    fun unsubscribe() {
        if (isSubscribed()) {
            val topicId = topicIdFromState(pushNotificationServiceManager.getSubscriptionState())
            if (!topicId.isNullOrEmpty()) {
                pushNotificationServiceManager.unsubscribeFrom(topicId)
            }
        }
    }

    fun subscribe() {
        if (!isSubscribed()) {
            val topicId = topicIdFromState(pushNotificationServiceManager.getSubscriptionState())
            if (!topicId.isNullOrEmpty()) {
                pushNotificationServiceManager.subscribeTo(topicId)
            }
        }
    }

    fun topicIdFromState(subscriptionState: CNSubscriptionState?): String? {
        subscriptionState?.let {
            it.availableTopics.forEach topic@{ topic ->
                if (topic.name == "btc_high") {
                    return topic.id
                }
            }
        }
        return null
    }
}
