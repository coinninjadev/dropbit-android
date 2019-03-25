package com.coinninja.coinkeeper.util.uri.routes;

import java.util.ArrayList;
import java.util.List;

public enum DropbitRoute {
    DROPBIT_TRANSACTION,
    REGULAR_TRANSACTION,
    TRANSACTION_DETAILS;

    private final String TOOLTIP_ROUTE = "tooltips";

    DropbitRoute() {}

    public List<String> getPath() {
        List<String> paths = new ArrayList<String>();
        paths.add(TOOLTIP_ROUTE);
        switch(this) {
            case DROPBIT_TRANSACTION:
                paths.add("dropbittransaction");
                break;
            case TRANSACTION_DETAILS:
                paths.add("transactiondetails");
                break;
            case REGULAR_TRANSACTION:
                paths.add("regulartransaction");
                break;
        }

        return paths;
    }
}
