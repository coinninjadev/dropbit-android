package com.coinninja.coinkeeper.view.button

import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.animation.Animation
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric


@RunWith(AndroidJUnit4::class)
class ConfirmHoldButtonTest {

    private lateinit var confirmHoldButton: ConfirmHoldButton
    private val motionEventDown: MotionEvent = mock()
    private val motionEventUp: MotionEvent = mock()
    private val progressRingAnimation: ObjectAnimator = mock()
    private val growAnimation: Animation = mock()
    private val onConfirmHoldEndListener: ConfirmHoldButton.OnConfirmHoldEndListener = mock()

    @Before
    fun setUp() {
        val activity = Robolectric.setupActivity(TestableActivity::class.java)
        activity.appendLayout(R.layout.activity_confirm_payment)
        confirmHoldButton = activity.findViewById(R.id.confirm_pay_hold_progress_btn)

        whenever(motionEventDown.action).thenReturn(MotionEvent.ACTION_DOWN)
        whenever(motionEventUp.action).thenReturn(MotionEvent.ACTION_UP)

        confirmHoldButton.progressRingAnimation(progressRingAnimation)
        confirmHoldButton.setGrowAnimation(growAnimation)
        confirmHoldButton.setOnConfirmHoldEndListener(onConfirmHoldEndListener)
    }

    @Test
    fun touch_and_hold() {

        confirmHoldButton.onTouch(confirmHoldButton, motionEventDown)
        confirmHoldButton.countDownRunnable.run()

        verify(progressRingAnimation).start()
        verify(growAnimation).start()
        verify(onConfirmHoldEndListener).onHoldCompleteSuccessfully()
    }

    @Test
    fun touch_lift_finger() {

        confirmHoldButton.onTouch(confirmHoldButton, motionEventUp)

        verify(progressRingAnimation).cancel()
        verify(growAnimation).cancel()
    }
}