package com.coinninja.coinkeeper.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.db.schema.V34_Schema
import com.coinninja.coinkeeper.model.db.DaoMaster
import com.commonsware.cwac.saferoom.SafeHelperFactory
import org.greenrobot.greendao.database.Database


@Mockable
internal class DatabaseOpenHelper constructor(
        internal val context: Context,
        internal val migrationExecutor: MigrationExecutor,
        internal val version: Int
) : DaoMaster.OpenHelper(context, DB_NAME) {

    val callBack: Callback = object : Callback(version) {

        override fun onCreate(db: SupportSQLiteDatabase) {
            onCreate(wrap(db))
            onUpgrade(db, V34_Schema.SCHEMA_VERSION, version)
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(wrap(db), oldVersion, newVersion)
        }
    }


    override fun onCreate(db: Database) {
        V34_Schema().create(db)
        onUpgrade(db, V34_Schema.SCHEMA_VERSION, version)
    }

    override fun onUpgrade(db: Database, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) migrationExecutor.performUpgrade(db, oldVersion, newVersion)
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
    }

    override fun onOpen(db: Database?) {
        super.onOpen(db)
    }

    fun updateDatabaseFormat(secret: CharArray){
        val openHelper = supportHelper(secret)
        wrap(openHelper.writableDatabase)
    }

    private fun supportHelper(secret: CharArray): SupportSQLiteOpenHelper {
        return SafeHelperFactory(secret, SafeHelperFactory.POST_KEY_SQL_MIGRATE)
                .create(context, DB_NAME, callBack)
    }


    internal fun wrap(db: SupportSQLiteDatabase): Database {
        return CNDatabase(db)
    }

    companion object {
        private const val DB_NAME = "coin-ninja-db"
    }

}