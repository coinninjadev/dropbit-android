package com.coinninja.coinkeeper.service.client.model.injection;

import com.coinninja.coinkeeper.service.client.model.VIn;
import com.coinninja.coinkeeper.service.client.model.VOut;

public class VInInjectModel extends VIn {

    private String transactionId;//YESS
    private VOutInjectModel previousOutput;

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }


    public void setPreviousOutput(VOutInjectModel previousOutput) {
        this.previousOutput = previousOutput;
    }


    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public VOut getPreviousOutput() {
        return previousOutput;
    }
}
