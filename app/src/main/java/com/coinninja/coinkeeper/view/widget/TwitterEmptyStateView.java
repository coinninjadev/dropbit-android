package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.coinninja.coinkeeper.R;

public class TwitterEmptyStateView extends ConstraintLayout {

    public TwitterEmptyStateView(Context context) {
        this(context, null);
    }

    public TwitterEmptyStateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwitterEmptyStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.merge_twitter_empty_state, this, true);
    }
}
