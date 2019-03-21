package com.coinninja.coinkeeper.util.uri.routes;

public enum CoinNinjaRoute {
    TRANSACTION,
    ADDRESS;

    CoinNinjaRoute() {}

    public String getPath() {
        switch(this) {
            case TRANSACTION:
                return "tx";
            case ADDRESS:
                return "address";
        }

        return "";
    }
}
