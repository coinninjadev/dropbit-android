package com.coinninja.coinkeeper.view.progress;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.coinninja.coinkeeper.R;

import androidx.annotation.Nullable;

public class SendingProgressView extends RelativeLayout {
    private ProgressBar progressBar;
    private ImageView checkBox;

    public SendingProgressView(Context context) {
        super(context);
        initView();
    }

    public SendingProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SendingProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SendingProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.progress_bar_sending_layout, this);
        progressBar = findViewById(R.id.sending_progress_bar);
        checkBox = findViewById(R.id.sending_check_box);

        resetView();
    }

    public void resetView() {
        Drawable progressDrawable = getResources().getDrawable(R.drawable.sending_progress_success);
        progressBar.setProgressDrawable(progressDrawable);
        progressBar.setProgress(0);

        checkBox.setImageResource(R.drawable.ic_sending_check_idle);
        setTag(R.drawable.ic_sending_check_idle);
    }

    public void completeSuccess() {
        Drawable progressDrawable = getResources().getDrawable(R.drawable.sending_progress_success);
        progressBar.setProgressDrawable(progressDrawable);
        checkBox.setImageResource(R.drawable.ic_sending_check_success);
        setTag(R.drawable.ic_sending_check_success);
    }

    public void completeFail() {
        Drawable progressDrawable = getResources().getDrawable(R.drawable.sending_progress_fail);
        progressBar.setProgressDrawable(progressDrawable);
        checkBox.setImageResource(R.drawable.ic_sending_x_fail);
        setTag(R.drawable.ic_sending_x_fail);
    }

    public void setProgress(int progress) {
        if (progress < 2) {
            resetView();
        }
        progressBar.setProgress(progress);
    }

    public int getProgress() {
        return progressBar.getProgress();
    }
}
