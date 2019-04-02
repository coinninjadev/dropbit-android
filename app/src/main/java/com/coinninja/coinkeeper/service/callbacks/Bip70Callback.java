package com.coinninja.coinkeeper.service.callbacks;

import com.coinninja.coinkeeper.service.client.model.MerchantResponse;
import com.coinninja.coinkeeper.service.client.model.MerchantResponseCode;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Bip70Callback implements Callback<MerchantResponse> {

    private BasicCallbackHandler<MerchantResponse> callbackHandler;

    public Bip70Callback(BasicCallbackHandler<MerchantResponse> callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    @Override
    public void onResponse(Call<MerchantResponse> call, Response<MerchantResponse> response) {
        MerchantResponseCode code = isResponseSupported(response.body());

        switch (code) {
            case NO_ERROR:
                callbackHandler.success(response.body());
                break;
            default:
                callbackHandler.failure(code.getMessage());
                break;
        }
    }

    @Override
    public void onFailure(Call<MerchantResponse> call, Throwable t) {
        callbackHandler.failure(t.getMessage());
    }

    private MerchantResponseCode isResponseSupported(MerchantResponse response) {
        if (response == null || response.getCurrency() == null || response.getNetwork() == null || response.getOutputs() == null || response.getExpires() == null) {
            return MerchantResponseCode.MISSING_VALUE;
        }

        if(!response.getCurrency().equals("BTC")) {
            return MerchantResponseCode.INVALID_CURRENCY;
        } else if (!response.getNetwork().equals("main")) {
            return MerchantResponseCode.INVALID_NETWORK;
        } else if (response.getOutputs().size() == 0 || response.getOutputs().get(0).getAddress() == null ||  response.getOutputs().get(0).getAmount() == 0L) {
            return MerchantResponseCode.MISSING_OUTPUT;
        } else if (response.getExpires().before(Calendar.getInstance().getTime())) {
            return MerchantResponseCode.EXPIRED;
        } else {
            return MerchantResponseCode.NO_ERROR;
        }
    }
}
