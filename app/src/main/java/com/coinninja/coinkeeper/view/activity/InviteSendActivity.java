package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.presenter.activity.InviteContactPresenter;
import com.coinninja.coinkeeper.service.SaveInviteService;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;
import com.coinninja.coinkeeper.view.progress.SendingProgressView;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class InviteSendActivity extends SecuredActivity implements InviteContactPresenter.View {
    public static final String RATE_LIMIT_DROPBIT_FRAGMENT_TAG = "EXPIRED_CODE_FRAGMENT_TAG";
    static final String RESTORE_STATE = "RESTORE_STATE";

    @Inject
    ActivityNavigationUtil activityNavigationUtil;

    @Inject
    InviteContactPresenter invitePresenter;

    SendingProgressView sendingProgressView;
    PendingInviteDTO pendingInviteDTO;
    SendState sendState = SendState.INIT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        sendingProgressView = findViewById(R.id.broadcast_sending_progress);
        pendingInviteDTO = getIntent().getParcelableExtra(Intents.EXTRA_INVITE_DTO);
        onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (sendState) {
            case INIT:
                showInitTransaction();
                startInviteContact();
                break;
            case COMPLETED_SUCCESS:
                showSuccessUI();
                break;
            case COMPLETED_FAILED:
                showFailureInviteUI();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(RESTORE_STATE, sendState.value);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) { return; }
        super.onRestoreInstanceState(savedInstanceState);
        sendState = (SendState) savedInstanceState.get(RESTORE_STATE);
    }

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

    private void startInviteContact() {
        invitePresenter.attachView(this);
        invitePresenter.requestInvite(pendingInviteDTO);
    }

    public void showFailureInviteUI() {
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
        sendState = SendState.COMPLETED_FAILED;
        showFailureInviteUI();
        reportFail();

        if (Intents.ACTION_DROPBIT__ERROR_RATE_LIMIT.equals(dropBitActionError)) {
            onRateLimitError();
        } else if (Intents.ACTION_DROPBIT__ERROR_UNKNOWN.equals(dropBitActionError)) {
            onUnknownError();
        }
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

    @Override
    public void showInviteSuccessful(InvitedContact inviteContact) {
        sendState = SendState.COMPLETED_SUCCESS;
        saveInvite(inviteContact);
        showSuccessUI();
        reportSuccessful();
    }

    private void showSuccessUI() {
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

    private void reportSuccessful() {
        analytics.trackEvent(Analytics.EVENT_DROPBIT_INITIATED);
    }

    private void reportFail() {
        analytics.trackEvent(Analytics.EVENT_DROPBIT_INITIATION_FAILED);
    }

    private void saveInvite(InvitedContact invitedContact) {
        Intent intent = new Intent(this, SaveInviteService.class);
        intent.putExtra(Intents.EXTRA_COMPLETED_INVITE_DTO, new CompletedInviteDTO(pendingInviteDTO, invitedContact));
        startService(intent);
    }

    public void onRetryClicked() {
        showInitTransaction();
        startInviteContact();
    }

    @Override
    public void showProgress(int progress) {
        sendingProgressView.setProgress(progress);
    }

    enum SendState {
        INIT(0), STARTED(1), COMPLETED_FAILED(2), COMPLETED_SUCCESS(3);

        private final int value;

        SendState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SendState valueOf(int value) {
            for (SendState sendState : SendState.values()) {
                if (sendState.getValue() == value) return sendState;
            }

            return SendState.INIT;
        }
    }
}
