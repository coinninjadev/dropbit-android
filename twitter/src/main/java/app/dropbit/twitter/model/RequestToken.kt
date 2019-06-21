package app.dropbit.twitter.model

import app.dropbit.annotations.Mockable
import app.dropbit.twitter.Constant

@Mockable
data class RequestToken constructor(var oAuthToken: String = "",
                                    var oAuthTokenSecret: String = "",
                                    var oAuthCallbackConfirmed: Boolean = false) {

    companion object {
        fun from(string: String): RequestToken {
            val requestToken = RequestToken()
            val params = string.split("&")
            if (params.size != 3) {
                return requestToken
            }

            params.forEach { param ->
                val item = param.split("=")
                when (item[0]) {
                    Constant.OAUTH_TOKEN_KEY -> requestToken.oAuthToken = item[1]
                    Constant.OAUTH_TOKEN_SECRET_KEY -> requestToken.oAuthTokenSecret = item[1]
                    "oauth_callback_confirmed" -> requestToken.oAuthCallbackConfirmed = if ("true".equals(item[1])) true else false
                }
            }
            return requestToken
        }
    }
}
