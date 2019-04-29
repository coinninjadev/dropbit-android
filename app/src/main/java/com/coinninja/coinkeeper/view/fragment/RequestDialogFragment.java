package com.coinninja.coinkeeper.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.service.QRCodeService;
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.uri.BitcoinUriBuilder;

import javax.inject.Inject;

import static com.coinninja.coinkeeper.util.uri.routes.BitcoinRoute.DEFAULT;

public class RequestDialogFragment extends BaseBottomDialogFragment {
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
    IntentFilter intentFilter = new IntentFilter(DropbitIntents.ACTION_VIEW_QR_CODE);
    private Uri qrImageURI;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String image = intent.getStringExtra(DropbitIntents.EXTRA_QR_CODE_LOCATION);
            qrImageURI = Uri.parse(image);
            drawQR(qrImageURI);
        }
    };
    private String receiveAddress;

    @Override
    public void onPause() {
        super.onPause();
        localBroadCastUtil.unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupShareUri();
        setupCloseButton();
        setupRequestButton();
        setupCopyButton();
        startQRImageService();
        localBroadCastUtil.registerReceiver(receiver, intentFilter);
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.fragment_request_dialog;
    }

    private void drawQR(Uri qrImageURI) {
        this.qrImageURI = qrImageURI;
        ImageView qrImage = getView().findViewById(R.id.request_body_qr_image);
        qrImage.setImageURI(qrImageURI);
        qrImage.setVisibility(View.VISIBLE);
    }

    private void startQRImageService() {
        Intent intent = new Intent(getActivity(), QRCodeService.class);
        intent.putExtra(DropbitIntents.EXTRA_TEMP_QR_SCAN, address.toString());
        getActivity().startService(intent);
    }

    private void setupShareUri() {
        receiveAddress = accountManager.getNextReceiveAddress();
        address = bitcoinUriBuilder.build(DEFAULT.setAddress(receiveAddress));
    }

    private void setupCloseButton() {
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
        intent.putExtra("subject", "Request Bitcoin");
        intent.putExtra("sms_body", address.toString());
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
}
