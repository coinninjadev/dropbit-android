package com.coinninja.coinkeeper.service.client.model.injection;

import com.coinninja.coinkeeper.service.client.model.ScriptPubKey;

public class ScriptPubKeyInjectModel extends ScriptPubKey {
    private String[] addresses;

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }


    @Override
    public String[] getAddresses() {
        return addresses;
    }
}
