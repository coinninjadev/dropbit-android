package com.coinninja.coinkeeper.presenter.activity;

import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.service.runner.InviteContactRunner;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import javax.inject.Inject;

public class InviteContactPresenter implements InviteContactRunner.OnInviteListener {

    private final Analytics analytics;
    private InviteContactRunner inviteRunner;
    private View view;

    @Inject
    public InviteContactPresenter(InviteContactRunner inviteRunner, Analytics analytics) {
        this.inviteRunner = inviteRunner;
        this.analytics = analytics;
    }

    public void attachView(View view) {
        this.view = view;
    }

    public void requestInvite(PendingInviteDTO pendingInviteDTO) {
        inviteRunner.setOnInviteListener(this);
        inviteRunner.setPendingInviteDTO(pendingInviteDTO);
        inviteRunner = inviteRunner.clone();
        inviteRunner.execute(pendingInviteDTO.getContact());
    }

    @Override
    public void onInviteSuccessful(InvitedContact inviteContact) {
        view.showInviteSuccessful(inviteContact);
        analytics.setUserProperty(Analytics.PROPERTY_HAS_SENT_DROPBIT, true);
    }

    @Override
    public void onInviteProgress(int progress) {
        view.showProgress(progress);
    }

    @Override
    public void onInviteError(String dropBitActionError, String errorMessage) {
        view.showInviteFail(dropBitActionError, errorMessage);
    }

    public interface View {
        void showInviteFail(String dropBitActionError, String errorMessage);

        void showInviteSuccessful(InvitedContact inviteContact);

        void showProgress(int progress);
    }
}
