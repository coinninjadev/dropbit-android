package com.coinninja.coinkeeper.cn.service.exception.base;

import retrofit2.Response;

public class CNServiceException extends Exception {

    private final Response response;

    public CNServiceException(Response response, String message) {
        super(message);
        this.response = response;
    }


    public Response getResponse() {
        return response;
    }
}
