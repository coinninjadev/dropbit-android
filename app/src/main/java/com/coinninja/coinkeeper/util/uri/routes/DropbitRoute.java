package com.coinninja.coinkeeper.util.uri.routes;

public enum DropbitRoute {
    DROPBIT_TRANSACTION,
    REGULAR_TRANSACTION,
    TRANSACTION_DETAILS;

    DropbitRoute() {}

    public String getRoute() {
        switch(this) {
            case DROPBIT_TRANSACTION:
                return "tooltip/dropbittransaction";
            case TRANSACTION_DETAILS:
                return "tooltip/transactiondetails";
            case REGULAR_TRANSACTION:
                return "tooltip/regulartransaction";
        }
    }
}
