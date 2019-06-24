package app.dropbit.twitter.util

import android.net.Uri
import android.util.Base64
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.DateUtil
import app.dropbit.commons.util.HmacSHA1Signer
import app.dropbit.twitter.Constant
import app.dropbit.twitter.client.Configuration
import okhttp3.Request
import java.net.URLEncoder

@Mockable
class Authorization constructor(val dateUtil: DateUtil, val configuration: Configuration,
                                val hmacSHA1Signer: HmacSHA1Signer) {

    fun addAuthorizationHeader(requestBuilder: Request.Builder) {
        requestBuilder.header("Authorization", asHeaderString(requestBuilder))
    }

    internal fun asHeaderString(requestBuilder: Request.Builder): String {
        val request = requestBuilder.build()
        val params = parametersForRequest(request)
        val signature = sign(buildSigningString(Uri.parse(request.url().toString()), request.method(), params))
        params.put("oauth_signature", signature)

        val stringBuilder = StringBuilder("OAuth ")

        params.keys.sorted().forEachIndexed { index: Int, key: String ->
            if (key.startsWith("oauth"))
                percentEncodeHeaderPair(stringBuilder, key, params.get(key).toString(), index < params.keys.size - 1)
        }

        return stringBuilder.toString()
    }

    internal fun buildSigningString(uri: Uri, method: String, parameters: Map<String, String>): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(method.toUpperCase()).append("&")
                .append(percentEncodeString(uriWithoutParams(uri)))
                .append("&")
        stringBuilder.append(percentEncodeString(buildSignatureBaseString(parameters)))
        return stringBuilder.toString()
    }

    internal fun uriWithoutParams(uri: Uri): String {
        return Uri.Builder().scheme(uri.scheme).authority(uri.authority)
                .path(uri.path).fragment(uri.fragment).toString()
    }

    internal fun buildSignatureBaseString(parameters: Map<String, String>): String {
        val keys = parameters.keys.sorted()
        val stringBuilder = StringBuilder()

        keys.forEachIndexed { index: Int, key: String ->
            if (index > 0) stringBuilder.append("&")
            stringBuilder.append(percentEncodeString(key)).append("=")
                    .append(percentEncodeString(parameters[key].toString()))
        }

        return stringBuilder.toString()
    }

    internal fun parametersForRequest(request: Request): MutableMap<String, String> {
        val params = parametersForSigning()

        val uri = Uri.parse(request.url().toString())
        if (uri.queryParameterNames.size > 0) {
            appendParams(params, uri)
        }
        return params
    }


    internal fun parametersForSigning(): MutableMap<String, String> {
        val parameters = mutableMapOf<String, String>()
        parameters[Constant.OAUTH_TIMESTAMP_KEY] = dateUtil.timeInSeconds().toString()
        parameters[Constant.OAUTH_NONCE_KEY] = provideNonce()
        parameters[Constant.OAUTH_CONSUMER_KEY_KEY] = configuration.apiKey()
        parameters[Constant.OAUTH_SIGNATURE_METHOD_KEY] = "HMAC-SHA1"
        parameters[Constant.OAUTH_VERSION_KEY] = "1.0"

        if (configuration.accessToken().isEmpty()) {
            withCallback(parameters)
        } else {
            withToken(parameters)
        }

        if (configuration.verifierToken().isNotEmpty()) {
            withVerifier(parameters)
        }
        return parameters
    }

    internal fun withVerifier(parameters: MutableMap<String, String>): MutableMap<String, String> {
        parameters[Constant.OAUTH_VERIFIER_KEY] = configuration.verifierToken()
        return parameters
    }

    internal fun withToken(parameters: MutableMap<String, String>): MutableMap<String, String> {
        parameters[Constant.OAUTH_TOKEN_KEY] = configuration.accessToken()
        return parameters
    }

    internal fun withCallback(parameters: MutableMap<String, String>): MutableMap<String, String> {
        parameters[Constant.OAUTH_CALLBACK_KEY] = configuration.callBackRoute()
        return parameters
    }

    private fun appendParams(map: MutableMap<String, String>, uri: Uri): MutableMap<String, String> {
        uri.queryParameterNames.forEach {
            val value: String = if (uri.getQueryParameters(it).size > 1) uri.getQueryParameters(it).toString() else uri.getQueryParameter(it).toString()
            map.put(it, value)
        }
        return map
    }

    private fun provideNonce(): String {
        return Base64.encodeToString(dateUtil.timeInSeconds().toString().toByteArray(),
                Base64.URL_SAFE or Base64.NO_CLOSE or Base64.NO_WRAP).toString()
    }

    private fun percentEncodeHeaderPair(stringBuilder: StringBuilder, key: String, value: String, addComma: Boolean) {
        stringBuilder.append(percentEncodeString(key))
                .append("=\"").append(percentEncodeString(value)).append("\"")

        if (addComma) stringBuilder.append(", ")
    }

    private fun percentEncodeString(value: String): String {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.M) {
            return URLEncoder.encode(value, "UTF-8")
        } else {
            return URLEncoder.encode(value, Charsets.UTF_8.toString())
        }
    }

    internal fun sign(data: String): String {
        return hmacSHA1Signer.signAndEncodeDigest(data, configuration.signingKey())
    }


}
