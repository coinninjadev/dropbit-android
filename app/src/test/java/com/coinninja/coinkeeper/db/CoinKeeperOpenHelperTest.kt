package com.coinninja.coinkeeper.db

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import net.sqlcipher.database.SQLiteException
import org.greenrobot.greendao.database.Database
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test

class CoinKeeperOpenHelperTest {
    private val appSecret = "--secret--".toCharArray()
    private val defaultSecret = "--default-secret--".toCharArray()

    private fun createOpenHelper(withEncryption: Boolean = true): CoinKeeperOpenHelper {
        val helper = CoinKeeperOpenHelper(mock(), mock(), mock(), withEncryption)
        whenever(helper.databaseSecretProvider.secret).thenReturn(appSecret)
        whenever(helper.upgradeDBFormatStorage.isUpgraded).thenReturn(true)
        whenever(helper.databaseSecretProvider.default).thenReturn(defaultSecret)
        return helper
    }

    @Test
    fun opens_non_encrypted_db() {
        val helper = createOpenHelper(false)
        val database:Database = mock()
        whenever(helper.databaseOpenHelper.writableDb).thenReturn(database)

        assertThat(helper.writableDatabase, equalTo(database))
    }

    @Test
    fun opens_encrypted_db() {
        val helper = createOpenHelper()
        val database:Database = mock()
        whenever(helper.databaseOpenHelper.getEncryptedWritableDb(appSecret)).thenReturn(database)

        assertThat(helper.writableDatabase, equalTo(database))
    }

    @Test
    fun uses_default_encryption_key_when_new_key_breaks() {
        val helper = createOpenHelper()
        val database:Database = mock()
        whenever(helper.databaseOpenHelper.getEncryptedWritableDb(appSecret)).thenThrow(SQLiteException())
        whenever(helper.databaseOpenHelper.getEncryptedWritableDb(defaultSecret)).thenReturn(database)

        assertThat(helper.writableDatabase, equalTo(database))
    }
}