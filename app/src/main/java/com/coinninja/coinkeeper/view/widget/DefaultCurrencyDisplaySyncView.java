package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.coinninja.android.helpers.Views;
import com.coinninja.coinkeeper.R;

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
        setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        styleTextView(secondaryCurrencyView, secondaryTextAppearance);
        Views.INSTANCE.rotate(rotationImageView);
    }

    public DefaultCurrencyDisplaySyncView(Context context) {
        this(context, null);
    }

    public void showSyncingUI() {
        rotationImageView.setVisibility(View.VISIBLE);
        Views.INSTANCE.rotate(rotationImageView);
    }

    public void hideSyncingUI() {
        rotationImageView.setVisibility(View.GONE);
        rotationImageView.clearAnimation();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.merge_default_currency_display_sync_view;
    }
}
