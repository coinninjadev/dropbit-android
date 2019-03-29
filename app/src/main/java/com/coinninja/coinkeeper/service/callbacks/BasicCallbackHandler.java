package com.coinninja.coinkeeper.service.callbacks;

public interface BasicCallbackHandler<T> {
    void success(T object);
    void failure(String errorMessage);
}
