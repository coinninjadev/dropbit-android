package com.coinninja.coinkeeper.view.fragment;

import android.widget.CheckBox;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.interactor.PreferenceInteractor;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class InviteHelpDialogFragmentTest {

    private static final String PHONE_NUMBER_NATIONAL = "(330) 555-1111";
    private static final String PHONE_NUMBER = "+13305551111";
    private PhoneNumber phoneNumber = new PhoneNumber(PHONE_NUMBER);
    private static final String DISPLAY_NAME = "Joe Blow";

    private InviteHelpDialogFragment dialog;

    private FragmentController<InviteHelpDialogFragment> fragmentController;

    private Contact contact = new Contact(phoneNumber, DISPLAY_NAME, false);

    private PreferenceInteractor preferenceInteractor;

    private InviteHelpDialogFragment.OnInviteHelpAcceptedCallback onInviteHelpAcceptedCallback;

    @Before
    public void setUp() {
        preferenceInteractor = mock(PreferenceInteractor.class);
        onInviteHelpAcceptedCallback = mock(InviteHelpDialogFragment.OnInviteHelpAcceptedCallback.class);
        fragmentController = Robolectric.buildFragment(InviteHelpDialogFragment.class);

        dialog = fragmentController.get();

        dialog.setContact(contact);
        dialog.setOnInviteHelpAcceptedCallback(onInviteHelpAcceptedCallback);
        dialog.setPreferenceInteractor(preferenceInteractor);

    }

    private void showDialog() {
        fragmentController.create().resume().start().visible();
    }

    @Test
    public void notifies_caller_that_user_acknowleged_invite_message_when_skipping() {
        showDialog();
        ((CheckBox) dialog.getView().findViewById(R.id.permission)).setChecked(true);

        dialog.onSkipPreferenceComplete();

        verify(onInviteHelpAcceptedCallback).onInviteHelpAccepted();
    }

    @Test
    public void notifies_caller_that_user_acknowleged_invite_message_when_not_skipping() {
        showDialog();

        dialog.getView().findViewById(R.id.done).performClick();

        verify(onInviteHelpAcceptedCallback).onInviteHelpAccepted();
    }

    @Test
    public void does_not_save_permission_when_permission_is_not_checked() {
        showDialog();

        dialog.getView().findViewById(R.id.done).performClick();

        verify(preferenceInteractor, times(0)).skipInviteHelpScreen(any(PreferencesUtil.Callback.class));
    }

    @Test
    public void saves_permission_to_skip_help() {
        showDialog();
        ((CheckBox) dialog.getView().findViewById(R.id.permission)).setChecked(true);

        dialog.getView().findViewById(R.id.done).performClick();

        verify(preferenceInteractor).skipInviteHelpScreen(any(PreferencesUtil.Callback.class));
    }

    @Test
    public void permission_not_checked_initially() {
        showDialog();

        assertFalse(((CheckBox) dialog.getView().findViewById(R.id.permission)).isChecked());
    }

    @Test
    public void shows_phone_number_when_name_is_not_available() {
        contact = new Contact(phoneNumber, "", false);
        dialog.setContact(contact);
        showDialog();

        String message = ((TextView) dialog.getView().findViewById(R.id.message)).getText().toString();

        assertThat(message, containsString(PHONE_NUMBER_NATIONAL));
    }

    @Test
    public void renders_message_with_contact_name() {
        showDialog();

        String message = ((TextView) dialog.getView().findViewById(R.id.message)).getText().toString();

        assertThat(message, containsString(DISPLAY_NAME));
    }

}