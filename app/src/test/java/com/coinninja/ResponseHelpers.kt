package com.coinninja

import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response


fun <T> failedHttpResponse(): Response<T> {
    return Response.error(500, ResponseBody.create(MediaType.parse("plain/text"), ""))
}