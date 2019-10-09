package com.coinninja.coinkeeper.db

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.di.interfaces.DBEncryption
import net.sqlcipher.database.SQLiteException
import org.greenrobot.greendao.database.Database

@Mockable
class CoinKeeperOpenHelper internal constructor(
        internal val databaseOpenHelper: DatabaseOpenHelper,
        internal val databaseSecretProvider: DatabaseSecretProvider,
        internal val upgradeDBFormatStorage: UpgradeDBFormatStorage,
        @DBEncryption internal val withEncryption: Boolean
) {
    val writableDatabase: Database
        get() = if (withEncryption) {
            encryptedWritableDb()
        } else {
            databaseOpenHelper.writableDb
        }

    private fun encryptedWritableDb(): Database {
        try {
            if (!upgradeDBFormatStorage.isUpgraded) {
                try {
                    databaseOpenHelper.updateDatabaseFormat(databaseSecretProvider.secret)
                } catch (ex: SQLiteException) {
                    databaseOpenHelper.updateDatabaseFormat(databaseSecretProvider.default)
                }
                upgradeDBFormatStorage.isUpgraded = true
            }
        } catch (e: Exception) {
            upgradeDBFormatStorage.isUpgraded = true
        }

        return try {
            databaseOpenHelper.getEncryptedWritableDb(databaseSecretProvider.secret)
        } catch (ex: SQLiteException) {
            databaseOpenHelper.getEncryptedWritableDb(databaseSecretProvider.default)
        }
    }

    companion object {
        val TAG: String = CoinKeeperOpenHelper::class.java.simpleName
    }

}