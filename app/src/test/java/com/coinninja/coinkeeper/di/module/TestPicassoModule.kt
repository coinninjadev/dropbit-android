package com.coinninja.coinkeeper.di.module

import android.net.Uri
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Transformation
import dagger.Module
import dagger.Provides


@Module
class TestPicassoModule {
    @Provides
    fun picasso(): Picasso {
        val picasso: Picasso = mock()
        val requestCreator: RequestCreator = mock()
        whenever(picasso.load(any<Uri>())).thenReturn(requestCreator)
        whenever(picasso.load(any<String>())).thenReturn(requestCreator)
        whenever(requestCreator.transform(any<Transformation>())).thenReturn(requestCreator)
        return picasso
    }
}