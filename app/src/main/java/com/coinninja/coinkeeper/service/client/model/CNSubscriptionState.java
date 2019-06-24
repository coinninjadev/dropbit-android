package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CNSubscriptionState {

    @SerializedName("available_topics")
    List<CNTopic> availableTopics = new ArrayList<>();

    List<CNSubscription> subscriptions = new ArrayList<>();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CNSubscriptionState that = (CNSubscriptionState) o;
        return Objects.equals(availableTopics, that.availableTopics) &&
                Objects.equals(subscriptions, that.subscriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(availableTopics, subscriptions);
    }
}
