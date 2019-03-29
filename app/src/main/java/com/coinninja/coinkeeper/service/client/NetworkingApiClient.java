package com.coinninja.coinkeeper.service.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Protocol;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;

public abstract class NetworkingApiClient {
    public static final int ERROR_CODE = 418;

    protected Response executeCall(Call call) {
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
            response = createTeaPotErrorFor(call, e.getMessage());
        }

        return response;
    }

    public Response createTeaPotErrorFor(Call call, String message) {
        return Response.error(okhttp3.ResponseBody.create(null, ""), new okhttp3.Response.Builder()
                .code(NetworkingApiClient.ERROR_CODE)
                .message(message)
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url(call.request().url()).build())
                .build());

    }

    @NonNull
    protected JsonObject createQuery(String property, String[] data) {
        Gson gson = new Gson();
        JsonElement element = gson.toJsonTree(data);
        return createQuery(property, element);
    }

    @NonNull
    protected JsonObject createQuery(String property, JsonElement element) {
        JsonObject json = new JsonObject();
        json.add(property, element);

        JsonObject terms = new JsonObject();
        terms.add("terms", json);

        JsonObject query = new JsonObject();
        query.add("query", terms);
        return query;
    }
}
