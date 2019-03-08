package com.coinninja.coinkeeper.view.analytics;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnalyticsLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        super.setOnClickListener(this);
        CoinKeeperApplication application = (CoinKeeperApplication) getContext().getApplicationContext();
        setAnalyticsClickListener(new AnalyticsClickListener(application.getAppComponent().getAnalytics()));
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
