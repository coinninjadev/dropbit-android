package com.coinninja.coinkeeper.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.model.db.enums.IdentityType;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.presenter.activity.BroadcastTransactionPresenter;
import com.coinninja.coinkeeper.service.BroadcastTransactionService;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.ui.twitter.ShareTransactionDialog;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.uri.CoinNinjaUriBuilder;
import com.coinninja.coinkeeper.util.uri.UriUtil;
import com.coinninja.coinkeeper.view.progress.SendingProgressView;

import javax.inject.Inject;

import static com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.TRANSACTION;

public class BroadcastActivity extends BaseActivity implements BroadcastTransactionPresenter.View {
    static final String RESTORE_STATE = "RESTORE_STATE";
    static final String TRANSACTION_ID = "TRANSACTION_ID";
    private static final String TAG = BroadcastActivity.class.getSimpleName();
    @Inject
    CoinNinjaUriBuilder coinNinjaUriBuilder;
    SendState sendState = SendState.INIT;
    @Inject
    ActivityNavigationUtil activityNavigationUtil;
    @Inject
    ShareTransactionDialog shareTransactionDialog;
    @Inject
    BroadcastTransactionPresenter broadcastPresenter;
    @Inject
    UserPreferences userPreferences;
    @Inject
    SyncWalletManager syncWalletManager;
    SendingProgressView sendingProgressView;
    private BroadcastTransactionDTO broadcastDTO;
    private TextView sendingProgressLabel;
    private TextView transactionIdLabel;
    private TextView transactionIdLink;
    private ImageView transactionIdIcon;
    private Button transactionActionBtn;
    private String transactionId;

    public void showInitTransaction() {
        sendingProgressView.setProgress(0);
        sendingProgressView.resetView();
        transactionIdLabel.setVisibility(View.INVISIBLE);
        transactionIdLink.setVisibility(View.INVISIBLE);
        transactionIdIcon.setVisibility(View.INVISIBLE);
        transactionActionBtn.setVisibility(View.INVISIBLE);
        sendingProgressLabel.setText("");
        transactionActionBtn.setText("");
        transactionIdLink.setText("");
        transactionActionBtn.setOnClickListener(null);
        transactionIdLink.setOnClickListener(null);
        transactionIdIcon.setOnClickListener(null);
        transactionActionBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }

    public void onRetryClicked() {
        showInitTransaction();
        startBroadcast(broadcastDTO.getTransactionData());
        analytics.trackEvent(Analytics.Companion.EVENT_TRANSACTION_RETRY);
    }

    @Override
    public void showBroadcastFail(TransactionBroadcastResult transactionBroadcastResult) {
        Log.e(TAG, "showBroadcastFail: " + transactionBroadcastResult.getMessage());
        genericFail();
    }

    @Override
    public void showBroadcastSuccessful(TransactionBroadcastResult transactionBroadcastResult) {
        transactionId = transactionBroadcastResult.getTxId();
        CompletedBroadcastDTO completedBroadcastActivityDTO = new CompletedBroadcastDTO(broadcastDTO, transactionId);
        finalizeTransaction(completedBroadcastActivityDTO);
        showInitTransaction();
        showSuccess();

        if (broadcastDTO.getIdentity() != null && broadcastDTO.getIdentity().getIdentityType() == IdentityType.TWITTER) {
            analytics.trackEvent(Analytics.Companion.EVENT_TWITTER_SEND_SUCCESSFUL);
        }
    }

