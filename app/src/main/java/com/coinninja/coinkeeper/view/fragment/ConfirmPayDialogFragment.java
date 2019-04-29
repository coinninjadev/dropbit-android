package com.coinninja.coinkeeper.view.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.BroadcastActivity;
import com.coinninja.coinkeeper.view.activity.InviteSendActivity;
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton;
import com.coinninja.coinkeeper.view.subviews.SharedMemoView;

import javax.inject.Inject;

import static com.coinninja.android.helpers.Views.withId;

public class ConfirmPayDialogFragment extends BaseBottomDialogFragment implements ConfirmHoldButton.OnConfirmHoldEndListener {

    static final int AUTHORIZE_PAYMENT_REQUEST_CODE = 10;

    @Inject
    Analytics analytics;
    PaymentBarCallbacks paymentBarCallbacks;
    private Contact contact;
    private PaymentHolder paymentHolder;

    public static ConfirmPayDialogFragment newInstance(Contact contact, PaymentHolder paymentHolder, PaymentBarCallbacks paymentBarCallbacks) {
        ConfirmPayDialogFragment fragment = new ConfirmPayDialogFragment();
        fragment.setContact(contact);
        fragment.commonInit(paymentHolder, paymentBarCallbacks);
        return fragment;
    }

    public static ConfirmPayDialogFragment newInstance(PaymentHolder paymentHolder, PaymentBarCallbacks paymentBarCallbacks) {
        ConfirmPayDialogFragment fragment = new ConfirmPayDialogFragment();
        fragment.commonInit(paymentHolder, paymentBarCallbacks);
        return fragment;
    }

    public void commonInit(PaymentHolder paymentHolder, PaymentBarCallbacks paymentBarCallbacks) {
        setPaymentHolder(paymentHolder);
        setPaymentBarCallbacks(paymentBarCallbacks);
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact phoneNumberSendTo) {
        contact = phoneNumberSendTo;
    }

    public void setPaymentHolder(PaymentHolder paymentHolder) {
        this.paymentHolder = paymentHolder;
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
        showPrice();
        analytics.trackEvent(Analytics.EVENT_CONFIRM_SCREEN_LOADED);
        analytics.flush();

        switch (getDisplayState()) {
            case INVITE_CONTACT:
                showContact();
                analytics.trackEvent(Analytics.EVENT_DROPBIT_SEND);
                break;
            case CONTACT:
                showContact();
                showSendAddress();
                analytics.trackEvent(Analytics.EVENT_CONTACT_SEND);
                break;
            case PHONE_NUMBER_ONLY:
                showContact();
                showSendAddress();
                analytics.trackEvent(Analytics.EVENT_CONTACT_SEND);
                break;
            case BTC_ADDRESS_ONLY:
                showSendAddress();
                analytics.trackEvent(Analytics.EVENT_BROADCAST_TO_ADDRESS);
                break;
        }

        setupSharedMemoFragment();
        setupConfirmButton();
        showFees(new BTCCurrency(paymentHolder.getTransactionData().getFeeAmount()));
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

        String primaryAmount = paymentHolder.getPrimaryCurrency().toFormattedCurrency();
        String secondaryAmount = paymentHolder.getSecondaryCurrency().toFormattedCurrency();

        primaryCurrencyView.setText(primaryAmount);
        secondaryCurrencyView.setText(secondaryAmount);
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
        String displayText = contact == null ? "" : contact.toDisplayNameOrInternationalPhoneNumber();
        SharedMemoView sharedMemoView = new SharedMemoView(sharedMemo, paymentHolder.getIsSharingMemo(), paymentHolder.getMemo(), displayText);
        sharedMemoView.render();
    }

    private void setupClose() {
        withId(getView(), R.id.confirm_pay_header_close_btn).setOnClickListener(v -> onCloseClicked());
    }

    private void showSendAddress() {
        if (paymentHolder.hasPaymentAddress()) {
            TextView view = getView().findViewById(R.id.confirm_pay_btc_address);
            view.setText(paymentHolder.getPaymentAddress());
        }
    }

    private void showContact() {
        TextView view = getView().findViewById(R.id.confirm_pay_name);

        view.setText(contact.toDisplayNameOrInternationalPhoneNumber());
    }

    private void showFees(BTCCurrency feeAmount) {
        TextView fee = withId(getView(), R.id.network_fee);
        fee.setText(Resources.getString(fee.getContext(),
                R.string.confirm_pay_fee,
                feeAmount.toFormattedString(),
                feeAmount.toUSD(paymentHolder.getEvaluationCurrency()).toFormattedCurrency())
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
        return contact != null && !paymentHolder.hasPaymentAddress();
    }

    private void sendInvite() {
        PendingInviteDTO inviteDto = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                paymentHolder.getTransactionData().getAmount(),
                paymentHolder.getTransactionData().getFeeAmount(),
                paymentHolder.getMemo(),
                paymentHolder.getIsSharingMemo()
        );
        Intent intent = new Intent(getActivity(), InviteSendActivity.class);
        intent.putExtra(DropbitIntents.EXTRA_INVITE_DTO, inviteDto);
        startActivity(intent);
        dismiss();
    }

    private void broadcastTransaction() {
        Intent intent = new Intent(getActivity(), BroadcastActivity.class);
        BroadcastTransactionDTO broadcastTransactionDTO = new BroadcastTransactionDTO(paymentHolder.getTransactionData(), contact, paymentHolder.getIsSharingMemo(), paymentHolder.getMemo(), paymentHolder.getPublicKey());
        intent.putExtra(DropbitIntents.EXTRA_BROADCAST_DTO, broadcastTransactionDTO);
        startActivity(intent);
        dismiss();
    }


    private DisplayState getDisplayState() {
        boolean hasPhoneNumber = contact != null && contact.getPhoneNumber() != null;

        if (!hasPhoneNumber) {
            return DisplayState.BTC_ADDRESS_ONLY;
        }

        String displayName = contact.getDisplayName();
        if (!contact.isVerified()) {
            return DisplayState.INVITE_CONTACT;
        }


        boolean hasDisplayName = displayName != null && !displayName.isEmpty();
        if (hasDisplayName) {
            return DisplayState.CONTACT;
        } else {
            return DisplayState.PHONE_NUMBER_ONLY;
        }

    }

    public enum DisplayState {
        CONTACT, INVITE_CONTACT, PHONE_NUMBER_ONLY, BTC_ADDRESS_ONLY
    }
}


