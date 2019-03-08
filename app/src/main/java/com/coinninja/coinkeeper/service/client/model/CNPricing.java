package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class CNPricing {

    @SerializedName("time")
    String time;

    @SerializedName("average")
    BigDecimal average;

    public String getTime() {
        return time;
    }

    public long getAverage() {
        average = average.movePointRight(2);
        return average.longValue();
    }

    public void setAverage(BigDecimal average) {
        this.average = average;
    }
}
