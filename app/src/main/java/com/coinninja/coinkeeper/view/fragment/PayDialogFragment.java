package com.coinninja.coinkeeper.view.fragment;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate;
import com.coinninja.coinkeeper.di.interfaces.CountryCodeLocales;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks;
import com.coinninja.coinkeeper.service.callbacks.BasicCallbackHandler;
import com.coinninja.coinkeeper.service.callbacks.Bip70Callback;
import com.coinninja.coinkeeper.service.client.Bip70Client;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.MerchantPaymentRequestOutput;
import com.coinninja.coinkeeper.service.client.model.MerchantResponse;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment;
import com.coinninja.coinkeeper.ui.payment.PaymentInputView;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.PickContactActivity;
import com.coinninja.coinkeeper.view.activity.QrScanActivity;
import com.coinninja.coinkeeper.view.subviews.SharedMemoToggleView;
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder;
import com.coinninja.coinkeeper.view.widget.PaymentReceiverView;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.coinninja.android.helpers.Views.shakeInError;
import static com.coinninja.android.helpers.Views.withId;

public class PayDialogFragment extends BaseDialogFragment {
    public static final int PICK_CONTACT_REQUEST = 100001;

    @Inject
    CNAddressLookupDelegate cnAddressLookupDelegate;
    @Inject
    Analytics analytics;
    @Inject
    WalletHelper walletHelper;
    @Inject
    BitcoinUtil bitcoinUtil;
    @Inject
    ClipboardUtil clipboardUtil;
    @Inject
    UserPreferences preferenceInteractor;
    @Inject
    CoinKeeperApplication application;
    @Inject
    SharedMemoToggleView memoToggleView;
    @Inject
    @CountryCodeLocales
    List<CountryCodeLocale> countryCodeLocales;
    @Inject
    Bip70Client bip70Client;

    PaymentBarCallbacks paymentBarCallbacks;
    PaymentUtil paymentUtil;
    PaymentHolder paymentHolder;
    PaymentReceiverView paymentReceiverView;
    PaymentInputView paymentInputView;
    AlertDialog progressSpinner;
    Bip70Callback bip70Callback;

    boolean shouldShowScanOnAttach = false;
    BitcoinUri injectedBitcoinUri = null;

    public static PayDialogFragment newInstance(PaymentUtil paymentUtil, PaymentBarCallbacks paymentBarCallbacks, boolean shouldShowScanOnAttach) {
        PayDialogFragment payFragment = commonInit(paymentUtil, paymentBarCallbacks);
        payFragment.shouldShowScanOnAttach = shouldShowScanOnAttach;
        return payFragment;
    }

    public static PayDialogFragment newInstance(PaymentUtil paymentUtil, PaymentBarCallbacks paymentBarCallbacks, BitcoinUri injectedBitcoinUri) {
        PayDialogFragment payFragment = commonInit(paymentUtil, paymentBarCallbacks);
        payFragment.injectedBitcoinUri = injectedBitcoinUri;
        return payFragment;
    }

