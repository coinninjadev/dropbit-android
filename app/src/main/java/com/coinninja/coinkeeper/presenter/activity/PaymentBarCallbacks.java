package com.coinninja.coinkeeper.presenter.activity;

import android.app.DialogFragment;

import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.MerchantResponse;

public interface PaymentBarCallbacks {

    void onQrScanPressed();

    void confirmPaymentFor(PaymentHolder paymentHolder);

    void confirmPaymentFor(PaymentHolder paymentHolder, Contact phoneNumber);

    void confirmInvite(Contact phoneNumber);

    void cancelPayment(DialogFragment dialogFragment);
}
