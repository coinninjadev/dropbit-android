package com.coinninja.coinkeeper.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.interactor.PinInteractor;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.view.edittext.PinEditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class AuthorizedActionActivityTest {

    public static final String VALID_PIN = "123456";
    public static final String INVALID_PIN = "000000";
    public static final String SAMPLE_AUTH_ACTION_MESSAGE = "This is some message";
    private ActivityController<AuthorizedActionActivity> controller;
    private AuthorizedActionActivity activity;
    private ShadowActivity shadowActivity;
    private PinInteractor pinInteractor;

    @Before
    public void setUp() {
        pinInteractor = mock(PinInteractor.class);

        Intent messageIntent = new Intent();
        messageIntent.putExtra(Intents.EXTRA_AUTHORIZED_ACTION_MESSAGE, SAMPLE_AUTH_ACTION_MESSAGE);
        controller = Robolectric.buildActivity(AuthorizedActionActivity.class, messageIntent);
        activity = controller.get();
        shadowActivity = shadowOf(activity);

        when(pinInteractor.hashThenVerify(anyString())).thenReturn(false);
        when(pinInteractor.hashThenVerify(VALID_PIN)).thenReturn(true);
    }

    public void start() {
        controller.create();

        // set mock interactor
        activity.pinInteractor = pinInteractor;

        controller.resume().start().visible();
    }


    @Test
    public void requests_focus_on_resume() {
        start();

        assertTrue(activity.findViewById(R.id.pin_entry_edittext).hasExplicitFocusable());
    }

    @Test
    public void three_invalid_pin_entries_flash_user_errror() {
        start();

        ((PinEditText) activity.findViewById(R.id.pin_entry_edittext)).setText(INVALID_PIN);
        ((PinEditText) activity.findViewById(R.id.pin_entry_edittext)).setText(INVALID_PIN);
        ((PinEditText) activity.findViewById(R.id.pin_entry_edittext)).setText(INVALID_PIN);

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(activity.getText(R.string.pin_mismatch_error_toast_fatal_re_enter)));
    }

    @Test
    public void three_invalid_pin_entries_finishes() {
        start();

        ((PinEditText) activity.findViewById(R.id.pin_entry_edittext)).setText(INVALID_PIN);
        ((PinEditText) activity.findViewById(R.id.pin_entry_edittext)).setText(INVALID_PIN);
        ((PinEditText) activity.findViewById(R.id.pin_entry_edittext)).setText(INVALID_PIN);

        assertThat(shadowActivity.getResultCode(), equalTo(Activity.RESULT_CANCELED));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void pin_field_clears_on_invalid_entry() {
        start();

        PinEditText pin = activity.findViewById(R.id.pin_entry_edittext);
        pin.setText(INVALID_PIN);

        assertThat(pin.getText().toString(), equalTo(""));
    }

    @Test
    public void invalid_pin_entry_presents_error() {
        start();

        ((PinEditText) activity.findViewById(R.id.pin_entry_edittext)).setText(INVALID_PIN);

        TextView errorMessage = activity.findViewById(R.id.error_message);
        assertThat(errorMessage.getVisibility(), equalTo(View.VISIBLE));
        assertThat(errorMessage.getText(), equalTo(activity.getText(R.string.pin_mismatch_error)));
    }

    @Test
    public void authorizes_with_users_valid_pin() {
        start();
        when(pinInteractor.hashThenVerify("123456")).thenReturn(true);

        ((PinEditText) activity.findViewById(R.id.pin_entry_edittext)).setText(VALID_PIN);

        assertThat(shadowActivity.getResultCode(), equalTo(AuthorizedActionActivity.RESULT_AUTHORIZED));
    }

    @Test
    public void renders_pin_entry() {
        start();
        assertNotNull(activity.findViewById(R.id.pin_entry_edittext));
    }

    @Test
    public void finishes_on_result() {
        start();

        activity.onAuthorized();

        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void display_auth_message() {
        String expectedAuthMessage = "This is some message";

        controller.create().resume().start().visible();
        TextView authMessageTextView = activity.findViewById(R.id.authenticate_message_textview);

        assertThat(authMessageTextView.getText().toString(), equalTo(expectedAuthMessage));
    }
}