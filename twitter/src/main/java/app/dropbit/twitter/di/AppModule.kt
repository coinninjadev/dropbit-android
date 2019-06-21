package app.dropbit.twitter.di

import android.content.Context
import android.net.Uri
import app.dropbit.twitter.BuildConfig
import app.dropbit.twitter.Twitter
import app.dropbit.twitter.TwitterProvider
import app.dropbit.twitter.di.qualifiers.TwitterCallbackUrl
import dagger.Module
import dagger.Provides

@Module
class AppModule constructor(val context: Context) {

    @Provides
    @TwitterCallbackUrl
    fun twitterCallbackUri(): Uri {
        return Uri.parse(BuildConfig.TWITTER_CALLBACK_URI)
    }

    @Provides
    fun twitter(): Twitter {
        return TwitterProvider.provide(context)
    }
}
