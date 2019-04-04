package com.coinninja.coinkeeper.ui.dropbit;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import javax.inject.Inject;

import static com.coinninja.android.helpers.Views.withId;

public class ManualInvitePresenter {
    private final ActivityNavigationUtil activityNavigationUtil;

    @Inject
    ManualInvitePresenter(ActivityNavigationUtil activityNavigationUtil) {
        this.activityNavigationUtil = activityNavigationUtil;
    }

    public void presentManualSend(Activity activity, DropBitInvitation invitedContact) {
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_manual_invite_dialog, withId(activity, android.R.id.content), false);
        GenericAlertDialog genericAlertDialog = GenericAlertDialog.newInstance(view, false, false);
        genericAlertDialog.asWide();
        genericAlertDialog.show(activity.getFragmentManager(), ManualInvitePresenter.class.getSimpleName());
        TextView message = withId(view, R.id.message);
        PhoneNumber phoneNumber = invitedContact.getMetadata().getReceiver().getPhoneNumber();
        String formattedNumber = phoneNumber.toInternationalDisplayText();
        message.setText(activity.getString(R.string.manual_send_dialog_message, formattedNumber));
        withId(view, R.id.ok).setOnClickListener(v -> activityNavigationUtil.shareDropbitManually(activity, invitedContact));
    }
}
