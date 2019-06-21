package com.coinninja.coinkeeper.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.greenrobot.greendao.annotation.Id;

public class InviteHelpDialogFragment extends BaseDialogFragment {

    public static final String TAG = InviteHelpDialogFragment.class.getSimpleName();

    private UserPreferences preferenceInteractor;
    private OnInviteHelpAcceptedCallback onInviteHelpAcceptedCallback;
    private Identity identity;

    public static DialogFragment newInstance(UserPreferences preferenceInteractor,
                                             Identity identity,
                                             OnInviteHelpAcceptedCallback onInviteHelpAcceptedCallback) {
        InviteHelpDialogFragment fragment = new InviteHelpDialogFragment();
        fragment.setPreferenceInteractor(preferenceInteractor);
        fragment.setIdentity(identity);
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
        getView().findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneClick();
            }
        });
    }

    private void onDoneClick() {
        if (shouldSkipInviteHelp()) {
            preferenceInteractor.skipInviteHelpScreen(new PreferencesUtil.Callback() {
                @Override
                public void onComplete() {
                    onSkipPreferenceComplete();
                }
            });
        }

        acknowledgeMessage();
    }

    private void acknowledgeMessage() {
        onInviteHelpAcceptedCallback.onInviteHelpAccepted();
    }

    void onSkipPreferenceComplete() {
        acknowledgeMessage();
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

    public void setPreferenceInteractor(UserPreferences preferenceInteractor) {
        this.preferenceInteractor = preferenceInteractor;
    }

    public void setOnInviteHelpAcceptedCallback(OnInviteHelpAcceptedCallback onInviteHelpAcceptedCallback) {
        this.onInviteHelpAcceptedCallback = onInviteHelpAcceptedCallback;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    private String getMessage() {
        String mask = getView().getResources().getString(R.string.invite_help_message_mask);
        String message = getView().getResources().getString(R.string.invite_help_message);
        return message.replace(mask, identity.getDisplayName());
    }

    public interface OnInviteHelpAcceptedCallback {
        void onInviteHelpAccepted();
    }
}
