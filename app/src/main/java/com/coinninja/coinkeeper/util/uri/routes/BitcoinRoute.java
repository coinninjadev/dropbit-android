package com.coinninja.coinkeeper.util.uri.routes;

public enum BitcoinRoute {
    BIP70(""),
    DEFAULT("");

    private String address;

    BitcoinRoute(String address) {
        this.address = address;
    }

    public BitcoinRoute setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getAddress() {
        return address;
    }
}
