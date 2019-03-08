package com.coinninja.coinkeeper.presenter.activity;

import android.app.DialogFragment;

import com.coinninja.coinkeeper.service.client.model.Contact;

public class CalculatorActivityPresenter {

    public interface View {

        void onQrScanPressed();

        void confirmPaymentFor(String btcAddress);

        void confirmPaymentFor(String btcAddress, Contact phoneNumber);

        void confirmInvite(Contact phoneNumber);

        void cancelPayment(DialogFragment dialogFragment);
    }
}
