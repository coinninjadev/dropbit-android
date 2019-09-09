package com.coinninja.coinkeeper.view.button;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;

public class ConfirmHoldButton extends ProgressBar implements View.OnTouchListener {
    private static final int MAX_PROGRESS = 100;

    private int animationDurationMS;
    private ObjectAnimator progressRingAnimation;
    private Animation growAnimation;
    private OnConfirmHoldEndListener onConfirmHoldEndListener;
    private final Runnable countDownRunnable = () -> onCountDownEnd();
    private OnConfirmHoldStartedListener onConfirmHoldStartedListener;

    public ConfirmHoldButton(Context context) {
        super(context);
        init();
    }

    public ConfirmHoldButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConfirmHoldButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ConfirmHoldButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOnTouchListener(this);
        setMax(MAX_PROGRESS);

        animationDurationMS = 3000;

        progressRingAnimation = ObjectAnimator.ofInt(this, "progress", 0, MAX_PROGRESS); // see this max value coming back here, we animate towards that value
        progressRingAnimation.setDuration(animationDurationMS); // in milliseconds
        progressRingAnimation.setInterpolator(new DecelerateInterpolator());

        growAnimation = new ScaleAnimation(1.3f, 1.6f, 1.3f, 1.6f, Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5f);
        growAnimation.setFillBefore(true);
        growAnimation.setDuration(animationDurationMS);
    }

    public void startHold() {
        if (onConfirmHoldStartedListener != null)
            onConfirmHoldStartedListener.onHoldStarted();
        progressRingAnimation.start();
        startAnimation(growAnimation);
        growAnimation.start();
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        startCallBackCountDown();
    }

    private void startCallBackCountDown() {
        postDelayed(countDownRunnable, animationDurationMS);
    }

    public void cancelHold() {
        progressRingAnimation.end();
        progressRingAnimation.cancel();
        growAnimation.cancel();
        growAnimation.reset();
        clearAnimation();
        setProgress(0);
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        removeCallbacks(countDownRunnable);
    }

    private void onCountDownEnd() {
        if (onConfirmHoldEndListener != null) {
            onConfirmHoldEndListener.onHoldCompleteSuccessfully();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startHold();
                break;
            case MotionEvent.ACTION_UP:
                cancelHold();
                break;
        }
        return true;
    }

    public void setOnConfirmHoldBeginListener(OnConfirmHoldStartedListener onConfirmHoldStartedListener) {
        this.onConfirmHoldStartedListener = onConfirmHoldStartedListener;
    }

    public void setOnConfirmHoldEndListener(OnConfirmHoldEndListener onConfirmHoldEndListener) {
        this.onConfirmHoldEndListener = onConfirmHoldEndListener;
    }

    public void progressRingAnimation(ObjectAnimator progressRingAnimation) {
        this.progressRingAnimation = progressRingAnimation;
    }

    public void setGrowAnimation(Animation growAnimation) {
        this.growAnimation = growAnimation;
    }

    public Runnable getCountDownRunnable() {
        return countDownRunnable;
    }

    public interface OnConfirmHoldEndListener {
        void onHoldCompleteSuccessfully();
    }

    public interface OnConfirmHoldStartedListener {
        void onHoldStarted();
    }

}
