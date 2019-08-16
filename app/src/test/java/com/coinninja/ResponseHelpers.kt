package com.coinninja

import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response


fun failedHttpResponse(content:String=""):Response<Any> {
    return Response.error(500, ResponseBody.create(MediaType.parse("plain/text"), content))
}
