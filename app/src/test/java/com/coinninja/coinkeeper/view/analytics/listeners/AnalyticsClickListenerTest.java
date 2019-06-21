package com.coinninja.coinkeeper.view.analytics.listeners;

import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnalyticsClickListenerTest {
    @Mock
    View view;

    @Mock
    private Analytics analytics;

    @InjectMocks
    private AnalyticsClickListener analyticsClickListener;

    @Test
    public void track_EVENT_BUTTON_REQUEST_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_REQUEST;
        int sampleEventID = R.id.request_btn;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_BALANCE_HISTORY_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_BALANCE_HISTORY;
        int sampleEventID = R.id.balance;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_SCAN_QR_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SCAN_QR;
        int sampleEventID = R.id.scan_btn;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_PAY_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_PAY;
        int sampleEventID = R.id.send_btn;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_SETTINGS_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SETTINGS;
        int sampleEventID = R.id.drawer_setting;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_SPEND_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SPEND;
        int sampleEventID = R.id.drawer_where_to_buy;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_SUPPORT_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SUPPORT;
        int sampleEventID = R.id.drawer_support;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_SEND_REQUEST_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SEND_REQUEST;
        int sampleEventID = R.id.request_funds;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_CONTACTS_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_CONTACTS;
        int sampleEventID = R.id.contacts_btn;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_SCAN_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SCAN;
        int sampleEventID = R.id.twitter_contacts_button;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_PASTE_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_PASTE;
        int sampleEventID = R.id.paste_address_btn;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_BUTTON_SHARE_TRANS_ID_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_BUTTON_SHARE_TRANS_ID;
        int sampleEventID = R.id.share_transaction;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void track_EVENT_CANCEL_DROPBIT_PRESSED_event_test() {
        String expectedAnalyticsEvent = Analytics.EVENT_CANCEL_DROPBIT_PRESSED;
        int sampleEventID = R.id.button_cancel_dropbit;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics).trackButtonEvent(expectedAnalyticsEvent);
    }

    @Test
    public void if_rID_not_a_tracked_event_do_nothing() {
        int sampleEventID = -3221324;
        setId(sampleEventID);

        analyticsClickListener.onClick(view);

        verify(analytics, times(0)).trackButtonEvent(anyString());
    }

    private void setId(int id) {
        when(view.getId()).thenReturn(id);
    }
}