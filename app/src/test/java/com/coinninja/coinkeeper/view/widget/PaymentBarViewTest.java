package com.coinninja.coinkeeper.view.widget;

import android.widget.Button;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.view.widget.PaymentBarView.OnRequestPressedObserver;
import com.coinninja.coinkeeper.view.widget.PaymentBarView.OnScanPressedObserver;
import com.coinninja.coinkeeper.view.widget.PaymentBarView.OnSendPressedObserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.shadows.ShadowView.clickOn;

@RunWith(RobolectricTestRunner.class)
public class PaymentBarViewTest {

    private TestableActivity activity;
    private PaymentBarView paymentBarView;
    private Button scanButton;
    private Button sendButton;
    private Button requestButton;

    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.fragment_payment_bar);
        paymentBarView = activity.findViewById(R.id.payment_bar);
        scanButton = activity.findViewById(R.id.scan_btn);
        sendButton = activity.findViewById(R.id.send_btn);
        requestButton = activity.findViewById(R.id.request_btn);
    }

    @Test
    public void sets_up_scan_button() {
        OnScanPressedObserver scanPressedObserver = mock(OnScanPressedObserver.class);

        paymentBarView.setOnScanPressedObserver(scanPressedObserver);
        clickOn(scanButton);
        verify(scanPressedObserver).onScanPressed();

        paymentBarView.setOnScanPressedObserver(null);
        clickOn(scanButton);
        verifyNoMoreInteractions(scanPressedObserver);
        verifyNoMoreInteractions(scanPressedObserver); // No Null Pointer Exception
    }

    @Test
    public void sets_up_request_button() {
        OnRequestPressedObserver requestPressedObserver = mock(OnRequestPressedObserver.class);

        paymentBarView.setOnRequestPressedObserver(requestPressedObserver);
        clickOn(requestButton);
        verify(requestPressedObserver).onRequestPressed();

        paymentBarView.setOnRequestPressedObserver(null);
        clickOn(requestButton);
        verifyNoMoreInteractions(requestPressedObserver); // No Null Pointer Exception
    }

    @Test
    public void sets_up_pay_button() {
        OnSendPressedObserver sendPressedObserver = mock(OnSendPressedObserver.class);

        paymentBarView.setOnSendPressedObserver(sendPressedObserver);
        clickOn(sendButton);
        verify(sendPressedObserver).onSendPressed();

        paymentBarView.setOnSendPressedObserver(null);
        clickOn(sendButton);
        verifyNoMoreInteractions(sendPressedObserver); // No Null Pointer Exception
    }

}