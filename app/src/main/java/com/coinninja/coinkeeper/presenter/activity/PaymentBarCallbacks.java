package com.coinninja.coinkeeper.presenter.activity;


import androidx.fragment.app.DialogFragment;

import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.PaymentHolder;

public interface PaymentBarCallbacks {

    void onQrScanPressed();

    void confirmPaymentFor(PaymentHolder paymentHolder);

    void confirmPaymentFor(PaymentHolder paymentHolder, Identity identity);

    void confirmInvite(Identity identity);

    void cancelPayment(DialogFragment dialogFragment);
}
