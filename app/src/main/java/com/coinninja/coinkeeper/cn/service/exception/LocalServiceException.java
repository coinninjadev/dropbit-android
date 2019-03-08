package com.coinninja.coinkeeper.cn.service.exception;

import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;

import retrofit2.Response;

public class LocalServiceException extends CNServiceException {
    public LocalServiceException(Response response, String message) {
        super(response, message);
    }
}
