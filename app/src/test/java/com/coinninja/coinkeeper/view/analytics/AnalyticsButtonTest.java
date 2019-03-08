package com.coinninja.coinkeeper.view.analytics;

import android.view.View;

import com.coinninja.coinkeeper.CoinKeeperApplication;
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
public class AnalyticsButtonTest {


    private AnalyticsButton analyticsButton;

    @Before
    public void setUp() throws Exception {
        CoinKeeperApplication application = (CoinKeeperApplication) RuntimeEnvironment.application;
        analyticsButton = new AnalyticsButton(application);
    }

    // TODO verify that analytics actually tracks button click

    @Test
    public void insure_analytics_click_event_and_original_click_event_is_engaged_test() {
        AnalyticsClickListener mockAnalyticsClickListener = mock(AnalyticsClickListener.class);
        View.OnClickListener mockOnClickListener = mock(View.OnClickListener.class);
        analyticsButton.setAnalyticsClickListener(mockAnalyticsClickListener);
        analyticsButton.setOnClickListener(mockOnClickListener);

        analyticsButton.performClick();

        verify(mockAnalyticsClickListener).onClick(analyticsButton);
        verify(mockOnClickListener).onClick(analyticsButton);
    }


    @Test
    public void insure_analytics_click_event_is_engaged_even_if_original_click_is_null_test() {
        AnalyticsClickListener mockAnalyticsClickListener = mock(AnalyticsClickListener.class);
        analyticsButton.setAnalyticsClickListener(mockAnalyticsClickListener);
        analyticsButton.setOnClickListener(null);

        analyticsButton.performClick();

        verify(mockAnalyticsClickListener).onClick(analyticsButton);
    }


    @Test
    public void insure_original_click_event_is_engaged_even_if_analytics_click_is_null_test() {
        View.OnClickListener mockOnClickListener = mock(View.OnClickListener.class);
        analyticsButton.setAnalyticsClickListener(null);
        analyticsButton.setOnClickListener(mockOnClickListener);

        analyticsButton.performClick();

        verify(mockOnClickListener).onClick(analyticsButton);
    }
}