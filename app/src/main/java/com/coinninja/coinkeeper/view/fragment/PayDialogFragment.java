package com.coinninja.coinkeeper.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate;
import com.coinninja.coinkeeper.di.interfaces.CountryCodeLocales;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks;
import com.coinninja.coinkeeper.service.callbacks.BasicCallbackHandler;
import com.coinninja.coinkeeper.service.callbacks.Bip70Callback;
import com.coinninja.coinkeeper.service.client.Bip70Client;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.MerchantPaymentRequestOutput;
import com.coinninja.coinkeeper.service.client.model.MerchantResponse;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.ui.account.verify.UserAccountVerificationActivity;
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment;
import com.coinninja.coinkeeper.ui.payment.PaymentInputView;
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.FeesManager;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.PickUserActivity;
import com.coinninja.coinkeeper.view.activity.QrScanActivity;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;
import com.coinninja.coinkeeper.view.subviews.SharedMemoToggleView;
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder;
import com.coinninja.coinkeeper.view.widget.PaymentReceiverView;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;

import javax.inject.Inject;

import static androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED;
import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;
import static com.coinninja.android.helpers.Views.shakeInError;
import static com.coinninja.android.helpers.Views.withId;

public class PayDialogFragment extends BaseBottomDialogFragment {
    public static final int PICK_CONTACT_REQUEST = 1001;

    @Inject
    FeesManager feesManager;
    @Inject
    CNAddressLookupDelegate cnAddressLookupDelegate;
    @Inject
    Analytics analytics;
    @Inject
    WalletHelper walletHelper;
    @Inject
    DropbitAccountHelper dropbitAccountHelper;
    @Inject
    BitcoinUtil bitcoinUtil;
    @Inject
    ClipboardUtil clipboardUtil;
    @Inject
    UserPreferences userPreferences;
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
    BitcoinUri bitcoinUri = null;

    public static PayDialogFragment newInstance(PaymentUtil paymentUtil, PaymentBarCallbacks paymentBarCallbacks, boolean shouldShowScanOnAttach) {
        PayDialogFragment payFragment = commonInit(paymentUtil, paymentBarCallbacks);
        payFragment.shouldShowScanOnAttach = shouldShowScanOnAttach;
        return payFragment;
    }

