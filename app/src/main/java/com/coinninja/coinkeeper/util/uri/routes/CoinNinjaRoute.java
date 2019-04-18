package com.coinninja.coinkeeper.util.uri.routes;

public enum CoinNinjaRoute {
    NEWS,
    TRANSACTION,
    BUY_BITCOIN,
    ADDRESS;

    CoinNinjaRoute() {}

    public String getPath() {
        switch(this) {
            case NEWS:
                return "news";
            case BUY_BITCOIN:
                return "buybitcoin";
            case TRANSACTION:
                return "tx";
            case ADDRESS:
                return "address";
        }

        return "";
    }
}
