package com.coinninja.coinkeeper.interfaces;


public interface Authentication {
    boolean isAuthenticated();

    void setAuthenticated();

    boolean hasOptedIntoFingerprintAuth();

    void forceDeAuthenticate();
}
