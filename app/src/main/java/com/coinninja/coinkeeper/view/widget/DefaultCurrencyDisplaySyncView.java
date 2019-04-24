package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.coinninja.android.helpers.Views;
import com.coinninja.coinkeeper.R;

import androidx.annotation.Nullable;

public class DefaultCurrencyDisplaySyncView extends DefaultCurrencyDisplayView {

    protected ImageView rotationImageView;

    public DefaultCurrencyDisplaySyncView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultCurrencyDisplaySyncView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DefaultCurrencyDisplaySyncView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        rotationImageView = findViewById(R.id.syncing_image);
        rotationImageView.setVisibility(View.GONE);
        findViewById(R.id.syncing_text).setVisibility(View.GONE);
        styleTextView(secondaryCurrencyView, secondaryTextAppearance);
        styleTextView(findViewById(R.id.syncing_text), secondaryTextAppearance);
        Views.rotate(rotationImageView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.merge_default_currency_display_sync_view;
    }

    public DefaultCurrencyDisplaySyncView(Context context) {
        this(context, null);
    }

    public void showSyncingUI() {
        rotationImageView.setVisibility(View.VISIBLE);
        Views.rotate(rotationImageView);
        findViewById(R.id.syncing_text).setVisibility(View.VISIBLE);
    }

    public void hideSyncingUI() {
        rotationImageView.setVisibility(View.GONE);
        findViewById(R.id.syncing_text).setVisibility(View.GONE);
        rotationImageView.clearAnimation();
    }
}
