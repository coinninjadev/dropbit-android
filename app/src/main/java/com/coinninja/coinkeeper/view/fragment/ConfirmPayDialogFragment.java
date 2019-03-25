package com.coinninja.coinkeeper.view.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.UnspentTransactionHolder;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.service.runner.FundingRunnable;
import com.coinninja.coinkeeper.service.runner.FundingTargetStatRunner;
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.BroadcastActivity;
import com.coinninja.coinkeeper.view.activity.InviteSendActivity;
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton;
import com.coinninja.coinkeeper.view.subviews.SharedMemoView;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import static com.coinninja.android.helpers.Views.withId;

public class ConfirmPayDialogFragment extends BaseDialogFragment implements ConfirmHoldButton.OnConfirmHoldEndListener, FundingTargetStatRunner.FundingTargetStatListener {

    static final int AUTHORIZE_PAYMENT_REQUEST_CODE = 10;
    PaymentBarCallbacks paymentBarCallbacks;
    UnspentTransactionHolder unspentTransactionHolder;
    @Inject
    FundingRunnable fundingRunnable;
    @Inject
    HDWallet hdWallet;
    @Inject
    Analytics analytics;
    @Inject
    DaoSessionManager daoSessionManager;
    @Inject
    AccountManager accountManager;
    String sendAddress;
    @Inject
    PhoneNumberUtil phoneNumberUtil;
    private FundingTargetStatRunner fundingAsync;
    private Contact contact;
    private PaymentHolder paymentHolder;
    private SharedMemoView sharedMemoView;

    public static ConfirmPayDialogFragment newInstance(Contact contact, PaymentHolder paymentHolder, PaymentBarCallbacks paymentBarCallbacks) {
        ConfirmPayDialogFragment fragment = new ConfirmPayDialogFragment();
        fragment.setContact(contact);
        fragment.commonInit(paymentHolder, paymentBarCallbacks);
        return fragment;
    }

    public static ConfirmPayDialogFragment newInstance(String sendAddress, Contact contact, PaymentHolder paymentHolder, PaymentBarCallbacks paymentBarCallbacks) {
        ConfirmPayDialogFragment fragment = new ConfirmPayDialogFragment();
        fragment.setSendAddress(sendAddress);
        fragment.setContact(contact);
        fragment.commonInit(paymentHolder, paymentBarCallbacks);
        return fragment;
    }

    public static ConfirmPayDialogFragment newInstance(String btcAddress, PaymentHolder paymentHolder, PaymentBarCallbacks paymentBarCallbacks) {
        ConfirmPayDialogFragment fragment = new ConfirmPayDialogFragment();
        fragment.commonInit(paymentHolder, paymentBarCallbacks);
        fragment.setSendAddress(btcAddress);
        return fragment;
    }

    public void commonInit(PaymentHolder paymentHolder, PaymentBarCallbacks paymentBarCallbacks) {
        setPaymentHolder(paymentHolder);
        setPaymentBarCallbacks(paymentBarCallbacks);
    }

    private void setPaymentBarCallbacks(PaymentBarCallbacks paymentBarCallbacks) {
        this.paymentBarCallbacks = paymentBarCallbacks;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact phoneNumberSendTo) {
        contact = phoneNumberSendTo;
    }

    public void setSendAddress(String btcSendAddress) {
        sendAddress = btcSendAddress;
    }

    public void setPaymentHolder(PaymentHolder paymentHolder) {
        this.paymentHolder = paymentHolder;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        analytics.trackEvent(Analytics.EVENT_CONFIRM_SCREEN_LOADED);
        analytics.flush();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_pay_dialog, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupClose();
        showPrice();

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

        gatherAsyncFunds();
        setupSharedMemoFragment();
        setupConfirmButton();
    }

    private void setupConfirmButton() {
        ConfirmHoldButton confirmHoldBtn = getView().findViewById(R.id.confirm_pay_hold_progress_btn);
        confirmHoldBtn.setOnConfirmHoldEndListener(this);
        confirmHoldBtn.isEnabled();
    }

