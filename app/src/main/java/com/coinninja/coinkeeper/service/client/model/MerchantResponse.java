package com.coinninja.coinkeeper.service.client.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class MerchantResponse implements Parcelable {
    public static final Creator<MerchantResponse> CREATOR = new Creator<MerchantResponse>() {
        @Override
        public MerchantResponse createFromParcel(Parcel in) {
            return new MerchantResponse(in);
        }

        @Override
        public MerchantResponse[] newArray(int size) {
            return new MerchantResponse[size];
        }
    };
    String network = "";
    String currency = "";
    double requiredFeeRate = 0L;
    List<MerchantPaymentRequestOutput> outputs = new ArrayList<>();
    Date time = new Date();
    Date expires = new Date();
    String memo = "";
    String paymentUrl = "";
    String paymentId = "";

    protected MerchantResponse(Parcel in) {
        network = in.readString();
        currency = in.readString();
        requiredFeeRate = in.readDouble();
        outputs = in.createTypedArrayList(MerchantPaymentRequestOutput.CREATOR);
        memo = in.readString();
        paymentUrl = in.readString();
        paymentId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(network);
        dest.writeString(currency);
        dest.writeDouble(requiredFeeRate);
        dest.writeTypedList(outputs);
        dest.writeString(memo);
        dest.writeString(paymentUrl);
        dest.writeString(paymentId);
    }

    public String getNetwork() {
        return network;
    }

    public String getCurrency() {
        return currency;
    }

    public double getRequiredFeeRate() {
        return requiredFeeRate;
    }

    public List<MerchantPaymentRequestOutput> getOutputs() {
        return outputs;
    }

    @Nullable
    public String getPaymentAddress() {
        if (getOutputs().size() > 0)
            return outputs.get(0).getAddress();
        return null;
    }

    public long getPaymentAmount() {
        if (getOutputs().size() > 0)
            return outputs.get(0).getAmount();
        return 0;
    }

    public double getRequiredFee() {
        if (requiredFeeRate != 0.0) {
            return Math.ceil(requiredFeeRate);
        }
        return requiredFeeRate;
    }

    public Date getTime() {
        return time;
    }

    public Date getExpires() {
        return expires;
    }

    public String getMemo() {
        return memo;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public String getPaymentId() {
        return paymentId;
    }
}

