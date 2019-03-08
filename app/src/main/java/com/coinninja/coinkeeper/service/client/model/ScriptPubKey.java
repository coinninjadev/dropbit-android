package com.coinninja.coinkeeper.service.client.model;

public class ScriptPubKey {
    String asm;
    String hex;
    int reqSigs;
    String type;
    String[] addresses;

    public String getAsm() {
        return asm;
    }

    public String getHex() {
        return hex;
    }

    public int getReqSigs() {
        return reqSigs;
    }

    public String getType() {
        return type;
    }

    public String[] getAddresses() {
        return addresses;
    }
}
