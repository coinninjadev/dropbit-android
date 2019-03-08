package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;

public interface CoinNinjaServiceCheck {

    boolean isVerified() throws CNServiceException;

    void performDeverification();

    DeverifiedCause deverificaitonReason();

    String getRaw();

    enum DeverifiedCause {
        DROPPED, MISMATCH
    }
}
