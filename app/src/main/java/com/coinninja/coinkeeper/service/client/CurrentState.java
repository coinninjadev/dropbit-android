package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.service.client.model.TransactionFee;

import app.dropbit.commons.currency.USDCurrency;

public class CurrentState {

    int blockheight;

    TransactionFee fees;

    Pricing pricing;

    public CurrentState() {
    }

    public CurrentState(int blockheight, TransactionFee fees, Pricing pricing) {
        this.blockheight = blockheight;
        this.fees = fees;
        this.pricing = pricing;
    }


    public int getBlockheight() {
        return blockheight;
    }

    public TransactionFee getFees() {
        return fees;
    }

    public Pricing getPricing() {
        return pricing;
    }

    public USDCurrency getLatestPrice() {
        return new USDCurrency(pricing.last);
    }

}
