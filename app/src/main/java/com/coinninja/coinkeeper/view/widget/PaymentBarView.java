package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.coinninja.coinkeeper.R;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import static com.coinninja.android.helpers.Views.withId;

public class PaymentBarView extends ConstraintLayout {

    private OnScanPressedObserver scanPressedObserver;
    private OnRequestPressedObserver requestPressedObserver;
    private OnSendPressedObserver sendPressedObserver;

    public PaymentBarView(Context context) {
        this(context, null);
    }

    public PaymentBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaymentBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.merge_component_payment_bar, this, true);
        withId(this, R.id.scan_btn).setOnClickListener(this::onScanPressed);
        withId(this, R.id.send_btn).setOnClickListener(this::onSendPressed);
        withId(this, R.id.request_btn).setOnClickListener(this::onRequestPressed);
    }

    private void onRequestPressed(View view) {
        if (requestPressedObserver != null) requestPressedObserver.onRequestPressed();
    }

    private void onSendPressed(View view) {
        if (sendPressedObserver != null) sendPressedObserver.onSendPressed();
    }

    private void onScanPressed(View view) {
        if (scanPressedObserver != null) scanPressedObserver.onScanPressed();
    }

    public void setOnRequestPressedObserver(OnRequestPressedObserver requestPressedObserver) {
        this.requestPressedObserver = requestPressedObserver;
    }

    public void setOnSendPressedObserver(OnSendPressedObserver sendPressedObserver) {
        this.sendPressedObserver = sendPressedObserver;
    }

    public void setOnScanPressedObserver(OnScanPressedObserver scanPressedObserver) {
        this.scanPressedObserver = scanPressedObserver;
    }

    public interface OnSendPressedObserver {
        void onSendPressed();
    }

    public interface OnScanPressedObserver {
        void onScanPressed();
    }

    public interface OnRequestPressedObserver {
        void onRequestPressed();
    }

}
