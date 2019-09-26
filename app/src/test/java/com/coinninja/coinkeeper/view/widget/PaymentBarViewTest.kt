package com.coinninja.coinkeeper.view.widget

import android.widget.Button
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R.id
import com.coinninja.coinkeeper.R.layout
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.coinninja.coinkeeper.view.widget.PaymentBarView.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowView.clickOn

@RunWith(AndroidJUnit4::class)
class PaymentBarViewTest {
    private var activity: TestableActivity? = null
    private var paymentBarView: PaymentBarView? = null
    private var scanButton: Button? = null
    private var sendButton: Button? = null
    private var requestButton: Button? = null

    @Before
    fun setUp() {
        activity = Robolectric.setupActivity(TestableActivity::class.java)
        activity!!.appendLayout(layout.fragment_payment_bar)
        paymentBarView = activity!!.findViewById<PaymentBarView?>(id.payment_bar)
        scanButton = activity!!.findViewById<Button?>(id.scan_btn)
        sendButton = activity!!.findViewById<Button?>(id.send_btn)
        requestButton = activity!!.findViewById<Button?>(id.request_btn)
    }

    @Test
    fun sets_up_scan_button() {
        val scanPressedObserver: OnScanPressedObserver = mock(OnScanPressedObserver::class.java)
        paymentBarView!!.setOnScanPressedObserver(scanPressedObserver)
        clickOn(scanButton)
        verify(scanPressedObserver).onScanPressed()
        paymentBarView!!.setOnScanPressedObserver(null)
        clickOn(scanButton)
        verifyNoMoreInteractions(scanPressedObserver)
        verifyNoMoreInteractions(scanPressedObserver) // No Null Pointer Exception
    }

    @Test
    fun sets_up_request_button() {
        val requestPressedObserver: OnRequestPressedObserver = mock(OnRequestPressedObserver::class.java)
        paymentBarView!!.setOnRequestPressedObserver(requestPressedObserver)
        clickOn(requestButton)
        verify(requestPressedObserver).onRequestPressed()
        paymentBarView!!.setOnRequestPressedObserver(null)
        clickOn(requestButton)
        verifyNoMoreInteractions(requestPressedObserver) // No Null Pointer Exception
    }

    @Test
    fun sets_up_pay_button() {
        val sendPressedObserver: OnSendPressedObserver = mock(OnSendPressedObserver::class.java)
        paymentBarView!!.setOnSendPressedObserver(sendPressedObserver)
        clickOn(sendButton)
        verify(sendPressedObserver).onSendPressed()
        paymentBarView!!.setOnSendPressedObserver(null)
        clickOn(sendButton)
        verifyNoMoreInteractions(sendPressedObserver) // No Null Pointer Exception
    }
}