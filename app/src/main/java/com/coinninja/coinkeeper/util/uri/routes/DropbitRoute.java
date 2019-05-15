package com.coinninja.coinkeeper.util.uri.routes;

import java.util.ArrayList;
import java.util.List;

public enum DropbitRoute {
    DUST_PROTECTION,
    DROPBIT_TRANSACTION,
    REGULAR_TRANSACTION,
    SERVER_ADDRESSES,
    TRANSACTION_DETAILS,
    DROPBIT_ME_LEARN_MORE;

    private final String TOOLTIP_ROUTE = "tooltips";

    DropbitRoute() {
    }

    public List<String> getPath() {
        List<String> paths = new ArrayList<String>();
        paths.add(TOOLTIP_ROUTE);
        switch (this) {
            case DUST_PROTECTION:
                paths.add("dustprotection");
                break;
            case DROPBIT_TRANSACTION:
                paths.add("dropbittransaction");
                break;
            case TRANSACTION_DETAILS:
                paths.add("transactiondetails");
                break;
            case REGULAR_TRANSACTION:
                paths.add("regulartransaction");
                break;
            case SERVER_ADDRESSES:
                paths.add("myaddresses");
                break;
            case DROPBIT_ME_LEARN_MORE:
                paths.clear();
                paths.add("learnmore");
                break;
        }

        return paths;
    }
}