    public static PayDialogFragment newInstance(PaymentUtil paymentUtil, PaymentBarCallbacks paymentBarCallbacks, BitcoinUri bitcoinUri) {
        PayDialogFragment payFragment = commonInit(paymentUtil, paymentBarCallbacks);
        payFragment.bitcoinUri = bitcoinUri;
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
            onPickUserResult(data);
        } else if (requestCode == DropbitIntents.REQUEST_QR_FRAGMENT_SCAN && resultCode == RESULT_CANCELED) {
            onCloseClicked();
        } else if (requestCode == DropbitIntents.REQUEST_QR_FRAGMENT_SCAN) {
            onQrScanResult(resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        paymentInputView.requestFocus();
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.fragment_pay_dialog;
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
        cnAddressLookupDelegate.teardown();
    }

    @Override
    public void onStart() {
        super.onStart();
        memoToggleView.render((AppCompatActivity) getActivity(), getView());
        USDCurrency.setMaxLimit((USDCurrency) paymentHolder.getEvaluationCurrency());
        paymentHolder.setMaxLimitForFiat();
        setupView();
        setupBip70Callback();
        processBitcoinUriIfNecessary(bitcoinUri);
    }

    public void onPaymentAddressChange(String address) {
        paymentHolder.setPublicKey("");
        paymentUtil.setAddress(address);
        paymentReceiverView.setPaymentAddress(address);
        showSendToInput();
        updateSharedMemosUI();
        paymentInputView.requestFocus();
    }

    public void showPasteAttemptFail(String message) {
        paymentUtil.setAddress("");
        paymentHolder.setPaymentAddress("");
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public void onPhoneNumberValid(Phonenumber.PhoneNumber phoneNumber) {
        cnAddressLookupDelegate.fetchAddressFor(new PhoneNumber(phoneNumber), this::onFetchContactComplete);
        paymentUtil.setIdentity(new Identity(new Contact(new PhoneNumber(phoneNumber), "", false)));
        updateSharedMemosUI();
    }

    public void onPhoneNumberInvalid(String text) {
        paymentReceiverView.clear();
        paymentUtil.setIdentity(null);
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

    void startContactInviteFlow(Identity identity) {
        if (userPreferences.getShouldShowInviteHelp()) {
            showInviteHelpScreen(identity);
        } else {
            inviteUnverifiedIdentity(identity);
        }
    }

    void onInviteHelpAccepted(Identity identity) {
        DialogFragment fragment = (DialogFragment) getFragmentManager().findFragmentByTag(InviteHelpDialogFragment.TAG);
        if (fragment != null) {
            fragment.dismiss();
        }

        inviteUnverifiedIdentity(identity);
    }

    void inviteUnverifiedIdentity(Identity identity) {
        paymentBarCallbacks.confirmInvite(paymentUtil, identity);
    }

    void sendPaymentTo(Identity identity) {
        paymentBarCallbacks.confirmPaymentFor(paymentUtil, identity);
    }

    void sendPayment() {
        setMemoOnPayment();
        switch (paymentUtil.getPaymentMethod()) {
            case ADDRESS:
                paymentBarCallbacks.confirmPaymentFor(paymentUtil);
                break;
            case INVITE:
            case VERIFIED_CONTACT:
                sendPaymentToContact();
                break;
        }

    }

    void onPasteClicked() {
        String cryptoUriString = clipboardUtil.getRaw();
        if (cryptoUriString == null || cryptoUriString.isEmpty()) {
            String reason = getString(R.string.clipboard_empty_error_message);
            showPasteAttemptFail(reason);
        } else {
            onCryptoStringReceived(cryptoUriString);
        }
    }

    private void onPickUserResult(Intent data) {
        if (data.getExtras().getParcelable(DropbitIntents.EXTRA_IDENTITY) != null) {
            Identity identity = data.getExtras().getParcelable(DropbitIntents.EXTRA_IDENTITY);
            setIdentityResult(identity);
        }
    }

    private void showError(String message) {
        GenericAlertDialog.newInstance(message).show(getActivity().getSupportFragmentManager(), "INVALID_PAYMENT");
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
        paymentInputView.setOnSendMaxObserver(this::onSendMaxObserved);
        paymentInputView.setOnSendMaxClearedObserver(this::onSendMaxClearedObserved);
    }

    private void onSendMaxClearedObserved() {
        if (paymentUtil.isSendingMax()) {
            paymentUtil.clearFunding();
        }
    }

    private void onSendMaxObserved() {
        if (paymentUtil.fundMax()) {
            paymentHolder.updateValue(new BTCCurrency(paymentHolder.getTransactionData().getAmount()));
            paymentInputView.setPaymentHolder(paymentHolder);
        }
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
                if (progressSpinner == null) {
                    return;
                }
                progressSpinner.dismiss();
                progressSpinner = null;
            }
        };

        bip70Callback = new Bip70Callback(handler);
    }

    private void configureButtons(View base) {
        withId(base, R.id.pay_footer_send_btn).setOnClickListener(v -> onSendButtonClicked());
        withId(base, R.id.twitter_contacts_button).setOnClickListener(v -> onTwitterClicked());
        withId(base, R.id.pay_header_close_btn).setOnClickListener(v -> onCloseClicked());
        withId(base, R.id.paste_address_btn).setOnClickListener(v -> onPasteClicked());
        withId(base, R.id.contacts_btn).setOnClickListener(v -> onContactsClicked());
    }

    private void configureSharedMemo() {
        if (!dropbitAccountHelper.getHasVerifiedAccount()) {
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
        paymentReceiverView.setOnScanObserver(v -> startScan());
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

    private void onSendButtonClicked() {
        if (paymentUtil.isValid() && paymentUtil.checkFunding()) {
            sendPayment();
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
        if (dropbitAccountHelper.getHasVerifiedAccount()) {
            startPickContactActivity(DropbitIntents.ACTION_CONTACTS_SELECTION);
        } else {
            showNonVerifiedAccountAlert();
        }
    }

    private void onTwitterClicked() {
        if (dropbitAccountHelper.isTwitterVerified()) {
            startPickContactActivity(DropbitIntents.ACTION_TWITTER_SELECTION);
        } else {
            showNonTwitterVerifiedAccountAlert();
        }
    }

    private void showNonVerifiedAccountAlert() {
        AlertDialog.Builder alertDialog = AlertDialogBuilder.build(getView().getContext(), "You do not have a verified account, please verify.");
        setupActionsForVerificationAlertBuilder(alertDialog);
    }

    private void showNonTwitterVerifiedAccountAlert() {
        AlertDialog.Builder alertDialog = AlertDialogBuilder.build(getView().getContext(), "Your twitter account is not verified, please verify.");
        setupActionsForVerificationAlertBuilder(alertDialog);
    }

    private void setupActionsForVerificationAlertBuilder(AlertDialog.Builder builder) {
        builder.setNegativeButton("Not now", (dialog, which) -> {
        });
        builder.setPositiveButton("Verify", (dialog, which) -> showUserVerificationActivity()).show();
    }

    private void showUserVerificationActivity() {
        Intent verificationActivity = new Intent(getActivity(), UserAccountVerificationActivity.class);
        startActivity(verificationActivity);
    }

    private void startPickContactActivity(String action) {
        Intent contactIntent = new Intent(getActivity(), PickUserActivity.class);
        contactIntent.setAction(action);
        startActivityForResult(contactIntent, PICK_CONTACT_REQUEST);
    }

    private void startScan() {
        Intent qrScanIntent = new Intent(getActivity(), QrScanActivity.class);
        startActivityForResult(qrScanIntent, DropbitIntents.REQUEST_QR_FRAGMENT_SCAN);
    }

    private void setMemoOnPayment() {
        paymentHolder.setMemo(memoToggleView.getMemo());
        paymentHolder.setIsSharingMemo(memoToggleView.isSharing());
    }

    private void sendPaymentToContact() {
        if (dropbitAccountHelper.getHasVerifiedAccount()) {
            if (paymentHolder.hasPaymentAddress()) {
                sendPaymentTo(paymentUtil.getIdentity());
            } else if (paymentUtil.isVerifiedContact()) {
                inviteUnverifiedIdentity(paymentUtil.getIdentity());
            } else {
                startContactInviteFlow(paymentUtil.getIdentity());
            }
        } else {
            getActivity().startActivity(new Intent(getActivity(), VerificationActivity.class));
        }
    }

    private void showInviteHelpScreen(Identity identity) {
        DialogFragment dialogFragment = InviteHelpDialogFragment.newInstance(userPreferences,
                identity,
                () -> onInviteHelpAccepted(identity));

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

    private void setIdentityResult(Identity identity) {
        paymentUtil.setIdentity(identity);
        hideSendToInput();
        ((TextView) getView().findViewById(R.id.contact_name)).setText(identity.getDisplayName());
        ((TextView) getView().findViewById(R.id.contact_number)).setText(identity.getSecondaryDisplayName());
        cnAddressLookupDelegate.fetchAddressFor(identity, this::onFetchContactComplete);
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
        if (resultCode == DropbitIntents.RESULT_SCAN_OK) {
            String cryptoUriString = data.getStringExtra(DropbitIntents.EXTRA_SCANNED_DATA);
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
        if (bitcoinUri == null) {
            return;
        }
        onReceiveBitcoinUri(bitcoinUri);
        checkForBip70Url(bitcoinUri);
    }

    private void resetPaymentHolderIfNecessary() {
        paymentUtil.setFee(feesManager.currentFee());
        memoToggleView.setText("");
        paymentHolder.setMemo(null);
    }

    private void checkForBip70Url(BitcoinUri bitcoinUri) {
        if (bitcoinUri == null) {
            return;
        }
        Uri bip70Uri = bitcoinUri.getBip70UrlIfApplicable();
        if (bip70Uri == null) {
            return;
        }

        progressSpinner = AlertDialogBuilder.buildIndefiniteProgress((AppCompatActivity) getActivity());
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
            paymentUtil.setFee(roundedUpFeeRate);
        }
    }

    private void setAmount(Long satoshiAmount) {
        if (satoshiAmount != null && satoshiAmount > 0) {
            onPaymentChange(new BTCCurrency(satoshiAmount));
        }
    }

    private void onReceiveBitcoinUri(BitcoinUri bitcoinUri) {
        if (bitcoinUri == null || bitcoinUri.getAddress() == null) {
            return;
        }
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
