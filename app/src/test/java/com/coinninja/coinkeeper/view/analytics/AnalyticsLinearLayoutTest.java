package com.coinninja.coinkeeper.view.analytics;

import android.view.View;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.view.analytics.listeners.AnalyticsClickListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class AnalyticsLinearLayoutTest {

    private AnalyticsLinearLayout analyticsLinearLayout;

    @Before
    public void setUp() throws Exception {
        analyticsLinearLayout = new AnalyticsLinearLayout(RuntimeEnvironment.application);
    }

    @Test
    public void insure_analytics_click_event_and_original_click_event_is_engaged_test() {
        AnalyticsClickListener mockAnalyticsClickListener = mock(AnalyticsClickListener.class);
        View.OnClickListener mockOnClickListener = mock(View.OnClickListener.class);
        analyticsLinearLayout.setAnalyticsClickListener(mockAnalyticsClickListener);
        analyticsLinearLayout.setOnClickListener(mockOnClickListener);

        analyticsLinearLayout.performClick();

        verify(mockAnalyticsClickListener).onClick(analyticsLinearLayout);
        verify(mockOnClickListener).onClick(analyticsLinearLayout);
    }


    @Test
    public void insure_analytics_click_event_is_engaged_even_if_original_click_is_null_test() {
        AnalyticsClickListener mockAnalyticsClickListener = mock(AnalyticsClickListener.class);
        analyticsLinearLayout.setAnalyticsClickListener(mockAnalyticsClickListener);
        analyticsLinearLayout.setOnClickListener(null);

        analyticsLinearLayout.performClick();

        verify(mockAnalyticsClickListener).onClick(analyticsLinearLayout);
    }


    @Test
    public void insure_original_click_event_is_engaged_even_if_analytics_click_is_null_test() {
        View.OnClickListener mockOnClickListener = mock(View.OnClickListener.class);
        analyticsLinearLayout.setAnalyticsClickListener(null);
        analyticsLinearLayout.setOnClickListener(mockOnClickListener);

        analyticsLinearLayout.performClick();

        verify(mockOnClickListener).onClick(analyticsLinearLayout);
    }

}