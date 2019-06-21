package app.dropbit.twitter.di

import app.dropbit.twitter.ui.login.TwitterLoginActivity
import dagger.Component

@Component(modules = arrayOf(AppModule::class))
interface Component {
    fun inject(twitterLoginActivity: TwitterLoginActivity)
}