    @Override
    public void showProgress(int progress) {
        sendingProgressView.setProgress(progress);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        sendingProgressView = findViewById(R.id.broadcast_sending_progress);
        sendingProgressLabel = findViewById(R.id.broadcast_sending_progress_label);
        transactionIdLabel = findViewById(R.id.transaction_id_label);
        transactionIdLink = findViewById(R.id.transaction_id_link);
        transactionIdIcon = findViewById(R.id.transaction_id_link_image);
        transactionActionBtn = findViewById(R.id.transaction_complete_action_button);
        if (getIntent().hasExtra(DropbitIntents.EXTRA_BROADCAST_DTO)) {
            broadcastDTO = getIntent().getParcelableExtra(DropbitIntents.EXTRA_BROADCAST_DTO);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showInitTransaction();
        syncWalletManager.cancel30SecondSync();
    }

    @Override
    protected void onStop() {
        super.onStop();
        syncWalletManager.schedule60SecondSync();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (null != savedInstanceState && savedInstanceState.containsKey(RESTORE_STATE)) {
            sendState = SendState.valueOf(savedInstanceState.getInt(RESTORE_STATE));
            if (savedInstanceState.containsKey(TRANSACTION_ID)) {
                transactionId = savedInstanceState.getString(TRANSACTION_ID);
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(RESTORE_STATE, sendState.value);
        if (transactionId != null) {
            outState.putString(TRANSACTION_ID, transactionId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (sendState) {
            case INIT:
                startBroadcast(broadcastDTO.getTransactionData());
                break;
            case COMPLETED_SUCCESS:
                showSuccess();
                break;
            case COMPLETED_FAILED:
                genericFail();
        }
    }

    private void startBroadcast(TransactionData transactionData) {
        sendState = SendState.STARTED;
        broadcastPresenter.attachView(this);
        broadcastPresenter.broadcastTransaction(transactionData);
    }

    private void showTwitterShareCardIfNecessary() {
        if (!userPreferences.getShouldShareOnTwitter()) {
            return;
        }
        shareTransactionDialog.show(getSupportFragmentManager(), ShareTransactionDialog.class.getName());
    }

    private void genericFail() {
        sendState = SendState.COMPLETED_FAILED;
        showInitTransaction();
        sendingProgressView.setProgress(100);
        sendingProgressView.completeFail();
        transactionActionBtn.setVisibility(View.VISIBLE);
        sendingProgressLabel.setVisibility(View.VISIBLE);
        sendingProgressLabel.setText(getResources().getText(R.string.broadcast_sent_failed));
        transactionActionBtn.setText(getResources().getText(R.string.broadcast_sent_try_again));
        transactionActionBtn.setBackgroundColor(getResources().getColor(R.color.color_error));
        transactionActionBtn.setOnClickListener(view -> onRetryClicked());
    }

    private void showSuccess() {
        sendingProgressView.setProgress(100);
        sendingProgressView.completeSuccess();
        sendingProgressLabel.setVisibility(View.VISIBLE);
        transactionIdLabel.setVisibility(View.VISIBLE);
        transactionIdLink.setVisibility(View.VISIBLE);
        transactionIdIcon.setVisibility(View.VISIBLE);
        transactionActionBtn.setVisibility(View.VISIBLE);
        sendingProgressLabel.setText(getResources().getText(R.string.broadcast_sent_label));
        transactionActionBtn.setText(getResources().getText(R.string.broadcast_sent_ok));
        transactionIdLink.setText(transactionId);
        transactionActionBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        transactionIdLink.setOnClickListener(view -> exploreBlock(view.getContext(), transactionId));
        transactionIdIcon.setOnClickListener(view -> exploreBlock(view.getContext(), transactionId));
        transactionActionBtn.setOnClickListener(view -> activityNavigationUtil.navigateToHome(this));
        sendState = SendState.COMPLETED_SUCCESS;
        showTwitterShareCardIfNecessary();
    }

    private void finalizeTransaction(CompletedBroadcastDTO completedBroadcastActivityDTO) {
        Intent serviceIntent = new Intent(this, BroadcastTransactionService.class);
        serviceIntent.putExtra(DropbitIntents.EXTRA_COMPLETED_BROADCAST_DTO, completedBroadcastActivityDTO);
        startService(serviceIntent);
    }

    private void exploreBlock(Context context, String record) {
        UriUtil.openUrl(coinNinjaUriBuilder.build(TRANSACTION, record), (AppCompatActivity) context);
    }

    enum SendState {
        INIT(0), STARTED(1), COMPLETED_FAILED(2), COMPLETED_SUCCESS(3);

        private final int value;

        SendState(int value) {
            this.value = value;
        }

        public static SendState valueOf(int value) {
            for (SendState sendState : SendState.values()) {
                if (sendState.getValue() == value) return sendState;
            }

            return SendState.INIT;
        }

        public int getValue() {
            return value;
        }
    }
}
