package com.coinninja.coinkeeper.model;

public interface FundedCallback {
    void onComplete(FundingUTXOs fundingUTXOs);
}
