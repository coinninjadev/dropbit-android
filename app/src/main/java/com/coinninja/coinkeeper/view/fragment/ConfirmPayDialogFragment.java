package com.coinninja.coinkeeper.view.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks;
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.FeesManager;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.BroadcastActivity;
import com.coinninja.coinkeeper.view.activity.InviteSendActivity;
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton;
import com.coinninja.coinkeeper.view.subviews.SharedMemoView;
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import static com.coinninja.android.helpers.Views.withId;

public class ConfirmPayDialogFragment extends BaseBottomDialogFragment implements ConfirmHoldButton.OnConfirmHoldEndListener {

    static final int AUTHORIZE_PAYMENT_REQUEST_CODE = 10;

    @Inject
    Analytics analytics;

    @Inject
    FeesManager feesManager;

    @Inject
    TransactionFundingManager transactionFundingManager;

    PaymentBarCallbacks paymentBarCallbacks;
    private Identity identity;
    private PaymentUtil paymentUtil;

    public static ConfirmPayDialogFragment newInstance(Identity identity, PaymentUtil paymentUtil, PaymentBarCallbacks paymentBarCallbacks) {
        ConfirmPayDialogFragment fragment = new ConfirmPayDialogFragment();
        fragment.setIdentity(identity);
        fragment.commonInit(paymentUtil, paymentBarCallbacks);
        return fragment;
    }

    public static ConfirmPayDialogFragment newInstance(PaymentUtil paymentUtil, PaymentBarCallbacks paymentBarCallbacks) {
        ConfirmPayDialogFragment fragment = new ConfirmPayDialogFragment();
        fragment.commonInit(paymentUtil, paymentBarCallbacks);
        return fragment;
    }

    public void commonInit(PaymentUtil paymentUtil, PaymentBarCallbacks paymentBarCallbacks) {
        this.paymentUtil = paymentUtil;
        setPaymentBarCallbacks(paymentBarCallbacks);
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identityToSendTo) {
        identity = identityToSendTo;
    }

