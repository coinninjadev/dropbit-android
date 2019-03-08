package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

public class VIn {

    @SerializedName("txid")
    private String transactionId;
    private int vout;
    private ScriptSig scriptSig;
    @SerializedName("txinwitness")
    private String[] transactionWitnesses;
    private long sequence;
    @SerializedName("previousoutput")
    private VOut previousOutput;

    public String getTransactionId() {
        return transactionId;
    }

    public String[] getTransactionWitnesses() {
        return transactionWitnesses;
    }

    public int getVOut() {
        return vout;
    }

    public ScriptSig getScriptSig() {
        return scriptSig;
    }

    public long getSequence() {
        return sequence;
    }

    public VOut getPreviousOutput() {
        return previousOutput;
    }

}
