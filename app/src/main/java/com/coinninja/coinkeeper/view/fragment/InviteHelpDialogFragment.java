package com.coinninja.coinkeeper.view.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.interactor.PreferenceInteractor;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;

import androidx.annotation.Nullable;

public class InviteHelpDialogFragment extends BaseDialogFragment {

    public static final String TAG = InviteHelpDialogFragment.class.getSimpleName();

    private PreferenceInteractor preferenceInteractor;
    private OnInviteHelpAcceptedCallback onInviteHelpAcceptedCallback;
    private Contact contact;
    private CharSequence contactForMessage;
    private final PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();

    public static DialogFragment newInstance(PreferenceInteractor preferenceInteractor,
                                             Contact contact,
                                             OnInviteHelpAcceptedCallback onInviteHelpAcceptedCallback) {
        InviteHelpDialogFragment fragment = new InviteHelpDialogFragment();
        fragment.setPreferenceInteractor(preferenceInteractor);
        fragment.setContact(contact);
        fragment.setOnInviteHelpAcceptedCallback(onInviteHelpAcceptedCallback);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_invite_help, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setMessage();
        setupCheckbox();
        setupDoneButton();
    }

    private void setupDoneButton() {
        getView().findViewById(R.id.done).setOnClickListener(v -> onDoneClick());
    }

    private void onDoneClick() {
        if (shouldSkipInviteHelp()) {
            preferenceInteractor.skipInviteHelpScreen(() -> onSkipPreferenceComplete());
        }

        acknowlegeMessage();
    }

    private void acknowlegeMessage() {
        onInviteHelpAcceptedCallback.onInviteHelpAccepted();
    }

    void onSkipPreferenceComplete() {
        acknowlegeMessage();
    }

    private void setupCheckbox() {
        ((CheckBox) getView().findViewById(R.id.permission)).setChecked(false);
    }

    private void setMessage() {
        ((TextView) getView().findViewById(R.id.message)).setText(getMessage());
    }

    private boolean shouldSkipInviteHelp() {
        return ((CheckBox) getView().findViewById(R.id.permission)).isChecked();
    }

    public void setPreferenceInteractor(PreferenceInteractor preferenceInteractor) {
        this.preferenceInteractor = preferenceInteractor;
    }

    public void setOnInviteHelpAcceptedCallback(OnInviteHelpAcceptedCallback onInviteHelpAcceptedCallback) {
        this.onInviteHelpAcceptedCallback = onInviteHelpAcceptedCallback;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    private String getMessage() {
        String mask = getView().getResources().getString(R.string.invite_help_message_mask);
        String message = getView().getResources().getString(R.string.invite_help_message);
        return message.replace(mask, getContactForMessage());
    }

    private String getContactForMessage() {
        if (contact.getDisplayName() != null && !contact.getDisplayName().isEmpty()) {
            return contact.getDisplayName();
        } else {
            return contact.getPhoneNumber().toNationalDisplayText();
        }
    }

    public interface OnInviteHelpAcceptedCallback {
        void onInviteHelpAccepted();
    }
}
