package com.coinninja.coinkeeper.util;

import android.app.Activity;

import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class AnalyticUtil implements Analytics {

    private MixpanelAPI analytics;

    public AnalyticUtil(MixpanelAPI analytics) {
        this.analytics = analytics;
    }

    @Override
    public Analytics start() {
        if (!analytics.getPeople().isIdentified()) {
            analytics.identify(analytics.getDistinctId());
            analytics.getPeople().identify(analytics.getDistinctId());
            analytics.getPeople().set(Analytics.PROPERTY_HAS_WALLET, false);
            analytics.getPeople().set(Analytics.PROPERTY_PHONE_VERIFIED, false);
            analytics.getPeople().set(Analytics.PROPERTY_HAS_WALLET_BACKUP, false);
            analytics.getPeople().set(Analytics.PROPERTY_HAS_SENT_DROPBIT, false);
            analytics.getPeople().set(Analytics.PROPERTY_HAS_SENT_ADDRESS, false);
            analytics.getPeople().set(Analytics.PROPERTY_HAS_BTC_BALANCE, false);
            analytics.getPeople().set(Analytics.PROPERTY_HAS_RECEIVED_ADDRESS, false);
            analytics.getPeople().set(Analytics.PROPERTY_HAS_RECEIVED_DROPBIT, false);
        }
        return this;
    }

    @NonNull
    @Override
    public void onActivityStop(Activity activity) {
        analytics.flush();
    }

    @NonNull
    @Override
    public void trackFragmentStop(Fragment fragment) {
        analytics.flush();
    }

    @NonNull
    @Override
    public void trackEvent(String event) {
        analytics.track(event);
    }


    @NonNull
    @Override
    public void trackEvent(String event, JSONObject properties) {
        analytics.track(event, properties);
    }

    @NonNull
    @Override
    public void trackButtonEvent(String event) {
        analytics.track(event + EVENT_BUTTON_SUFFIX);
    }

    @NonNull
    @Override
    public void setUserProperty(String propertyName, boolean value) {
        analytics.getPeople().set(propertyName, value);
    }

    @Override
    public void flush() {
        analytics.flush();
    }
}
