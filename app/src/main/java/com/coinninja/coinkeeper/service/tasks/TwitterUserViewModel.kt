package com.coinninja.coinkeeper.service.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.dropbit.twitter.Twitter
import app.dropbit.twitter.model.TwitterUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TwitterUserViewModel  : ViewModel {

    private var twitter: Twitter
    var followingTwitterUsers: MutableLiveData<List<TwitterUser>>? = null
    var searchTwitterUsers: MutableLiveData<List<TwitterUser>>? = null

    @Inject
    constructor(twitter: Twitter) {
        this.twitter = twitter
        this.followingTwitterUsers = MutableLiveData()
        this.searchTwitterUsers = MutableLiveData()
    }

    fun search(search: String) {
        GlobalScope.launch {
            val response = twitter.search(search)
            withContext(Dispatchers.Main) {
                response?.isSuccessful.let {
                    searchTwitterUsers?.value = response?.body()
                }
            }
        }
    }

    fun load() {
        GlobalScope.launch {
            val response = twitter.getFollowing()
            withContext(Dispatchers.Main) {
                response?.isSuccessful.let {
                    followingTwitterUsers?.value = response?.body()?.users
                }
            }
        }
    }

    fun setDefaultListToFollowing() {
        followingTwitterUsers?.value = followingTwitterUsers?.value
    }
}