package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CNSubscriptionState {

    @SerializedName("available_topics")
    List<CNTopic> availableTopics;

    List<CNSubscription> subscriptions;

    public List<CNTopic> getAvailableTopics() {
        return availableTopics;
    }

    public List<CNSubscription> getSubscriptions() {
        return subscriptions;
    }

    public void setAvailableTopics(List<CNTopic> topics) {
        availableTopics = topics;
    }

    public void setSubscriptions(List<CNSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
