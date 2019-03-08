package com.coinninja.coinkeeper.view.notifications;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.InternalNotification;
import com.coinninja.coinkeeper.model.db.enums.MessageLevel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class InternalNotificationViewTest {

    private InternalNotificationView internalNotificationView;
    private ViewGroup baseLayout;
    private CoinKeeperApplication application;
    private ShadowApplication shadowActivity;

    @Before
    public void setUp() throws Exception {
        application = (CoinKeeperApplication) RuntimeEnvironment.application;
        Context context = RuntimeEnvironment.application.getApplicationContext();
        baseLayout = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.fragment_confirm_pay_dialog, null, false);

        internalNotificationView = new InternalNotificationView((ViewGroup) baseLayout);
        shadowActivity = shadowOf(application);
    }

    @Test
    public void show_notification_test() {
        String sampleTestMessage = "Some message";
        MessageLevel sampleMessageLevel = MessageLevel.INFO;
        InternalNotification mockInternalNotification = buildSampleNotification(sampleTestMessage, sampleMessageLevel);

        internalNotificationView.show(mockInternalNotification);
        View notificationView = baseLayout.getChildAt(1);
        TextView messageView = notificationView.findViewById(R.id.internal_message);

        assertThat(messageView.getText().toString(), equalTo("Some message"));
        assertThat(notificationView.getTag(), equalTo(R.color.info_background));
    }

    @Test
    public void on_exit_call_dismissListener_test() {
        String sampleTestMessage = "Some message";
        MessageLevel sampleMessageLevel = MessageLevel.INFO;
        InternalNotification mockInternalNotification = buildSampleNotification(sampleTestMessage, sampleMessageLevel);
        InternalNotificationView.DismissListener dismissListener = mock(InternalNotificationView.DismissListener.class);
        internalNotificationView.setDismissListener(dismissListener);
        internalNotificationView.show(mockInternalNotification);
        View notificationView = baseLayout.getChildAt(1);


        internalNotificationView.onExitBtnClicked(notificationView, mockInternalNotification);

        verify(dismissListener).onDismiss(mockInternalNotification);
    }

    @Test
    public void on_exit_remove_child_test() {
        String sampleTestMessage = "Some message";
        MessageLevel sampleMessageLevel = MessageLevel.INFO;
        InternalNotification mockInternalNotification = buildSampleNotification(sampleTestMessage, sampleMessageLevel);
        InternalNotificationView.DismissListener dismissListener = mock(InternalNotificationView.DismissListener.class);
        internalNotificationView.setDismissListener(dismissListener);
        internalNotificationView.show(mockInternalNotification);
        View notificationView = baseLayout.getChildAt(1);


        assertThat(baseLayout.getChildCount(), equalTo(2));
        internalNotificationView.onExitBtnClicked(notificationView, mockInternalNotification);

        assertThat(baseLayout.getChildCount(), equalTo(1));
    }

    @Test
    public void exit_remove_child_but_do_NOT_call_dismiss_listener_test() {
        String sampleTestMessage = "Some message";
        MessageLevel sampleMessageLevel = MessageLevel.INFO;
        InternalNotification mockInternalNotification = buildSampleNotification(sampleTestMessage, sampleMessageLevel);
        InternalNotificationView.DismissListener dismissListener = mock(InternalNotificationView.DismissListener.class);
        internalNotificationView.setDismissListener(dismissListener);
        internalNotificationView.show(mockInternalNotification);


        assertThat(baseLayout.getChildCount(), equalTo(2));
        internalNotificationView.unNaturallyDismiss();

        assertThat(baseLayout.getChildCount(), equalTo(1));
        verify(dismissListener, times(0)).onDismiss(mockInternalNotification);
    }

    @Test
    public void on_exit_view_holder_clicked_exit_the_notification_test() {
        String sampleTestMessage = "Some message";
        MessageLevel sampleMessageLevel = MessageLevel.INFO;
        InternalNotification mockInternalNotification = buildSampleNotification(sampleTestMessage, sampleMessageLevel);
        InternalNotificationView.DismissListener dismissListener = mock(InternalNotificationView.DismissListener.class);
        internalNotificationView.setDismissListener(dismissListener);
        internalNotificationView.show(mockInternalNotification);
        View exitViewHolder = baseLayout.findViewById(R.id.exit_button);

        exitViewHolder.performClick();

        verify(dismissListener).onDismiss(mockInternalNotification);
    }

    @Test
    public void on_message_body_view_holder_clicked_and_message_has_no_url_then_exit_the_notification_test() {
        String sampleTestMessage = "Some message";
        MessageLevel sampleMessageLevel = MessageLevel.INFO;
        InternalNotification mockInternalNotification = buildSampleNotification(sampleTestMessage, sampleMessageLevel);
        InternalNotificationView.DismissListener dismissListener = mock(InternalNotificationView.DismissListener.class);
        when(mockInternalNotification.getClickAction()).thenReturn(null);
        internalNotificationView.setDismissListener(dismissListener);
        internalNotificationView.show(mockInternalNotification);
        View messageBodyViewHolder = baseLayout.findViewById(R.id.internal_message);

        messageBodyViewHolder.performClick();

        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertNull(nextStartedActivity);
        verify(dismissListener).onDismiss(mockInternalNotification);
    }

    @Test
    public void on_message_body_view_holder_clicked_and_message_has_an_clickable_url_action_then_open_the_weband_exit_test() {
        String sampleTestMessage = "Some message";
        MessageLevel sampleMessageLevel = MessageLevel.INFO;
        InternalNotification mockInternalNotification = buildSampleNotification(sampleTestMessage, sampleMessageLevel);
        InternalNotificationView.DismissListener dismissListener = mock(InternalNotificationView.DismissListener.class);
        when(mockInternalNotification.getClickAction()).thenReturn(Uri.parse("https://coinninja.com"));
        internalNotificationView.setDismissListener(dismissListener);
        internalNotificationView.show(mockInternalNotification);
        View messageBodyViewHolder = baseLayout.findViewById(R.id.internal_message);

        messageBodyViewHolder.performClick();


        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getAction(), equalTo(Intent.ACTION_VIEW));
        assertThat(nextStartedActivity.getData().toString(), equalTo("https://coinninja.com"));
        verify(dismissListener).onDismiss(mockInternalNotification);
    }


    @Test
    public void INFO_message_Level_to_r_id_test() {
        MessageLevel sampleMessageLevel = MessageLevel.INFO;

        int rIDColor = internalNotificationView.getColorRid(sampleMessageLevel);

        assertThat(rIDColor, equalTo(R.color.info_background));
    }

    @Test
    public void WARN_message_Level_to_r_id_test() {
        MessageLevel sampleMessageLevel = MessageLevel.WARN;

        int rIDColor = internalNotificationView.getColorRid(sampleMessageLevel);

        assertThat(rIDColor, equalTo(R.color.warn_background));
    }

    @Test
    public void message_Level_is_NULL_show_info_colors_test() {
        MessageLevel sampleMessageLevel = null;

        int rIDColor = internalNotificationView.getColorRid(sampleMessageLevel);

        assertThat(rIDColor, equalTo(R.color.info_background));
    }

    @Test
    public void SUCCESS_message_Level_to_r_id_test() {
        MessageLevel sampleMessageLevel = MessageLevel.SUCCESS;

        int rIDColor = internalNotificationView.getColorRid(sampleMessageLevel);

        assertThat(rIDColor, equalTo(R.color.success_background));
    }

    @Test
    public void ERROR_message_Level_to_r_id_test() {
        MessageLevel sampleMessageLevel = MessageLevel.ERROR;

        int rIDColor = internalNotificationView.getColorRid(sampleMessageLevel);

        assertThat(rIDColor, equalTo(R.color.error_background));
    }

    private InternalNotification buildSampleNotification(String sampleTestMessage, MessageLevel sampleMessageLevel) {
        InternalNotification mockInternalNotification = mock(InternalNotification.class);
        when(mockInternalNotification.getMessage()).thenReturn(sampleTestMessage);
        when(mockInternalNotification.getMessageLevel()).thenReturn(sampleMessageLevel);
        return mockInternalNotification;
    }
}