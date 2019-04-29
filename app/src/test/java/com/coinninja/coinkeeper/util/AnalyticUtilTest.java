package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import androidx.appcompat.app.AppCompatActivity;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnalyticUtilTest {
    public static final String UUID = "UUID -- 585-4558-2563-215dfghg-fhf-";

    @Mock
    MixpanelAPI analytics;

    @Mock
    MixpanelAPI.People people;

    @InjectMocks
    private AnalyticUtil util;

    @Before
    public void setup() {
        when(analytics.getPeople()).thenReturn(people);
        when(people.isIdentified()).thenReturn(true);
        when(analytics.getDistinctId()).thenReturn(UUID);
    }

    @Test
    public void exposes_tracking_events_with_properties() throws JSONException {
        JSONObject props = new JSONObject();
        props.put("Foo", "bar");

        util.trackEvent("eventName", props);

        verify(analytics).track("eventName", props);
    }

    @Test
    public void exposes_flush() {
        util.flush();

        verify(analytics).flush();
    }

    @Test
    public void sets_boolean_property_on_user() {
        util.setUserProperty(Analytics.PROPERTY_HAS_WALLET, true);

        verify(people).set(Analytics.PROPERTY_HAS_WALLET, true);
    }

    @Test
    public void only_identifies_once() {
        util.start();
        verify(analytics, times(0)).identify(UUID);
        verify(people, times(0)).identify(UUID);
        verify(people, times(0)).set(Analytics.PROPERTY_HAS_WALLET, false);
        verify(people, times(0)).set(Analytics.PROPERTY_HAS_BTC_BALANCE, false);
        verify(people, times(0)).set(Analytics.PROPERTY_HAS_RECEIVED_ADDRESS, false);
        verify(people, times(0)).set(Analytics.PROPERTY_HAS_RECEIVED_DROPBIT, false);
        verify(people, times(0)).set(Analytics.PROPERTY_HAS_SENT_ADDRESS, false);
        verify(people, times(0)).set(Analytics.PROPERTY_HAS_SENT_DROPBIT, false);
        verify(people, times(0)).set(Analytics.PROPERTY_HAS_WALLET_BACKUP, false);
        verify(people, times(0)).set(Analytics.PROPERTY_PHONE_VERIFIED, false);
    }

    @Test
    public void starting_assignes_uuid_on_session() {
        when(people.isIdentified()).thenReturn(false);

        util.start();

        verify(analytics).identify(UUID);
        verify(people).identify(UUID);
        verify(people).set(Analytics.PROPERTY_HAS_WALLET, false);
    }

    @Test
    public void flushes_when_activity_reports_done() {
        util.onActivityStop(new AppCompatActivity());
        verify(analytics).flush();
    }

    @Test
    public void tracks_events_without_properties() {
        util.trackEvent(Analytics.EVENT_BROADCAST_COMPLETE);

        verify(analytics).track(Analytics.EVENT_BROADCAST_COMPLETE);
    }

    @Test
    public void track_button_events_test() {
        util.trackButtonEvent(Analytics.EVENT_BUTTON_HISTORY);

        verify(analytics).track(Analytics.EVENT_BUTTON_HISTORY + Analytics.EVENT_BUTTON_SUFFIX);
    }

    @Test
    public void track_button_events_add_suffix_to_each_button_event_test() {
        String expectedButtonSuffix = "Btn";

        util.trackButtonEvent("Some random button event");

        verify(analytics).track("Some random button event" + expectedButtonSuffix);
    }
}