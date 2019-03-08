package com.coinninja.coinkeeper.view.button;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class ConfirmHoldButtonTest {

    private ConfirmHoldButton confirmHoldButton;
    private MotionEvent motionEventDown;
    private MotionEvent motionEventUp;
    private ObjectAnimator progressRingAnimation;
    private Animation growAnimation;
    private ConfirmHoldButton.OnConfirmHoldEndListener onConfirmHoldEndListener;

    @Before
    public void setUp() throws Exception {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        View layout = LayoutInflater.from(context).inflate(R.layout.fragment_confirm_pay_dialog, null, false);

        confirmHoldButton = layout.findViewById(R.id.confirm_pay_hold_progress_btn);
        motionEventDown = mock(MotionEvent.class);
        motionEventUp = mock(MotionEvent.class);

        when(motionEventDown.getAction()).thenReturn(MotionEvent.ACTION_DOWN);
        when(motionEventUp.getAction()).thenReturn(MotionEvent.ACTION_UP);

        onConfirmHoldEndListener = mock(ConfirmHoldButton.OnConfirmHoldEndListener.class);
        progressRingAnimation = mock(ObjectAnimator.class);
        growAnimation = mock(Animation.class);

        confirmHoldButton.progressRingAnimation(progressRingAnimation);
        confirmHoldButton.setGrowAnimation(growAnimation);
        confirmHoldButton.setOnConfirmHoldEndListener(onConfirmHoldEndListener);
    }

    @Test
    public void touch_and_hold() {

        confirmHoldButton.onTouch(confirmHoldButton, motionEventDown);
        confirmHoldButton.getCountDownRunnable().run();

        verify(progressRingAnimation).start();
        verify(growAnimation).start();
        verify(onConfirmHoldEndListener).onHoldCompleteSuccessfully();
    }

    @Test
    public void touch_lift_finger() {

        confirmHoldButton.onTouch(confirmHoldButton, motionEventUp);

        verify(progressRingAnimation).cancel();
        verify(growAnimation).cancel();
    }
}