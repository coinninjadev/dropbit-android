package com.coinninja.coinkeeper.service.client.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class TransactionFee implements Parcelable {
    @SerializedName("min")
    private double min;

    @SerializedName("avg")
    private double avg;

    @SerializedName("max")
    private double max;

    public TransactionFee(double min, double avg, double max) {
        this.min = min;
        this.avg = avg;
        this.max = max;
    }

    public TransactionFee(Parcel parcel) {
        min = parcel.readDouble();
        avg = parcel.readDouble();
        max = parcel.readDouble();
    }

    public double getMin() {
        return min;
    }

    public double getAvg() {
        return avg;
    }

    public double getMax() {
        return max;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(min);
        dest.writeDouble(avg);
        dest.writeDouble(max);
    }


    public static final Creator<TransactionFee> CREATOR = new Creator<TransactionFee>() {
        @Override
        public TransactionFee createFromParcel(Parcel in) {
            return new TransactionFee(in);
        }

        @Override
        public TransactionFee[] newArray(int size) {
            return new TransactionFee[size];
        }
    };
}
