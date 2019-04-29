package com.coinninja.coinkeeper.presenter.activity;


import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.service.client.model.Contact;

import androidx.fragment.app.DialogFragment;

public interface PaymentBarCallbacks {

    void onQrScanPressed();

    void confirmPaymentFor(PaymentHolder paymentHolder);

    void confirmPaymentFor(PaymentHolder paymentHolder, Contact phoneNumber);

    void confirmInvite(Contact phoneNumber);

    void cancelPayment(DialogFragment dialogFragment);
}
