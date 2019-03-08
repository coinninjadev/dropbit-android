package com.coinninja.coinkeeper.cn.service.exception;

import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;

import retrofit2.Response;

public class RemoteServiceException extends CNServiceException {
    public RemoteServiceException(Response response, String message) {
        super(response, message);
    }
}
