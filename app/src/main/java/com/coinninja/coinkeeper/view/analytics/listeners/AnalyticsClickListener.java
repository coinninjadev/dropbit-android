package com.coinninja.coinkeeper.view.analytics.listeners;

import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import java.util.HashMap;

public class AnalyticsClickListener implements View.OnClickListener {
    private static final HashMap<Integer, String> BUTTON_ANALYTICS_EVENT_MAP;

    static {
        BUTTON_ANALYTICS_EVENT_MAP = new HashMap<>();
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.balance, Analytics.EVENT_BUTTON_BALANCE_HISTORY);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.request_btn, Analytics.EVENT_BUTTON_REQUEST);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.scan_btn, Analytics.EVENT_BUTTON_SCAN_QR);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.send_btn, Analytics.EVENT_BUTTON_PAY);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.drawer_setting, Analytics.EVENT_BUTTON_SETTINGS);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.drawer_where_to_buy, Analytics.EVENT_BUTTON_SPEND);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.drawer_support, Analytics.EVENT_BUTTON_SUPPORT);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.request_funds, Analytics.EVENT_BUTTON_SEND_REQUEST);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.contacts_btn, Analytics.EVENT_BUTTON_CONTACTS);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.scan_btc_address_btn, Analytics.EVENT_BUTTON_SCAN);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.paste_address_btn, Analytics.EVENT_BUTTON_PASTE);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.share_transaction, Analytics.EVENT_BUTTON_SHARE_TRANS_ID);
        BUTTON_ANALYTICS_EVENT_MAP.put(R.id.button_cancel_dropbit, Analytics.EVENT_CANCEL_DROPBIT_PRESSED);
    }

    private final Analytics analytics;

    public AnalyticsClickListener(Analytics analytics) {

        this.analytics = analytics;
    }

    @Override
    public void onClick(View view) {
        if (analytics == null) return;

        int rID = view.getId();
        String event = getButtonEvent(rID);
        if (event == null) return;

        analytics.trackButtonEvent(event);
    }

    protected String getButtonEvent(int rID) {
        String event = BUTTON_ANALYTICS_EVENT_MAP.get(rID);
        if (event == null || event.isEmpty()) {
            return null;
        } else {
            return event;
        }
    }
}
