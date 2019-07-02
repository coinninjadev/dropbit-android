package com.coinninja.coinkeeper.presenter.activity;


import androidx.fragment.app.DialogFragment;

import com.coinninja.coinkeeper.model.Contact;
import com.coinninja.coinkeeper.model.Identity;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.util.PaymentUtil;

public interface PaymentBarCallbacks {

    void onQrScanPressed();

    void confirmPaymentFor(PaymentUtil paymentUtil);

    void confirmPaymentFor(PaymentUtil paymentUtil, Identity identity);

    void confirmInvite(PaymentUtil paymentUtil, Identity identity);

    void cancelPayment(DialogFragment dialogFragment);
}
