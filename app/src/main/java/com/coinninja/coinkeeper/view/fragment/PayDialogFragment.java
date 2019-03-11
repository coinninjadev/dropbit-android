package com.coinninja.coinkeeper.view.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate;
import com.coinninja.coinkeeper.interactor.PreferenceInteractor;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.presenter.activity.CalculatorActivityPresenter;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.text.CurrencyFormattingTextWatcher;
import com.coinninja.coinkeeper.text.PhoneNumberFormattingTextWatcher;
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUriBuilder;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.PickContactActivity;
import com.coinninja.coinkeeper.view.activity.QrScanActivity;
import com.coinninja.coinkeeper.view.activity.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.view.subviews.SharedMemoToggleView;
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder;
import com.google.i18n.phonenumbers.Phonenumber;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import static android.app.Activity.RESULT_OK;

public class PayDialogFragment extends BaseDialogFragment implements PhoneNumberFormattingTextWatcher.Callback, CurrencyFormattingTextWatcher.Callback {
    public static final int PICK_CONTACT_REQUEST = 100001;

    @Inject
    CNAddressLookupDelegate cnAddressLookupDelegate;

    @Inject
    Analytics analytics;

    @Inject
    WalletHelper walletHelper;

    @Inject
    BitcoinUriBuilder bitcoinUriBuilder;

    @Inject
    ClipboardUtil clipboardUtil;

    @Inject
    PreferenceInteractor preferenceInteractor;

    @Inject
    CurrencyFormattingTextWatcher currencyFormattingTextWatcher;

    @Inject
    CoinKeeperApplication application;

    @Inject
    SharedMemoToggleView memoToggleView;

    CalculatorActivityPresenter.View calculatorView;
    PaymentUtil paymentUtil;
    PaymentHolder paymentHolder;

    private EditText sendToInput;
    private TextView secondaryCurrency;
    private EditText primaryCurrency;

    @Inject
    PhoneNumberUtil phoneNumberUtil;

