package com.coinninja.coinkeeper.service.client.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MerchantPaymentRequestOutput implements Parcelable {
    private Long amount = null;
    private String address = "";

    protected MerchantPaymentRequestOutput(Parcel in) {
        if (in.readByte() == 0) {
            amount = null;
        } else {
            amount = in.readLong();
        }
        address = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (amount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(amount);
        }
        dest.writeString(address);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MerchantPaymentRequestOutput> CREATOR = new Creator<MerchantPaymentRequestOutput>() {
        @Override
        public MerchantPaymentRequestOutput createFromParcel(Parcel in) {
            return new MerchantPaymentRequestOutput(in);
        }

        @Override
        public MerchantPaymentRequestOutput[] newArray(int size) {
            return new MerchantPaymentRequestOutput[size];
        }
    };

    public Long getAmount() {
        return amount;
    }

    public String getAddress() {
        return address;
    }
}