    private static PayDialogFragment commonInit(PaymentUtil paymentUtil, PaymentBarCallbacks paymentBarCallbacks) {
        PayDialogFragment payFragment = new PayDialogFragment();
        payFragment.paymentBarCallbacks = paymentBarCallbacks;
        payFragment.paymentUtil = paymentUtil;
        payFragment.paymentHolder = paymentUtil.getPaymentHolder();
        return payFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayDialogFragment.PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            onPickContactResult(data);
        } else if (requestCode == Intents.REQUEST_QR_FRAGMENT_SCAN && resultCode == RESULT_CANCELED) {
            onCloseClicked();
        } else if (requestCode == Intents.REQUEST_QR_FRAGMENT_SCAN) {
            onQrScanResult(resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay_dialog, container, false);
        memoToggleView.render(getActivity(), view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        paymentInputView.requestFocus();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        analytics.trackEvent(Analytics.EVENT_PAY_SCREEN_LOADED);
        analytics.flush();

        if (shouldShowScanOnAttach) {
            startScan();
            shouldShowScanOnAttach = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        memoToggleView.tearDown();
        paymentUtil.reset();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        cnAddressLookupDelegate.teardown();
        super.onDismiss(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        USDCurrency.SET_MAX_LIMIT((USDCurrency) paymentHolder.getEvaluationCurrency());
        paymentHolder.setMaxLimitForFiat();
        setupView();
        setupBip70Callback();
        processBitcoinUriIfNecessary(injectedBitcoinUri);
    }

    public void onPaymentAddressChange(String text) {
        paymentHolder.clearPayment();
        paymentUtil.setAddress(text);
        paymentReceiverView.setPaymentAddress(text);
        showSendToInput();
        updateSharedMemosUI();
        paymentInputView.requestFocus();
    }

    public void showPasteAttemptFail(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public void onPhoneNumberValid(Phonenumber.PhoneNumber phoneNumber) {
        cnAddressLookupDelegate.fetchAddressFor(new PhoneNumber(phoneNumber), this::onFetchContactComplete);
        paymentUtil.setContact(new Contact(new PhoneNumber(phoneNumber), "", false));
        updateSharedMemosUI();
    }

    public void onPhoneNumberInvalid(String text) {
        paymentReceiverView.clear();
        paymentUtil.setContact(null);
        shakeInError(paymentReceiverView);
    }

    public void onFetchContactComplete(AddressLookupResult addressLookupResult) {
        paymentHolder.setPublicKey(addressLookupResult.getAddressPubKey());
        paymentHolder.setPaymentAddress(addressLookupResult.getAddress());
        updateSharedMemosUI();
    }

    public PaymentUtil getPaymentUtil() {
        return paymentUtil;
    }

    void startContactInviteFlow(Contact pickedContact) {
        if (preferenceInteractor.getShouldShowInviteHelp()) {
            showInviteHelpScreen(pickedContact);
        } else {
            inviteContact(pickedContact);
        }
    }

    void onInviteHelpAccepted(Contact pickedContact) {
        DialogFragment fragment = (DialogFragment) getFragmentManager().findFragmentByTag(InviteHelpDialogFragment.TAG);
        if (fragment != null) {
            fragment.dismiss();
        }

        inviteContact(pickedContact);
    }

    void inviteContact(Contact pickedContact) {
        setMemoOnPayment();
        paymentBarCallbacks.confirmInvite(pickedContact);
        dismiss();
    }

    void sendPaymentTo() {
        setMemoOnPayment();
        paymentBarCallbacks.confirmPaymentFor(paymentUtil.getAddress());
        dismiss();
    }

    void sendPaymentTo(String address, Contact phoneNumber) {
        setMemoOnPayment();
        paymentBarCallbacks.confirmPaymentFor(address, phoneNumber);
        dismiss();
    }

    void onComplete(FundingUTXOs fundingUTXOs) {
        if (paymentUtil.isFunded()) {
            sendPayment();
        } else {
            invalidPayment();
        }
    }

    protected void showError(String message) {
        AlertDialogBuilder.build(getActivity(), message)
                .show();
    }

    protected void onPasteClicked() {
        String cryptoUriString = clipboardUtil.getRaw();
        if (cryptoUriString == null || cryptoUriString.isEmpty()) {
            String reason = getString(R.string.clipboard_empty_error_message);
            showPasteAttemptFail(reason);
        } else {
            onCryptoStringReceived(cryptoUriString);
        }
    }

    private void onPickContactResult(Intent data) {
        Contact contact = data.getExtras().getParcelable(Intents.EXTRA_CONTACT);
        setContactResult(contact);
    }

    private void setupView() {
        View base = getView();
        configureButtons(base);
        configureSharedMemo();
        configurePaymentReceiver();
        configurePaymentInput(base);
    }

    private void configurePaymentInput(View base) {
        paymentInputView = withId(base, R.id.payment_input_view);
        paymentInputView.setPaymentHolder(paymentHolder);
    }

    private void setupBip70Callback() {
        BasicCallbackHandler<MerchantResponse> handler = new BasicCallbackHandler<MerchantResponse>() {

            @Override
            public void success(MerchantResponse object) {
                commonCompletion();
                setBip70UriParameters(object);
            }

            @Override
            public void failure(String message) {
                commonCompletion();
                AlertDialogBuilder.build(getView().getContext(), message).show();
            }

            private void commonCompletion() {
                if (progressSpinner == null) { return; }
                progressSpinner.dismiss();
                progressSpinner = null;
            }
        };

        bip70Callback = new Bip70Callback(handler);
    }

    private void configureButtons(View base) {
        withId(base, R.id.pay_footer_send_btn).setOnClickListener(v -> onSendBtnClicked());
        withId(base, R.id.scan_btc_address_btn).setOnClickListener(v -> onScanClicked());
        withId(base, R.id.pay_header_close_btn).setOnClickListener(v -> onCloseClicked());
        withId(base, R.id.paste_address_btn).setOnClickListener(v -> onPasteClicked());
        withId(base, R.id.contacts_btn).setOnClickListener(v -> onContactsClicked());
    }

    private void configureSharedMemo() {
        if (!walletHelper.hasVerifiedAccount()) {
            paymentHolder.setIsSharingMemo(false);
        }
    }

    private void configurePaymentReceiver() {
        String initialSendTo = paymentUtil.getAddress() == null ? "" : paymentUtil.getAddress();
        paymentReceiverView = withId(getView(), R.id.payment_receiver);
        paymentReceiverView.setOnValidPhoneNumberObserver(this::onPhoneNumberValid);
        paymentReceiverView.setOnInvalidPhoneNumberObserver(this::onPhoneNumberInvalid);
        paymentReceiverView.setCountryCodeLocales(countryCodeLocales);
        paymentReceiverView.setPaymentAddress(initialSendTo);
    }

    private void onPaymentChange(Currency currency) {
        paymentHolder.updateValue(currency);
        paymentInputView.setPaymentHolder(paymentHolder);
    }

    private void updateSharedMemosUI() {
        if (paymentHolder.hasPubKey() || paymentUtil.getPaymentMethod() == PaymentUtil.PaymentMethod.INVITE) {
            memoToggleView.showSharedMemoViews();
        } else {
            memoToggleView.hideSharedMemoViews();
        }
    }

    private void onSendBtnClicked() {
        if (paymentUtil.isValid()) {
            paymentUtil.checkFunding(this::onComplete);
        } else {
            invalidPayment();
        }
    }

    private void invalidPayment() {
        String errorMessage = paymentUtil.getErrorMessage();
        if (null == errorMessage || "".equals(errorMessage))
            errorMessage = getString(R.string.pay_error_add_valid_bitcoin_address);
        showError(errorMessage);
        paymentUtil.clearErrors();
    }

    private void onContactsClicked() {
        if (walletHelper.hasVerifiedAccount()) {
            startActivityForResult(new Intent(getActivity(), PickContactActivity.class), PICK_CONTACT_REQUEST);
        } else {
            getActivity().startActivity(new Intent(getActivity(), VerifyPhoneNumberActivity.class));
        }
    }

    private void onScanClicked() {
        startScan();
    }

    private void startScan() {
        Intent qrScanIntent = new Intent(getActivity(), QrScanActivity.class);
        startActivityForResult(qrScanIntent, Intents.REQUEST_QR_FRAGMENT_SCAN);
    }

    private void setMemoOnPayment() {
        paymentHolder.setMemo(memoToggleView.getMemo());
        paymentHolder.setIsSharingMemo(memoToggleView.isSharing());
    }

    private void sendPayment() {
        switch (paymentUtil.getPaymentMethod()) {
            case ADDRESS:
                sendPaymentTo();
                break;
            case INVITE:
            case VERIFIED_CONTACT:
                sendPaymentToContact();
                break;
            default:
        }
    }

    private void sendPaymentToContact() {
        if (walletHelper.hasVerifiedAccount()) {
            if (paymentHolder.hasPaymentAddress()) {
                sendPaymentTo(paymentHolder.getPaymentAddress(), paymentUtil.getContact());
            } else if (paymentUtil.isVerifiedContact()) {
                inviteContact(paymentUtil.getContact());
            } else {
                startContactInviteFlow(paymentUtil.getContact());
            }
        } else {
            getActivity().startActivity(new Intent(getActivity(), VerifyPhoneNumberActivity.class));
        }
    }

    private void showInviteHelpScreen(Contact contact) {
        DialogFragment dialogFragment = InviteHelpDialogFragment.newInstance(preferenceInteractor,
                contact,
                () -> onInviteHelpAccepted(contact));

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(InviteHelpDialogFragment.TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, InviteHelpDialogFragment.TAG);
    }

    private void onCloseClicked() {
        paymentHolder.setMemo(null);
        paymentHolder.clearPayment();
        paymentBarCallbacks.cancelPayment(this);
    }

    private void setContactResult(Contact contact) {
        paymentUtil.setContact(contact);
        hideSendToInput();
        ((TextView) getView().findViewById(R.id.contact_name)).setText(contact.getDisplayName());
        ((TextView) getView().findViewById(R.id.contact_number)).setText(contact.getPhoneNumber().toInternationalDisplayText());

        if (contact.isVerified()) {
            cnAddressLookupDelegate.fetchAddressFor(contact, this::onFetchContactComplete);
        } else {
            updateSharedMemosUI();
        }
    }

    private void hideSendToInput() {
        paymentReceiverView.setVisibility(View.GONE);
        paymentReceiverView.clear();
        getView().findViewById(R.id.contact_name).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.contact_number).setVisibility(View.VISIBLE);
    }

    private void showSendToInput() {
        paymentReceiverView.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.contact_name).setVisibility(View.GONE);
        getView().findViewById(R.id.contact_number).setVisibility(View.GONE);
    }

    private void onQrScanResult(int resultCode, Intent data) {
        if (resultCode == Intents.RESULT_SCAN_OK) {
            String cryptoUriString = data.getStringExtra(Intents.EXTRA_SCANNED_DATA);
            onCryptoStringReceived(cryptoUriString);
        }
    }

    private void onCryptoStringReceived(String cryptoUriString) {
        resetPaymentHolderIfNecessary();
        try {
            BitcoinUri bitcoinUri = bitcoinUtil.parse(cryptoUriString);
            processBitcoinUriIfNecessary(bitcoinUri);
        } catch (UriException e) {
            onInvalidBitcoinUri(e.getReason());
        }
    }

    private void processBitcoinUriIfNecessary(BitcoinUri bitcoinUri) {
        if (bitcoinUri == null) { return; }
        onReceiveBitcoinUri(bitcoinUri);
        checkForBip70Url(bitcoinUri);
    }

    private void resetPaymentHolderIfNecessary() {
        paymentHolder.setTransactionFee(null);
        memoToggleView.setText("");
        paymentHolder.setMemo(null);
    }

    private void checkForBip70Url(BitcoinUri bitcoinUri) {
        if (bitcoinUri == null) { return; }
        Uri bip70Uri = bitcoinUri.getBip70UrlIfApplicable();
        if (bip70Uri == null) { return; }

        progressSpinner = AlertDialogBuilder.buildIndefiniteProgress(getActivity());
        bip70Client.getMerchantInformation(bip70Uri, bip70Callback);
    }

    private void setBip70UriParameters(MerchantResponse merchantResponse) {
       if (merchantResponse.getMemo() != null) {
           memoToggleView.setText(merchantResponse.getMemo());
           paymentHolder.setMemo(merchantResponse.getMemo());
       }

       if (merchantResponse.getOutputs() != null && merchantResponse.getOutputs().size() > 0) {
           MerchantPaymentRequestOutput output = merchantResponse.getOutputs().get(0);

           onPaymentAddressChange(output.getAddress());
           setAmount(output.getAmount());
       }

       if (merchantResponse.getRequiredFeeRate() != 0L) {
           double roundedUpFeeRate = Math.ceil(merchantResponse.getRequiredFeeRate());
           TransactionFee transactionFee = new TransactionFee(roundedUpFeeRate, roundedUpFeeRate, roundedUpFeeRate);
           paymentHolder.setTransactionFee(transactionFee);
       }
    }

    private void setAmount(Long satoshiAmount) {
        if (satoshiAmount != null && satoshiAmount > 0) {
            onPaymentChange(new BTCCurrency(satoshiAmount));
        }
    }

    private void onReceiveBitcoinUri(BitcoinUri bitcoinUri) {
        if (bitcoinUri == null || bitcoinUri.getAddress() == null) { return; }
        onPaymentAddressChange(bitcoinUri.getAddress());
        setAmount(bitcoinUri.getSatoshiAmount());
    }

    private void onInvalidBitcoinUri(BitcoinUtil.ADDRESS_INVALID_REASON pasteError) {
        switch (pasteError) {
            case NULL_ADDRESS:
                showPasteAttemptFail(getString(R.string.invalid_bitcoin_address_error));
                break;
            case IS_BC1:
                showPasteAttemptFail(getString(R.string.bc1_error_message));
                break;
            case NOT_BASE58:
                showPasteAttemptFail(getString(R.string.invalid_btc_adddress__base58));
                break;
            case NOT_STANDARD_BTC_PATTERN:
                showPasteAttemptFail(getString(R.string.invalid_bitcoin_address_error));
                break;
        }
    }
}