    public static PayDialogFragment newInstance(PaymentUtil paymentUtil, CalculatorActivityPresenter.View view) {
        PayDialogFragment payFragment = new PayDialogFragment();
        payFragment.calculatorView = view;
        payFragment.paymentUtil = paymentUtil;
        payFragment.paymentHolder = paymentUtil.getPaymentHolder();
        return payFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Dialog);
    }


    @Override
    public void onResume() {
        super.onResume();
        currencyFormattingTextWatcher.setCurrency(paymentHolder.getPrimaryCurrency());
        currencyFormattingTextWatcher.setCallback(this);
        setupView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay_dialog, container, false);
        memoToggleView.render(getActivity(), view);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PayDialogFragment.PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            onPickContactResult(data);
        } else if (requestCode == Intents.REQUEST_QR_FRAGMENT_SCAN) {
            onQrScanResult(resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        analytics.trackEvent(Analytics.EVENT_PAY_SCREEN_LOADED);
        analytics.flush();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        memoToggleView.tearDown();
        paymentUtil.reset();
    }

    // Currency Text Watcher Callbacks
    @Override
    public void onValid(Currency currency) {
        paymentHolder.loadPaymentFrom(currency);
        updateSecondary();
    }

    // Currency Text Watcher Callbacks
    @Override
    public void onInvalid(String text) {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_view);
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        primaryCurrency.startAnimation(animation);
        primaryCurrency.postDelayed(() -> vibrator.cancel(), 250);
        long[] pattern = {25, 100, 25, 100};
        vibrator.vibrate(pattern, 0);
    }

    private void onPickContactResult(Intent data) {
        Contact contact = data.getExtras().getParcelable(Intents.EXTRA_CONTACT);
        setContactResult(contact);
    }

    private void setupView() {
        View base = getView();
        secondaryCurrency = base.findViewById(R.id.secondary_currency);
        primaryCurrency = base.findViewById(R.id.primary_currency);
        primaryCurrency.setRawInputType(Configuration.KEYBOARD_12KEY);
        primaryCurrency.addTextChangedListener(currencyFormattingTextWatcher);
        base.findViewById(R.id.pay_footer_send_btn).setOnClickListener(v -> onSendBtnClicked());
        base.findViewById(R.id.scan_btc_address_btn).setOnClickListener(v -> onScanClicked());
        base.findViewById(R.id.pay_header_close_btn).setOnClickListener(v -> onCloseClicked());
        base.findViewById(R.id.paste_address_btn).setOnClickListener(v -> onPasteClicked());
        base.findViewById(R.id.contacts_btn).setOnClickListener(v -> onContactsClicked());
        setupFragment();
        setupSendToInput();
        showPrice();

        USDCurrency.SET_MAX_LIMIT((USDCurrency) paymentHolder.getEvaluationCurrency());

        if (paymentHolder.getPrimaryCurrency().isZero()) {
            primaryCurrency.setText("");
        }
    }

    private void setupFragment() {
        if (!walletHelper.hasVerifiedAccount()) {
            paymentHolder.setIsSharingMemo(false);
        }
    }

    private void setupSendToInput() {
        String initialSendTo = paymentUtil.getAddress() == null ? "" : paymentUtil.getAddress();
        PhoneNumberFormattingTextWatcher phoneWatcher = new PhoneNumberFormattingTextWatcher(getResources().getConfiguration().locale, this);
        sendToInput = getView().findViewById(R.id.send_to_input);
        sendToInput.setText(initialSendTo);
        sendToInput.setOnFocusChangeListener(this::onSendToFocusChanged);
        sendToInput.addTextChangedListener(phoneWatcher);
    }

    private void onPaymentChange(Currency currency) {
        paymentUtil.getPaymentHolder().loadPaymentFrom(currency);
        currencyFormattingTextWatcher.setCurrency(currency);
        showPrice();
    }

    public void onPaymentAddressChange(String text) {
        paymentHolder.clearPayment();
        secondaryCurrency.clearFocus();
        paymentUtil.setAddress(text);
        sendToInput.setText(text);
        showSendToInput();
        primaryCurrency.requestFocus();
        showKeyboard(primaryCurrency);
        updateSharedMemosUI();
    }

    private void updateSharedMemosUI() {
        if (paymentHolder.hasPubKey() || paymentUtil.getPaymentMethod() == PaymentUtil.PaymentMethod.INVITE) {
            memoToggleView.showSharedMemoViews();
        } else {
            memoToggleView.hideSharedMemoViews();
        }
    }

    public void showPasteAttemptFail(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPhoneNumberValid(Phonenumber.PhoneNumber phoneNumber) {
        forceDropKeyboard(sendToInput);
        cnAddressLookupDelegate.fetchAddressFor(new PhoneNumber(phoneNumber), this::onFetchContactComplete);
        paymentUtil.setContact(new Contact(new PhoneNumber(phoneNumber), "", false));

        updateSharedMemosUI();
    }

    @Override
    public void onPhoneNumberInValid(String text) {
        paymentUtil.setContact(null);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        cnAddressLookupDelegate.teardown();
        super.onDismiss(dialog);
    }

    private void onSendToFocusChanged(View view, boolean b) {
        if (view.isFocused()) {
            ((EditText) view).setText("");
            paymentUtil.setAddress("");
        }
    }

    private void onSendBtnClicked() {
        if (paymentUtil.isValid()) {
            paymentUtil.checkFunding(this::onComplete);
        } else {
            invalidPayment();
        }
    }

    @Deprecated
    public void setCalculatorView(CalculatorActivityPresenter.View calculatorView) {
        this.calculatorView = calculatorView;
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
        Intent qrScanIntent = new Intent(getActivity(), QrScanActivity.class);
        startActivityForResult(qrScanIntent, Intents.REQUEST_QR_FRAGMENT_SCAN);
    }

    private void updateSecondary() {
        secondaryCurrency.setText(paymentHolder.getSecondaryCurrency().toFormattedCurrency());
    }

    private void showPrice() {
        PaymentHolder paymentHolder = paymentUtil.getPaymentHolder();
        primaryCurrency.setText(paymentHolder.getPrimaryCurrency().toFormattedCurrency());
        secondaryCurrency.setText(paymentHolder.getSecondaryCurrency().toFormattedCurrency());
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


    public void onFetchContactComplete(AddressLookupResult addressLookupResult) {
        paymentHolder.setPublicKey(addressLookupResult.getAddressPubKey());
        paymentHolder.setPaymentAddress(addressLookupResult.getAddress());
        updateSharedMemosUI();
    }

    void inviteContact(Contact pickedContact) {
        setMemoOnPayment();
        calculatorView.confirmInvite(pickedContact);
        dismiss();
    }

    void sendPaymentTo() {
        setMemoOnPayment();
        calculatorView.confirmPaymentFor(paymentUtil.getAddress());
        dismiss();
    }


    void sendPaymentTo(String address, Contact phoneNumber) {
        setMemoOnPayment();
        calculatorView.confirmPaymentFor(address, phoneNumber);
        dismiss();
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

    protected void showError(String message) {
        AlertDialogBuilder.build(getActivity(), message)
                .show();
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
        calculatorView.cancelPayment(this);
    }

    private void setContactResult(Contact contact) {
        paymentUtil.setContact(contact);
        hideSendToInput();
        ((TextView) getView().findViewById(R.id.contact_name)).setText(contact.getDisplayName());
        ((TextView) getView().findViewById(R.id.contact_number)).setText(contact.getPhoneNumber().toNationalDisplayText());

        if (contact.isVerified()) {
            cnAddressLookupDelegate.fetchAddressFor(contact, this::onFetchContactComplete);
        } else {
            updateSharedMemosUI();
        }
    }

    private void hideSendToInput() {
        sendToInput.setVisibility(View.GONE);
        getView().findViewById(R.id.contact_name).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.contact_number).setVisibility(View.VISIBLE);

    }

    private void showSendToInput() {
        sendToInput.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.contact_name).setVisibility(View.GONE);
        getView().findViewById(R.id.contact_number).setVisibility(View.GONE);
    }

    void onComplete(FundingUTXOs fundingUTXOs) {
        if (paymentUtil.isFunded()) {
            sendPayment();
        } else {
            invalidPayment();
        }
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

    private void onQrScanResult(int resultCode, Intent data) {
        if (resultCode == Intents.RESULT_SCAN_OK) {
            String cryptoUriString = data.getStringExtra(Intents.EXTRA_SCANNED_DATA);
            onCryptoStringReceived(cryptoUriString);
        }
    }

    private void onCryptoStringReceived(String cryptoUriString) {
        try {
            BitcoinUri bitcoinUri = bitcoinUriBuilder.parse(cryptoUriString);
            onReceiveBitcoinUri(bitcoinUri);
        } catch (UriException e) {
            onInvalidBitcoinUri(e.getReason());
        }
    }

    private void onReceiveBitcoinUri(BitcoinUri bitcoinUri) {
        Long satoshiAmount = bitcoinUri.getSatoshiAmount();
        if (satoshiAmount != null && satoshiAmount > 0) {
            onPaymentChange(new BTCCurrency(satoshiAmount));
        }
        onPaymentAddressChange(bitcoinUri.getAddress());
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