    public PaymentHolder getPaymentHolder() {
        return paymentUtil.getPaymentHolder();
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
        setupClose();
        analytics.trackEvent(Analytics.EVENT_CONFIRM_SCREEN_LOADED);
        analytics.flush();

        switch (getDisplayState()) {
            case INVITE:
                showIdentity();
                analytics.trackEvent(Analytics.EVENT_DROPBIT_SEND);
                break;
            case CONTACT:
                showIdentity();
                analytics.trackEvent(Analytics.EVENT_CONTACT_SEND);
                break;
            case PHONE_NUMBER_ONLY:
                showIdentity();
                analytics.trackEvent(Analytics.EVENT_CONTACT_SEND);
                break;
            case BTC_ADDRESS_ONLY:
                analytics.trackEvent(Analytics.EVENT_BROADCAST_TO_ADDRESS);
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

    private void setupAdjustedFeesUI() {
        if (feesManager.isAdjustableFeesEnabled()) {
            getView().findViewById(R.id.adjustable_fee_configuration_group).setVisibility(View.VISIBLE);
            setupCurrentlySelectedFeeUI();
        } else {
            getView().findViewById(R.id.adjustable_fee_configuration_group).setVisibility(View.GONE);
        }

        getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_left).setOnClickListener(v -> adjustedFeeButtonClicked(v));
        getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_center).setOnClickListener(v -> adjustedFeeButtonClicked(v));
        getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_right).setOnClickListener(v -> adjustedFeeButtonClicked(v));
    }

    private void setNewFeeForView(View view) {
        long amount = getPaymentHolder().getTransactionData().getAmount();

        if (view == getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_left)) {
            getPaymentHolder().setTransactionData(transactionFundingManager.buildFundedTransactionData(paymentUtil.getAddress(), feesManager.fee(FeesManager.FeeType.FAST), amount));
        } else if (view == getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_center)) {
            getPaymentHolder().setTransactionData(transactionFundingManager.buildFundedTransactionData(paymentUtil.getAddress(), feesManager.fee(FeesManager.FeeType.SLOW), amount));
        } else if (view == getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_right)) {
            getPaymentHolder().setTransactionData(transactionFundingManager.buildFundedTransactionData(paymentUtil.getAddress(), feesManager.fee(FeesManager.FeeType.CHEAP), amount));
        }
    }

    private void setApproximateTimeForFee(View view) {
        if (view == getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_left)) {
            ((TextView) getView().findViewById(R.id.estimated_delivery_time)).setText(R.string.approx_ten_minutes);
        } else if (view == getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_center)) {
            ((TextView) getView().findViewById(R.id.estimated_delivery_time)).setText(R.string.approx_hour_wait);
        } else if (view == getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_right)) {
            ((TextView) getView().findViewById(R.id.estimated_delivery_time)).setText(R.string.approx_day_wait);
        }
    }

    private Double feeForView(View view) {
        if (view == getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_left)) {
            return feesManager.fee(FeesManager.FeeType.FAST);
        } else if (view == getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_center)) {
            return feesManager.fee(FeesManager.FeeType.SLOW);
        } else if (view == getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_right)) {
            return feesManager.fee(FeesManager.FeeType.CHEAP);
        }

        return feesManager.fee(FeesManager.FeeType.FAST);
    }

    private void adjustedFeeButtonClicked(View view) {
        if (paymentUtil.isSendingMax()) {
            paymentUtil.setFee(feeForView(view));
            paymentUtil.fundMax();
            getPaymentHolder().updateValue(new BTCCurrency(getPaymentHolder().getTransactionData().getAmount()));
            setApproximateTimeForFee(view);
        } else if (paymentUtil.isTransactionFundableWithFee(feeForView(view), paymentUtil.getPaymentHolder().getTransactionData().getAmount())) {
            setNewFeeForView(view);
            setApproximateTimeForFee(view);
        } else {
            AlertDialogBuilder.build(getContext(), getString(R.string.fee_too_high_error)).show();
            setupCurrentlySelectedFeeUI();
        }

        setupFeeUI();
    }

    private void setupFeeUI() {
        showPrice();
        showFees(new BTCCurrency(getPaymentHolder().getTransactionData().getFeeAmount()));
    }

    private void setupCurrentlySelectedFeeUI() {
        RadioButton button = getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_left);

        switch (feesManager.getFeePreference()) {
            case FAST:
                break;
            case SLOW:
                button = getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_center);
                break;
            case CHEAP:
                button = getView().findViewById(R.id.adjustable_fees_fragment).findViewById(R.id.radio_right);
                break;
            default:

        }

        button.setChecked(true);
        setApproximateTimeForFee(button);
    }

    private void setPaymentBarCallbacks(PaymentBarCallbacks paymentBarCallbacks) {
        this.paymentBarCallbacks = paymentBarCallbacks;
    }

    private void setupConfirmButton() {
        ConfirmHoldButton confirmHoldBtn = getView().findViewById(R.id.confirm_pay_hold_progress_btn);
        confirmHoldBtn.setOnConfirmHoldEndListener(this);
        confirmHoldBtn.isEnabled();
    }

    private void setupSharedMemoFragment() {
        View sharedMemo = getView().findViewById(R.id.shared_transaction_subview);
        String displayText = identity == null ? "" : identity.getDisplayName();
        SharedMemoView sharedMemoView = new SharedMemoView(sharedMemo, getPaymentHolder().getIsSharingMemo(), getPaymentHolder().getMemo(), displayText);
        sharedMemoView.render();
    }

    private void setupClose() {
        withId(getView(), R.id.confirm_pay_header_close_btn).setOnClickListener(v -> onCloseClicked());
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
        TextView fee = withId(getView(), R.id.network_fee);
        fee.setText(Resources.INSTANCE.getString(fee.getContext(),
                R.string.confirm_pay_fee,
                feeAmount.toFormattedString(),
                feeAmount.toUSD(getPaymentHolder().getEvaluationCurrency()).toFormattedCurrency())
        );


    }

    private void onCloseClicked() {
        paymentBarCallbacks.cancelPayment(this);
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
                getPaymentHolder().getIsSharingMemo(),
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
                getPaymentHolder().getIsSharingMemo(), getPaymentHolder().getMemo(), identity, getPaymentHolder().getPublicKey());
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


