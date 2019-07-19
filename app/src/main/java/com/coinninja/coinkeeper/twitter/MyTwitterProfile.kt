package com.coinninja.coinkeeper.twitter

import app.dropbit.annotations.Mockable
import app.dropbit.twitter.Twitter
import app.dropbit.twitter.model.TwitterUser
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Mockable
@CoinkeeperApplicationScope
class MyTwitterProfile @Inject constructor(
        internal val twitter: Twitter,
        internal val dropbitAccountHelper: DropbitAccountHelper
) {

    internal var myUser: TwitterUser? = null

    suspend fun loadMyProfile(): TwitterUser? {
        if (twitter.hasTwitterEnabled && dropbitAccountHelper.isTwitterVerified) {
            withContext(Dispatchers.IO) {
                if (myUser == null) {
                    myUser = twitter.me()
                }
            }
        }
        return myUser
    }

    fun clear() {
        myUser = null
    }

}
