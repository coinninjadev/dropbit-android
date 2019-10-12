package com.coinninja.coinkeeper.db

import android.os.Build.VERSION_CODES
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.Serializable

@RunWith(AndroidJUnit4::class)
class DatabaseSecretProviderTest {
    companion object {
        private val appSecret: CharArray = "--app-secret--".toCharArray()
        private val defaultSecret: CharArray = "--default-secret--".toCharArray()

    }

    private fun createSecretProvider(): DatabaseSecretProvider = DatabaseSecretProvider(mock(), appSecret, defaultSecret)

    @Test
    @Config(sdk = [VERSION_CODES.O])
    fun hashes_app_secret() {
        val hashedSecret = "--hashed-secret--".toCharArray()
        val provider = createSecretProvider()
        whenever(provider.hasher.hash(appSecret)).thenReturn(hashedSecret)
        assertThat(provider.secret, equalTo<Serializable>(hashedSecret))
    }

    @Test
    @Config(sdk = [VERSION_CODES.N])
    fun uses_default_on_lower_apis() {
        val provider = createSecretProvider()
        assertThat(provider.secret, equalTo<Serializable?>(defaultSecret))
    }

    @Test
    fun provides_default() {
        val provider = createSecretProvider()
        val hashedSecret = "--hashed-secret--".toCharArray()
        whenever(provider.hasher.hash(appSecret)).thenReturn(hashedSecret)
        assertThat(provider.default, equalTo(defaultSecret))
    }
}