    private void setupSharedMemoFragment() {
        View sharedMemo = getView().findViewById(R.id.shared_transaction_subview);
        String displayText = contact == null ? "" : contact.toDisplayNameOrInternationalPhoneNumber();
        sharedMemoView = new SharedMemoView(sharedMemo, paymentHolder.getIsSharingMemo(), paymentHolder.getMemo(), displayText);

        if (paymentHolder.getMemo() == null || "".equals(paymentHolder.getMemo())) {
            sharedMemoView.hide();
        }
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
    public void onPause() {
        super.onPause();
        if (fundingAsync != null) {
            fundingAsync.cancel(true);
            fundingAsync = null;
        }
    }

    @Override
    public void onFundingSuccessful(UnspentTransactionHolder unspentTransactionHolder, long satoshisFee) {
        this.unspentTransactionHolder = unspentTransactionHolder;
        showFees(new BTCCurrency(satoshisFee));
    }

    @Override
    public void onFundingError(String errorMessage, long satoshisFee) {
        showFees(new BTCCurrency(satoshisFee));
    }

    @Override
    public void onFundingProgress(int progress) {

    }

    private void setupClose() {
        withId(getView(), R.id.confirm_pay_header_close_btn).setOnClickListener(v -> onCloseClicked());
    }

    private void showSendAddress() {
        if (sendAddress != null && !"".equals(sendAddress)) {
            TextView view = getView().findViewById(R.id.confirm_pay_btc_address);
            view.setText(sendAddress);
        }
    }

    private void showContact() {
        TextView view = getView().findViewById(R.id.confirm_pay_name);

        view.setText(contact.toDisplayNameOrInternationalPhoneNumber());
    }

    private void gatherAsyncFunds() {
        fundingAsync = new FundingTargetStatRunner(fundingRunnable);

        BTCCurrency btcSpending = paymentHolder.getBtcCurrency();
        TransactionFee transactionFee = paymentHolder.getTransactionFee();

        long satoshisSpending = btcSpending.toSatoshis();
        String paymentAddress = sendAddress;

        fundingAsync.setListener(this);
        fundingRunnable.setCurrentChangeAddressIndex(accountManager.getNextChangeIndex());
        fundingRunnable.setPaymentAddress(paymentAddress);
        fundingRunnable.setTransactionFee(transactionFee);
        fundingRunnable.setEvaluationCurrency(paymentHolder.getEvaluationCurrency());

        fundingAsync.execute(satoshisSpending);
    }

    private void showFees(BTCCurrency feeAmount) {
        TextView fee = withId(getView(), R.id.network_fee);
        fee.setText(Resources.getString(fee.getContext(),
                R.string.confirm_pay_fee,
                feeAmount.toFormattedString(),
                feeAmount.toUSD(paymentHolder.getEvaluationCurrency()).toFormattedCurrency())
        );
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
        return contact != null && (sendAddress == null || "".equals(sendAddress));
    }

    private void sendInvite() {
        PendingInviteDTO inviteDto = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                unspentTransactionHolder.satoshisRequestingToSpend,
                unspentTransactionHolder.satoshisFeeAmount,
                paymentHolder.getMemo(),
                paymentHolder.getIsSharingMemo()
        );
        Intent intent = new Intent(getActivity(), InviteSendActivity.class);
        intent.putExtra(Intents.EXTRA_INVITE_DTO, inviteDto);
        startActivity(intent);
        dismiss();
    }

    private void broadcastTransaction() {
        Intent intent = new Intent(getActivity(), BroadcastActivity.class);
        BroadcastTransactionDTO broadcastTransactionDTO = new BroadcastTransactionDTO(unspentTransactionHolder,
                contact, paymentHolder.getIsSharingMemo(), paymentHolder.getMemo(), paymentHolder.getPublicKey());
        intent.putExtra(Intents.EXTRA_BROADCAST_DTO, broadcastTransactionDTO);
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


