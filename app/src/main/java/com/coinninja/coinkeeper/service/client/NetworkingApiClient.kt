package com.coinninja.coinkeeper.service.client

import app.dropbit.annotations.Mockable
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.Protocol
import okhttp3.Request
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

@Mockable
abstract class NetworkingApiClient {


    protected fun createQuery(property: String, data: Array<String>): JsonObject {
        val gson = Gson()
        val element = gson.toJsonTree(data)
        return createQuery(property, element)
    }

    protected fun createQuery(property: String, element: JsonElement): JsonObject {
        val json = JsonObject()
        json.add(property, element)

        val terms = JsonObject()
        terms.add("terms", json)

        val query = JsonObject()
        query.add("query", terms)
        return query
    }

    protected fun <T> executeCall(call: Call<T>): Response<T> {
        val response: Response<T>

        response = try {
            call.execute()
        } catch (e: IOException) {
            e.printStackTrace()
            createTeaPotErrorFor(call, e.message)
        }

        return response
    }

    protected fun <T> createTeaPotErrorFor(call: Call<T>, message: String?): Response<T> {
        return Response.error(okhttp3.ResponseBody.create(null, ""), okhttp3.Response.Builder()
                .code(ERROR_CODE)
                .message(message ?: "")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url(call.request().url()).build())
                .build())
    }

    companion object {
        const val ERROR_CODE = 418
    }
}
