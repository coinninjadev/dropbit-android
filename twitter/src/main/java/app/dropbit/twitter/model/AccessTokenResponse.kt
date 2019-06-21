package app.dropbit.twitter.model

import app.dropbit.twitter.Constant

class AccessTokenResponse {
    var oauthToken: String? = ""
    var oauthSecret: String? = ""
    var userId: String? = ""
    var screenName: String? = ""

    companion object {
        fun from(responseString: String): AccessTokenResponse {
            val accessTokenResponse = AccessTokenResponse()

            val params = responseString.split("&")
            if (params.size != 4) {
                return accessTokenResponse
            }

            params.forEach { param ->
                val item = param.split("=")
                when (item[0]) {
                    Constant.OAUTH_TOKEN_KEY -> accessTokenResponse.oauthToken = item[1]
                    Constant.OAUTH_TOKEN_SECRET_KEY -> accessTokenResponse.oauthSecret = item[1]
                    "screen_name" -> accessTokenResponse.screenName = item[1]
                    "user_id" -> accessTokenResponse.userId = item[1]
                }
            }
            return accessTokenResponse
        }
    }
}
