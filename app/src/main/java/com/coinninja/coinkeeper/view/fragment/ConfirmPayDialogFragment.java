package com.coinninja.coinkeeper.view.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.FeesManager;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.BroadcastActivity;
import com.coinninja.coinkeeper.view.activity.InviteSendActivity;
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton;
import com.coinninja.coinkeeper.view.subviews.SharedMemoView;
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import app.coinninja.cn.libbitcoin.model.TransactionData;
import app.dropbit.commons.currency.BTCCurrency;

import static com.coinninja.coinkeeper.util.FeesManager.FeeType;
import static com.coinninja.coinkeeper.util.FeesManager.FeeType.CHEAP;
import static com.coinninja.coinkeeper.util.FeesManager.FeeType.FAST;
import static com.coinninja.coinkeeper.util.FeesManager.FeeType.SLOW;

public class ConfirmPayDialogFragment extends BaseBottomDialogFragment implements ConfirmHoldButton.OnConfirmHoldEndListener, TabLayout.OnTabSelectedListener {

    static final int AUTHORIZE_PAYMENT_REQUEST_CODE = 10;

    @Inject
    Analytics analytics;

    @Inject
    FeesManager feesManager;

    @Inject
    TransactionFundingManager transactionFundingManager;

    FeeType feePref = FAST;

    private Identity identity;
    private PaymentUtil paymentUtil;

    public static ConfirmPayDialogFragment newInstance(Identity identity, PaymentUtil paymentUtil) {
        ConfirmPayDialogFragment fragment = new ConfirmPayDialogFragment();
        fragment.setIdentity(identity);
        fragment.commonInit(paymentUtil);
        return fragment;
    }

    public static ConfirmPayDialogFragment newInstance(PaymentUtil paymentUtil) {
        ConfirmPayDialogFragment fragment = new ConfirmPayDialogFragment();
        fragment.commonInit(paymentUtil);
        return fragment;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identityToSendTo) {
        identity = identityToSendTo;
    }

