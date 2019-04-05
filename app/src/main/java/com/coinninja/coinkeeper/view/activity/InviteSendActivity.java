package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.presenter.activity.InviteContactPresenter;
import com.coinninja.coinkeeper.service.SaveInviteService;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.ui.dropbit.ManualInvitePresenter;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;
import com.coinninja.coinkeeper.view.progress.SendingProgressView;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class InviteSendActivity extends SecuredActivity implements InviteContactPresenter.View {
    public static final String RATE_LIMIT_DROPBIT_FRAGMENT_TAG = "EXPIRED_CODE_FRAGMENT_TAG";

    @Inject
    ActivityNavigationUtil activityNavigationUtil;
    @Inject
    InviteContactPresenter invitePresenter;
    @Inject
    ManualInvitePresenter manualInvitePresenter;

    SendingProgressView sendingProgressView;
    PendingInviteDTO pendingInviteDTO;

    public void showInitTransaction() {
        TextView sendingProgressLabel = findViewById(R.id.broadcast_sending_progress_label);
        TextView transactionIdLabel = findViewById(R.id.transaction_id_label);
        TextView transactionIdLink = findViewById(R.id.transaction_id_link);
        ImageView transactionIdIcon = findViewById(R.id.transaction_id_link_image);
        Button transactionActionBtn = findViewById(R.id.transaction_complete_action_button);


        sendingProgressView.setProgress(0);
        sendingProgressView.resetView();

        transactionIdLabel.setVisibility(View.INVISIBLE);
        transactionIdLink.setVisibility(View.INVISIBLE);
        transactionIdIcon.setVisibility(View.INVISIBLE);
        transactionActionBtn.setVisibility(View.INVISIBLE);

        sendingProgressLabel.setText(getResources().getText(R.string.broadcast_sent_label));
        transactionActionBtn.setText("");
        transactionIdLink.setText("");
        transactionActionBtn.setOnClickListener(null);
        transactionIdLink.setOnClickListener(null);
        transactionIdIcon.setOnClickListener(null);
        transactionActionBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }

    public void showDefaultInviteFail(String message) {
        TextView sendingProgressLabel = findViewById(R.id.broadcast_sending_progress_label);
        TextView transactionIdLabel = findViewById(R.id.transaction_id_label);
        TextView transactionIdLink = findViewById(R.id.transaction_id_link);
        ImageView transactionIdIcon = findViewById(R.id.transaction_id_link_image);
        Button transactionActionBtn = findViewById(R.id.transaction_complete_action_button);


        sendingProgressView.setProgress(100);
        sendingProgressView.completeFail();

        transactionIdLabel.setVisibility(View.VISIBLE);
        transactionIdLink.setVisibility(View.INVISIBLE);
        transactionIdIcon.setVisibility(View.INVISIBLE);
        transactionActionBtn.setVisibility(View.INVISIBLE);

        sendingProgressLabel.setText(getResources().getText(R.string.broadcast_sent_failed));
        transactionIdLink.setText("");
        transactionIdLabel.setText(getResources().getText(R.string.invite_failed));

        transactionIdLink.setOnClickListener(null);
        transactionIdIcon.setOnClickListener(null);
    }

    @Override
    public void showInviteFail(String dropBitActionError, String message) {
        showDefaultInviteFail(message);

        if (Intents.ACTION_DROPBIT__ERROR_RATE_LIMIT.equals(dropBitActionError)) {
            onRateLimitError();
        } else if (Intents.ACTION_DROPBIT__ERROR_UNKNOWN.equals(dropBitActionError)) {
            onUnknownError();
        }
    }

    @Override
    public void showInviteSuccessful(DropBitInvitation inviteContact) {
        saveInvite(inviteContact);

        TextView sendingProgressLabel = findViewById(R.id.broadcast_sending_progress_label);
        TextView transactionIdLabel = findViewById(R.id.transaction_id_label);
        Button transactionActionBtn = findViewById(R.id.transaction_complete_action_button);


        sendingProgressView.setProgress(100);
        sendingProgressView.completeSuccess();

        transactionIdLabel.setVisibility(View.VISIBLE);
        transactionActionBtn.setVisibility(View.VISIBLE);

        sendingProgressLabel.setText(getResources().getText(R.string.broadcast_sent_label));
        transactionActionBtn.setText(getResources().getText(R.string.broadcast_sent_ok));
        transactionIdLabel.setText(getResources().getText(R.string.invite_sent_successfully));
        transactionActionBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        transactionActionBtn.setOnClickListener(view -> activityNavigationUtil.navigateToHome(view.getContext()));
    }

    @Override
    public void showInviteSuccessfulDegradedSms(DropBitInvitation inviteContact) {
        saveInvite(inviteContact);

        TextView sendingProgressLabel = findViewById(R.id.broadcast_sending_progress_label);
        TextView transactionIdLabel = findViewById(R.id.transaction_id_label);
        Button transactionActionBtn = findViewById(R.id.transaction_complete_action_button);


        sendingProgressView.setProgress(100);
        sendingProgressView.completeSuccess();

        transactionIdLabel.setVisibility(View.VISIBLE);
        transactionActionBtn.setVisibility(View.VISIBLE);

        sendingProgressLabel.setText(getResources().getText(R.string.broadcast_sent_label));
        transactionActionBtn.setText(getResources().getText(R.string.broadcast_sent_ok));
        transactionIdLabel.setText(getResources().getText(R.string.invite_sent_successfully));
        transactionActionBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        transactionActionBtn.setOnClickListener(view -> activityNavigationUtil.navigateToHome(view.getContext()));
        manualInvitePresenter.presentManualSend(this, inviteContact);
    }

    @Override
    public void showProgress(int progress) {
        sendingProgressView.setProgress(progress);
    }

    public void onRetryClicked() {
        showInitTransaction();
        startInviteContact();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        sendingProgressView = findViewById(R.id.broadcast_sending_progress);
        pendingInviteDTO = getIntent().getParcelableExtra(Intents.EXTRA_INVITE_DTO);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showInitTransaction();
        startInviteContact();
    }

    private void startInviteContact() {
        invitePresenter.attachView(this);
        invitePresenter.requestInvite(pendingInviteDTO.getContact(), pendingInviteDTO.getInviteAmount(), pendingInviteDTO.getBitcoinPrice());
    }

    private void onRateLimitError() {
        showErrorRetryButton();


        GenericAlertDialog alertDialog = GenericAlertDialog.newInstance(
                null,
                getString(R.string.invite_sent_error_rate_limit),
                "ok",
                null,
                null,
                false,
                false
        );

        alertDialog.show(getFragmentManager(), RATE_LIMIT_DROPBIT_FRAGMENT_TAG);
    }

    private void showErrorRetryButton() {
        Button transactionActionBtn = findViewById(R.id.transaction_complete_action_button);

        transactionActionBtn.setVisibility(View.VISIBLE);
        transactionActionBtn.setBackgroundColor(getResources().getColor(R.color.color_error));
        transactionActionBtn.setText(getResources().getText(R.string.broadcast_sent_try_again));

        transactionActionBtn.setOnClickListener(view -> onRetryClicked());
    }

    private void onUnknownError() {
        showErrorRetryButton();
    }

    private void saveInvite(DropBitInvitation invitedContact) {
        Intent intent = new Intent(this, SaveInviteService.class);
        intent.putExtra(Intents.EXTRA_COMPLETED_INVITE_DTO, new CompletedInviteDTO(pendingInviteDTO, invitedContact));
        startService(intent);
    }
}
