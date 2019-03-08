package com.coinninja.coinkeeper.service.client.model;

import com.google.gson.annotations.SerializedName;

public class VOut {
    private long value;
    @SerializedName("n")
    private int index;

    private ScriptPubKey scriptPubKey;

    public long getValue() {
        return value;
    }

    public int getIndex() {
        return index;
    }

    public ScriptPubKey getScriptPubKey() {
        return scriptPubKey;
    }

}