    @Override
    public void onHoldCompleteSuccessfully() {
        startActivityForResult(new Intent(getActivity(), AuthorizedActionActivity.class), AUTHORIZE_PAYMENT_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == AUTHORIZE_PAYMENT_REQUEST_CODE && resultCode == AuthorizedActionActivity.RESULT_AUTHORIZED) {
            startBroadcast();
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        feePref = feesManager.getFeePreference();
        analytics.trackEvent(Analytics.Companion.EVENT_CONFIRM_SCREEN_LOADED);
        analytics.flush();

        switch (getDisplayState()) {
            case INVITE:
                showIdentity();
                analytics.trackEvent(Analytics.Companion.EVENT_DROPBIT_SEND);
                break;
            case CONTACT:
                showIdentity();
                analytics.trackEvent(Analytics.Companion.EVENT_CONTACT_SEND);
                break;
            case PHONE_NUMBER_ONLY:
                showIdentity();
                analytics.trackEvent(Analytics.Companion.EVENT_CONTACT_SEND);
                break;
            case BTC_ADDRESS_ONLY:
                analytics.trackEvent(Analytics.Companion.EVENT_BROADCAST_TO_ADDRESS);
                break;
        }

        showSendAddressIfNecessary();
        setupSharedMemoFragment();
        setupConfirmButton();
        setupAdjustedFeesUI();
        setupFeeUI();

    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.fragment_confirm_pay_dialog;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case 0:
                fundWithNewFee(FAST);
                break;
            case 1:
                fundWithNewFee(SLOW);
                break;
            case 2:
                fundWithNewFee(CHEAP);
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    public void showPrice() {
        TextView primaryCurrencyView = getView().findViewById(R.id.primary_currency);
        TextView secondaryCurrencyView = getView().findViewById(R.id.secondary_currency);

        primaryCurrencyView.setVisibility(View.VISIBLE);
        secondaryCurrencyView.setVisibility(View.VISIBLE);

        String primaryAmount = getPaymentHolder().getPrimaryCurrency().toFormattedCurrency();
        String secondaryAmount = getPaymentHolder().getSecondaryCurrency().toFormattedCurrency();

        primaryCurrencyView.setText(primaryAmount);
        secondaryCurrencyView.setText(secondaryAmount);
    }

    private void commonInit(PaymentUtil paymentUtil) {
        this.paymentUtil = paymentUtil;
    }

    private PaymentHolder getPaymentHolder() {
        return paymentUtil.getPaymentHolder();
    }

    private void setupAdjustedFeesUI() {
        if (feesManager.isAdjustableFeesEnabled()) {
            getView().findViewById(R.id.adjustable_fee_configuration_group).setVisibility(View.VISIBLE);
            setupCurrentlySelectedFeeUI();
        } else {
            getView().findViewById(R.id.adjustable_fee_configuration_group).setVisibility(View.GONE);
        }
    }

    private void fundWithNewFee(FeeType feeType) {
        TransactionData transactionData;
        if (paymentUtil.isSendingMax()) {
            transactionData = transactionFundingManager.buildFundedTransactionData(
                    paymentUtil.getAddress(),
                    feesManager.fee(feeType)
            );
        } else {
            transactionData = transactionFundingManager.buildFundedTransactionData(
                    paymentUtil.getAddress(),
                    feesManager.fee(feeType),
                    getPaymentHolder().getTransactionData().getAmount(), null
            );
        }

        if (transactionData.getAmount() > 0) {
            feePref = feeType;
            getPaymentHolder().setTransactionData(transactionData);
        } else {
            AlertDialogBuilder.build(getContext(), getString(R.string.fee_too_high_error)).show();
        }

        updateWaitTime();
        setupFeeUI();
    }

    private void updateWaitTime() {
        String waitTime;

        switch (feePref) {
            case FAST:
                waitTime = getString(R.string.approx_ten_minutes);
                break;
            case SLOW:
                waitTime = getString(R.string.approx_hour_wait);
                break;
            default:
                waitTime = getString(R.string.day);
                break;
        }

        ((TextView) getView().findViewById(R.id.estimated_delivery_time)).setText(waitTime);
    }

    private void setupFeeUI() {
        showPrice();
        showFees(new BTCCurrency(getPaymentHolder().getTransactionData().getFeeAmount()));
    }

    private void setupCurrentlySelectedFeeUI() {
        TabLayout tabLayout = getView().findViewById(R.id.adjustable_fees).findViewById(R.id.adjustable_fees);

        switch (feePref) {
            case FAST:
                tabLayout.selectTab(tabLayout.getTabAt(0));
                break;
            case SLOW:
                tabLayout.selectTab(tabLayout.getTabAt(1));
                break;
            default:
                tabLayout.selectTab(tabLayout.getTabAt(2));
                break;
        }
        tabLayout.addOnTabSelectedListener(this);
        updateWaitTime();
    }


    private void setupConfirmButton() {
        ConfirmHoldButton confirmHoldBtn = getView().findViewById(R.id.confirm_pay_hold_progress_btn);
        confirmHoldBtn.setOnConfirmHoldEndListener(this);
        confirmHoldBtn.isEnabled();
    }

    private void setupSharedMemoFragment() {
        View sharedMemo = getView().findViewById(R.id.shared_transaction_subview);
        String displayText = identity == null ? "" : identity.getDisplayName();
        SharedMemoView sharedMemoView = new SharedMemoView(sharedMemo, getPaymentHolder().isSharingMemo(), getPaymentHolder().getMemo(), displayText);
        sharedMemoView.render();
    }

    private void showSendAddressIfNecessary() {
        if (!getPaymentHolder().hasPaymentAddress()) {
            return;
        }
        TextView view = getView().findViewById(R.id.confirm_pay_btc_address);
        view.setText(getPaymentHolder().getPaymentAddress());
    }

    private void showIdentity() {
        TextView nameTextView = getView().findViewById(R.id.confirm_pay_name);

        if (identity.getAvatarUrl() != null) {
            Picasso.get().load(identity.getAvatarUrl()).transform(CoinKeeperApplication.appComponent.provideCircleTransform()).into(((ImageView) getView().findViewById(R.id.avatar_image_view)));
        }

        nameTextView.setText(identity.getDisplayName());
    }

    private void showFees(BTCCurrency feeAmount) {
        TextView fee = findViewById(R.id.network_fee);
        fee.setText(Resources.INSTANCE.getString(fee.getContext(),
                R.string.confirm_pay_fee,
                feeAmount.toFormattedString(),
                feeAmount.toUSD(getPaymentHolder().getEvaluationCurrency()).toFormattedCurrency())
        );
    }


    private void startBroadcast() {
        if (shouldSendInvite()) {
            sendInvite();
        } else {
            broadcastTransaction();
        }
    }

    private boolean shouldSendInvite() {
        return identity != null && !getPaymentHolder().hasPaymentAddress();
    }

    private void sendInvite() {
        PendingInviteDTO inviteDto = new PendingInviteDTO(identity,
                getPaymentHolder().getEvaluationCurrency().toLong(),
                getPaymentHolder().getTransactionData().getAmount(),
                getPaymentHolder().getTransactionData().getFeeAmount(),
                getPaymentHolder().getMemo(),
                getPaymentHolder().isSharingMemo(),
                ""
        );
        Intent intent = new Intent(getActivity(), InviteSendActivity.class);
        intent.putExtra(DropbitIntents.EXTRA_INVITE_DTO, inviteDto);
        startActivity(intent);
        dismiss();
    }

    private void broadcastTransaction() {
        Intent intent = new Intent(getActivity(), BroadcastActivity.class);
        BroadcastTransactionDTO broadcastTransactionDTO = new BroadcastTransactionDTO(getPaymentHolder().getTransactionData(),
                getPaymentHolder().isSharingMemo(), getPaymentHolder().getMemo(), identity, getPaymentHolder().getPublicKey());
        intent.putExtra(DropbitIntents.EXTRA_BROADCAST_DTO, broadcastTransactionDTO);
        startActivity(intent);
        dismiss();
    }


    private DisplayState getDisplayState() {
        boolean hasIdentity = identity != null;

        if (!hasIdentity) {
            return DisplayState.BTC_ADDRESS_ONLY;
        }

        String displayName = identity.getDisplayName();
        if (!identity.isVerified()) {
            return DisplayState.INVITE;
        }

        boolean hasDisplayName = displayName != null && !displayName.isEmpty();
        if (hasDisplayName) {
            return DisplayState.CONTACT;
        } else {
            return DisplayState.PHONE_NUMBER_ONLY;
        }

    }

    public enum DisplayState {
        CONTACT, INVITE, PHONE_NUMBER_ONLY, BTC_ADDRESS_ONLY
    }
}


