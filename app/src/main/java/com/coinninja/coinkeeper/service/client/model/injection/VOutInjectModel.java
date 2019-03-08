package com.coinninja.coinkeeper.service.client.model.injection;

import com.coinninja.coinkeeper.service.client.model.ScriptPubKey;
import com.coinninja.coinkeeper.service.client.model.VOut;

public class VOutInjectModel extends VOut {
    private long value;
    private int index;
    private ScriptPubKeyInjectModel scriptPubKey;

    public void setValue(long value) {
        this.value = value;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setScriptPubKey(ScriptPubKeyInjectModel scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }


    @Override
    public long getValue() {
        return value;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public ScriptPubKey getScriptPubKey() {
        return scriptPubKey;
    }

}
