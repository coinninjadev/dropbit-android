package com.coinninja.coinkeeper.ui.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.service.DeverifyAccountService;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import androidx.annotation.Nullable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class RemoveCNPhoneNumberControllerTest {

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    private TestActivity activity;
    private RemovePhoneNumberController controller;
    private ShadowActivity shadowActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.setupActivity(TestActivity.class);
        shadowActivity = shadowOf(activity);
        controller = new RemovePhoneNumberController(localBroadCastUtil);
    }

    @After
    public void tearDown() {
        controller = null;
        activity = null;
        localBroadCastUtil = null;
        shadowActivity = null;
    }

    @Test
    public void observes_deverification_updates() {
        controller.onStart();

        verify(localBroadCastUtil).registerReceiver(controller.receiver, controller.intentFilter);
    }

    @Test
    public void removes_observer_when_stopped() {
        controller.onStop();

        verify(localBroadCastUtil).unregisterReceiver(controller.receiver);
    }

    @Test
    public void explains_that_pending_dropbits_will_be_canceled() {
        controller.onRemovePhoneNumber(activity.findViewById(R.id.change_remove_button));

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertThat(((TextView) dialog.findViewById(R.id.warning)).getText().toString(),
                equalTo(activity.getString(R.string.deverification_dialog_pending_dropbit_canceled_warning_message)));
        assertThat(((TextView) dialog.findViewById(R.id.message)).getText().toString(),
                equalTo(activity.getString(R.string.deverification_dialog_pending_dropbit_canceled_message)));
        assertThat(((TextView) dialog.findViewById(R.id.ok)).getText().toString(),
                equalTo(activity.getString(R.string.ok)));
    }

    @Test
    public void acknowledging_message_confirms_request() {
        controller.onRemovePhoneNumber(activity.findViewById(R.id.change_remove_button));
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();

        dialog.findViewById(R.id.ok).performClick();

        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void requests_deverification_when_positive_button_clicked() {
        controller.activity = activity;
        controller.onConfirmedRemovePhoneNumber();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(dialog);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        Intent intent = shadowActivity.getNextStartedService();
        assertThat(intent.getComponent().getClassName(), equalTo(DeverifyAccountService.class.getName()));
        assertTrue(shadowAlertDialog.hasBeenDismissed());
    }

    @Test
    public void dismisses_dialog_and_cleans_up_when_negative_button_pressed() {
        controller.activity = activity;
        controller.onConfirmedRemovePhoneNumber();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(dialog);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();

        assertNull(shadowActivity.getNextStartedService());
        assertTrue(shadowAlertDialog.hasBeenDismissed());
    }

    @Test
    public void shows_dialog_requesting_user_to_confirm_removal() {
        controller.activity = activity;

        controller.onConfirmedRemovePhoneNumber();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(dialog);
        assertThat(shadowAlertDialog.getMessage(), equalTo(activity.getString(R.string.deverification_message_are_you_sure)));
        assertThat(dialog.getButton(AlertDialog.BUTTON_POSITIVE).getText(), equalTo(activity.getString(R.string.deverification_dialog_are_you_sure_positive)));
        assertThat(dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getText(), equalTo(activity.getString(R.string.deverification_dialog_are_you_sure_negative)));
        assertFalse(shadowAlertDialog.isCancelableOnTouchOutside());
    }

    @Test
    public void dismisses_visible_dialogs_when_stopped() {
        controller.activity = activity;
        controller.onConfirmedRemovePhoneNumber();

        controller.onStop();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(dialog);
        assertTrue(shadowAlertDialog.hasBeenDismissed());
    }

    @Test
    public void cleans_up_leaky_references() {
        controller.activity = activity;

        controller.onStop();

        assertNull(controller.activity);
    }

    @Test
    public void observes_deverification_failure() {
        controller.activity = activity;

        controller.receiver.onReceive(activity, new Intent(Intents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED));

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(dialog);
        assertThat(shadowAlertDialog.getMessage(), equalTo(activity.getString(R.string.deverification_dialog_failed_message)));
        assertThat(dialog.getButton(AlertDialog.BUTTON_POSITIVE).getText(), equalTo(activity.getString(R.string.deverification_dialog_failed_positive_button)));
        assertThat(dialog.getButton(AlertDialog.BUTTON_NEGATIVE).getText(), equalTo(activity.getString(R.string.deverification_dialog_failed_negative_button)));
        assertFalse(shadowAlertDialog.isCancelableOnTouchOutside());
    }

    @Test
    public void allows_user_to_try_again_when_failed() {
        controller.activity = activity;
        controller.receiver.onReceive(activity, new Intent(Intents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED));

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(dialog);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        Intent intent = shadowActivity.getNextStartedService();
        assertThat(intent.getComponent().getClassName(), equalTo(DeverifyAccountService.class.getName()));
        assertTrue(shadowAlertDialog.hasBeenDismissed());
    }

    @Test
    public void dismisses_visible_dialog_when_deverification_completed() {
        controller.activity = activity;
        controller.receiver.onReceive(activity, new Intent(Intents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED));

        controller.receiver.onReceive(activity, new Intent(Intents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED));

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(dialog);
        assertTrue(shadowAlertDialog.hasBeenDismissed());
    }

    public static class TestActivity extends Activity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.user_account_verification);
        }
    }
}