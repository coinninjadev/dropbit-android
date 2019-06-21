package com.coinninja.coinkeeper.view.fragment;

import android.widget.CheckBox;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.coinninja.coinkeeper.view.activity.SplashActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.coinninja.android.helpers.Views.clickOn;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class InviteHelpDialogFragmentTest {

    private static final String PHONE_NUMBER_INTERNATIONAL = "+1 330-555-1111";
    private static final String PHONE_NUMBER = "+13305551111";
    private PhoneNumber phoneNumber = new PhoneNumber(PHONE_NUMBER);
    private static final String DISPLAY_NAME = "Joe Blow";

    private InviteHelpDialogFragment dialog;

    private Identity identity = new Identity(new Contact(phoneNumber, DISPLAY_NAME, false));

    @Mock
    private UserPreferences userPreferences;

    @Mock
    private InviteHelpDialogFragment.OnInviteHelpAcceptedCallback onInviteHelpAcceptedCallback;
    private ActivityScenario<SplashActivity> scenario;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        dialog = null;
        identity = null;
        userPreferences = null;
        scenario.close();
    }


    private void setupDialog() {
        dialog = (InviteHelpDialogFragment) InviteHelpDialogFragment.newInstance(userPreferences, identity, onInviteHelpAcceptedCallback);
        scenario = ActivityScenario.launch(SplashActivity.class);
        scenario.onActivity(activity -> dialog.show(activity.getSupportFragmentManager(), InviteHelpDialogFragment.TAG));
    }

    @Test
    public void notifies_caller_that_user_acknowledged_invite_message_when_skipping() {
        setupDialog();
        ((CheckBox) dialog.getView().findViewById(R.id.permission)).setChecked(true);

        dialog.onSkipPreferenceComplete();

        verify(onInviteHelpAcceptedCallback).onInviteHelpAccepted();
    }

    @Test
    public void notifies_caller_that_user_acknowledged_invite_message_when_not_skipping() {
        setupDialog();
        clickOn(dialog.getView(), R.id.done);

        verify(onInviteHelpAcceptedCallback).onInviteHelpAccepted();
    }

    @Test
    public void does_not_save_permission_when_permission_is_not_checked() {
        setupDialog();

        clickOn(dialog.getView(), R.id.done);

        verify(userPreferences, times(0)).skipInviteHelpScreen(any(PreferencesUtil.Callback.class));
    }

    @Test
    public void saves_permission_to_skip_help() {
        setupDialog();
        ((CheckBox) dialog.getView().findViewById(R.id.permission)).setChecked(true);

        clickOn(dialog.getView(), R.id.done);

        verify(userPreferences).skipInviteHelpScreen(any(PreferencesUtil.Callback.class));
    }

    @Test
    public void permission_not_checked_initially() {
        setupDialog();

        assertFalse(((CheckBox) dialog.getView().findViewById(R.id.permission)).isChecked());
    }

    @Test
    public void shows_phone_number_when_name_is_not_available() {
        identity = new Identity(new Contact(phoneNumber, PHONE_NUMBER_INTERNATIONAL, false));
        setupDialog();

        String message = ((TextView) dialog.getView().findViewById(R.id.message)).getText().toString();

        assertThat(message, containsString(PHONE_NUMBER_INTERNATIONAL));
    }

    @Test
    public void renders_message_with_contact_name() {
        setupDialog();

        String message = ((TextView) dialog.getView().findViewById(R.id.message)).getText().toString();

        assertThat(message, containsString(DISPLAY_NAME));
    }

}