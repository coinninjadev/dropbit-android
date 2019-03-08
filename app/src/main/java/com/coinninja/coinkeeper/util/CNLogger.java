package com.coinninja.coinkeeper.util;

import android.util.Log;

import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Response;

@CoinkeeperApplicationScope
public class CNLogger {

    @Inject
    public CNLogger() {
    }

    public void debug(String tag, String message) {
        Log.d(tag, message);
    }

    public void logError(String tag, String message, Response response) {
        if (null != message) debug(tag, message);

        debug(tag, "|------ statusCode: " + String.valueOf(response.code()));
        try {
            ResponseBody responseBody = response.errorBody();
            if (null != responseBody)
                debug(tag, "|--------- response body: " + responseBody.string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
