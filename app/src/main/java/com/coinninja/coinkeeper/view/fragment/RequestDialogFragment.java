package com.coinninja.coinkeeper.view.fragment;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.service.QRCodeService;
import com.coinninja.coinkeeper.ui.base.BaseDialogFragment;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUriBuilder;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class RequestDialogFragment extends BaseDialogFragment {
    private static final String TAG = RequestDialogFragment.class.getSimpleName();

    @Inject
    AccountManager accountManager;

    @Inject
    BitcoinUriBuilder bitcoinUriBuilder;

    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    @Inject
    ClipboardUtil clipboardUtil;

    BitcoinUri address;
    private Uri qrImageURI;
    private String receiveAddress;
    private PaymentHolder paymentHolder;

    BroadcastReceiver qrBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String image = intent.getStringExtra(Intents.EXTRA_QR_CODE_LOCATION);
            qrImageURI = Uri.parse(image);
            drawQR(qrImageURI);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Dialog);
    }

    @Override
    public void onAttach(Context context) {
        AndroidInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_dialog, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupShareUri();
        setupCloseButton();
        setupRequestButton();
        setupCopyButton();
        startQRImageService();
        setupPrice();
        localBroadCastUtil.registerReceiver(qrBroadcastReceiver, new IntentFilter(Intents.ACTION_VIEW_QR_CODE));
    }

    @Override
    public void onPause() {
        super.onPause();
        localBroadCastUtil.unregisterReceiver(qrBroadcastReceiver);
    }

    public void startQRImageService() {
        Intent intent = new Intent(getActivity(), QRCodeService.class);
        intent.putExtra(Intents.EXTRA_TEMP_QR_SCAN, address.toString());
        getActivity().startService(intent);
    }

    protected void drawQR(Uri qrImageURI) {
        this.qrImageURI = qrImageURI;
        ImageView qrImage = getView().findViewById(R.id.request_body_qr_image);
        qrImage.setImageURI(qrImageURI);
        qrImage.setVisibility(View.VISIBLE);
    }

    private void removePriceDisplay() {
        getView().findViewById(R.id.primary_currency).setVisibility(View.GONE);
        getView().findViewById(R.id.secondary_currency).setVisibility(View.GONE);
    }

    private void showPrice() {
        TextView primaryCurrency = getView().findViewById(R.id.primary_currency);
        primaryCurrency.setVisibility(View.VISIBLE);
        primaryCurrency.setText(paymentHolder.getPrimaryCurrency().toFormattedCurrency());

        TextView secondaryCurrency = getView().findViewById(R.id.secondary_currency);
        secondaryCurrency.setVisibility(View.VISIBLE);
        secondaryCurrency.setText(paymentHolder.getSecondaryCurrency().toFormattedCurrency());
    }

    private void setupPrice() {
        if (paymentHolder.getPrimaryCurrency().isZero()) {
            removePriceDisplay();
        } else {
            showPrice();
        }
    }

    private void setupShareUri() {
        receiveAddress = accountManager.getNextReceiveAddress();
        bitcoinUriBuilder.setAddress(receiveAddress);

        Currency currency = paymentHolder.getCryptoCurrency();
        if (!currency.isZero())
            bitcoinUriBuilder.setAmount((BTCCurrency) currency);

        address = bitcoinUriBuilder.build();
    }

    private void setupCloseButton() {
        getView().findViewById(R.id.close_btn).setOnClickListener(V -> dismiss());
        getView().findViewById(R.id.close).setOnClickListener(V -> dismiss());
    }

    private void setupCopyButton() {
        Button copyButton = getView().findViewById(R.id.request_copy_button);
        copyButton.setOnClickListener(V -> onCopy());
        copyButton.setTransformationMethod(null);
        copyButton.setText(receiveAddress);
    }

    private void setupRequestButton() {
        getView().findViewById(R.id.request_funds).setOnClickListener(V -> onRequestFunds());
    }

    private void onRequestFunds() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, address.toString());
        intent.putExtra(Intent.EXTRA_SUBJECT, "Request Bitcoin");
        intent.putExtra("subject", "Request Bitcoin");//support older sms clients
        intent.putExtra("sms_body", address.toString());//support older sms clients
        intent.setType(Intent.normalizeMimeType("image/*"));

        try {
            intent.putExtra(Intent.EXTRA_STREAM, qrImageURI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        } catch (Exception e) {
            Log.i(TAG, "unable to share qr image");
        }
        startActivity(Intent.createChooser(intent, "Request Bitcoin"));
    }

    private void onCopy() {
        clipboardUtil.setClipFromText("Bitcoin Address", address.toString());
        Toast.makeText(getActivity(),
                getActivity().getString(R.string.request_copied_message),
                Toast.LENGTH_SHORT)
                .show();
    }

    public PaymentHolder getPaymentHolder() {
        return paymentHolder;
    }

    public void setPaymentHolder(PaymentHolder paymentHolder) {
        this.paymentHolder = paymentHolder;
    }
}
