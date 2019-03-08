package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;
import com.coinninja.coinkeeper.util.CNLogger;

import java.io.IOException;

import retrofit2.Response;

abstract class AbstractAuthorizationServiceCheck {

    abstract CNLogger getLogger();

    abstract String getTag();

    abstract void setRaw(String string);


    protected boolean handleResponse(Response response) throws CNServiceException {
        boolean isVerified = false;
        switch (response.code()) {
            case 200:
                isVerified = true;
                break;
            case 401:
                isVerified = false;
                setRawBody(response);
                break;
            default:
                handleUnexpectedResponse(response);
        }
        return isVerified;
    }

    private void setRawBody(Response response) {
        try {
            setRaw(response.errorBody().string());
        } catch (IOException e) {
            e.printStackTrace();
            setRaw("");
        }
    }

    void handleUnexpectedResponse(Response response) throws CNServiceException {
        String message = "|---- Verification Result Was Not Expected";
        getLogger().logError(getTag(), message, response);
        throw new CNServiceException(response, message);
    }
}
