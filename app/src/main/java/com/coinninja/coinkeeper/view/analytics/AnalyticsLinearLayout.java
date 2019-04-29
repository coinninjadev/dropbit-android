package com.coinninja.coinkeeper.view.analytics;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.view.analytics.listeners.AnalyticsClickListener;

import androidx.annotation.Nullable;

public class AnalyticsLinearLayout extends LinearLayout implements View.OnClickListener {

    private OnClickListener originalClickListener;
    private AnalyticsClickListener analyticsClickListener;

    public AnalyticsLinearLayout(Context context) {
        super(context);
        init();
    }

    public AnalyticsLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnalyticsLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AnalyticsLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        super.setOnClickListener(this);
        setAnalyticsClickListener(new AnalyticsClickListener(CoinKeeperApplication.appComponent.getAnalytics()));
    }

    @Override
    public void onClick(View view) {
        trackAnalytics(view);
        preformOriginalClick(view);
    }

    protected void trackAnalytics(View view) {
        if (analyticsClickListener != null) analyticsClickListener.onClick(view);
    }

    private void preformOriginalClick(View view) {
        if (originalClickListener != null) originalClickListener.onClick(view);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener originalClickListener) {
        this.originalClickListener = originalClickListener;
    }

    public void setAnalyticsClickListener(@Nullable AnalyticsClickListener analyticsClickListener) {
        this.analyticsClickListener = analyticsClickListener;
    }
}
