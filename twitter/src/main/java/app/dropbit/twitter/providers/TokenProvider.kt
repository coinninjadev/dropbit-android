package app.dropbit.twitter.providers

import android.content.Context
import android.preference.PreferenceManager
import app.dropbit.annotations.Mockable
import app.dropbit.twitter.model.AccessTokenResponse
import app.dropbit.twitter.model.RequestToken

@Mockable
class TokenProvider constructor(val context: Context) {
    companion object {
        const val requestAuthTokenKey = "app.dropbit.twitter.RequestAuthToken"
        const val verifierKey = "app.dropbit.twitter.VerifierToken"
        const val authTokenKey = "app.dropbit.twitter.AuthToken"
        const val authTokenSecretKey = "app.dropbit.twitter.AuthTokenSecret"
        const val screenNameKey = "app.dropbit.twitter.ScreenName"
        const val userIdKey = "app.dropbit.twitter.UserId"
    }

    fun oAuthRequestToken(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(requestAuthTokenKey, "")
    }

    fun oAuthToken(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(authTokenKey, "")
    }

    fun oAuthTokenSecret(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(authTokenSecretKey, "")
    }

    fun userId(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(userIdKey, "")
    }

    fun screenName(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(screenNameKey, "")
    }

    fun accessToken(): String {
        val token: String = if (!oAuthToken().isEmpty()) oAuthToken() else if (!oAuthRequestToken().isEmpty()) oAuthRequestToken() else ""
        return token
    }

    fun verifierToken(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(verifierKey, "")
    }

    fun saveRequestToken(requestToken: RequestToken) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(authTokenSecretKey, requestToken.oAuthTokenSecret)
        editor.apply()
    }

    fun saveAccessToken(accessTokenResponse: AccessTokenResponse) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(authTokenSecretKey, accessTokenResponse.oauthSecret)
        editor.putString(authTokenKey, accessTokenResponse.oauthToken)
        editor.putString(userIdKey, accessTokenResponse.userId)
        editor.putString(screenNameKey, accessTokenResponse.screenName)
        editor.remove(requestAuthTokenKey)
        editor.remove(verifierKey)
        editor.apply()
    }

    fun saveRequestAuthToken(requestAuthToken: String, authVerifier: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(requestAuthTokenKey, requestAuthToken)
        editor.putString(verifierKey, authVerifier)
        editor.apply()
    }

    fun clear() {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.remove(authTokenSecretKey)
        editor.remove(requestAuthTokenKey)
        editor.remove(authTokenKey)
        editor.remove(screenNameKey)
        editor.remove(userIdKey)
        editor.apply()
    }


}

