package app.dropbit.twitter.client

import app.dropbit.twitter.util.Authorization
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class RequestInterceptor constructor(val authorization: Authorization) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        authorize(builder)
        return chain.proceed(builder.build())
    }

    private fun authorize(requestBuilder: Request.Builder) {
        authorization.addAuthorizationHeader(requestBuilder)
    }

}