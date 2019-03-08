package com.coinninja.coinkeeper.service.tasks;

import android.os.AsyncTask;

import com.coinninja.coinkeeper.model.FundedCallback;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper;

import androidx.annotation.NonNull;

public class CreateFundingUTXOsTask extends AsyncTask<Void, Void, FundingUTXOs> {


    private final FundingUTXOs.Builder fundingUTXTbuilder;
    private final TargetStatHelper targetStatHelper;
    private final PaymentHolder paymentHolder;
    private final FundedCallback callback;

    public static CreateFundingUTXOsTask newInstance(@NonNull FundingUTXOs.Builder fundingUTXOsBuilder, @NonNull TargetStatHelper targetStatHelper,
                                                     @NonNull PaymentHolder paymentHolder, @NonNull FundedCallback callback) {

        return new CreateFundingUTXOsTask(fundingUTXOsBuilder, targetStatHelper, paymentHolder, callback);
    }

    public CreateFundingUTXOsTask(@NonNull FundingUTXOs.Builder fundingUTXTbuilder, @NonNull TargetStatHelper targetStatHelper, @NonNull PaymentHolder paymentHolder, @NonNull FundedCallback callback) {
        this.fundingUTXTbuilder = fundingUTXTbuilder;
        this.targetStatHelper = targetStatHelper;
        this.paymentHolder = paymentHolder;
        this.callback = callback;
    }

    @Override
    protected FundingUTXOs doInBackground(Void... voids) {
        fundingUTXTbuilder.setUsableTargets(targetStatHelper.getSpendableTargets());
        fundingUTXTbuilder.setTransactionFee(paymentHolder.getTransactionFee());
        fundingUTXTbuilder.setSatoshisSpending(paymentHolder.getBtcCurrency().toSatoshis());
        return fundingUTXTbuilder.build();
    }

    @Override
    protected void onPostExecute(FundingUTXOs fundingUTXOs) {
        callback.onComplete(fundingUTXOs);
    }
}