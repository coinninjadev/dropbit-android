package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.coinninja.coinkeeper.R;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MerchantLogoView extends ConstraintLayout {

    public MerchantLogoView(Context context) {
        this(context, null);
    }

    public MerchantLogoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MerchantLogoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.merge_merchant_logo_display, this, true);
    }

}
