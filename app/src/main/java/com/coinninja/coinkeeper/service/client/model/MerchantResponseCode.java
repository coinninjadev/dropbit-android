package com.coinninja.coinkeeper.service.client.model;

public enum MerchantResponseCode {
    NO_ERROR,
    INVALID_CURRENCY,
    INVALID_NETWORK,
    EXPIRED,
    MISSING_VALUE,
    MISSING_OUTPUT;

    public String getMessage() {
        switch (this) {
            case INVALID_CURRENCY:
                return "DropBit only supports BTC transactions";
            case INVALID_NETWORK:
                return "The request is currently for a non-main network. It should be on the main network";
            case EXPIRED:
                return "The request has expired. Please refresh the request and scan it again";
            case MISSING_OUTPUT:
                return "The request did not include the receive address and/or amount";
            case MISSING_VALUE:
                return "The request was not successful. Check your request and try again";
            default:
                return "";
        }
    }
}