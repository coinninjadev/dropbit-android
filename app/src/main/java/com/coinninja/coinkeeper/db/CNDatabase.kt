package com.coinninja.coinkeeper.db

import android.database.Cursor
import android.database.SQLException
import androidx.sqlite.db.SupportSQLiteDatabase
import app.dropbit.annotations.Mockable
import org.greenrobot.greendao.database.Database
import org.greenrobot.greendao.database.DatabaseStatement


@Mockable
/**
 *  Adapter bridge between saferoom and green robot
 *  This will get removed when moved over to Room
 */
class CNDatabase(val db: SupportSQLiteDatabase) : Database {

    override fun rawQuery(sql: String?, selectionArgs: Array<String?>?): Cursor? {
        return db.query(sql, selectionArgs)
    }

    @Throws(SQLException::class)
    override fun execSQL(sql: String) {
        db.execSQL(sql)
    }

    override fun beginTransaction() {
        db.beginTransaction()
    }

    override fun endTransaction() {
        db.endTransaction()
    }

    override fun inTransaction(): Boolean {
        return db.inTransaction()
    }

    override fun setTransactionSuccessful() {
        db.setTransactionSuccessful()
    }

    @Throws(SQLException::class)
    override fun execSQL(sql: String?, bindArgs: Array<Any?>?) {
        db.execSQL(sql, bindArgs)
    }

    override fun compileStatement(sql: String): DatabaseStatement {
        return CNStatement(db.compileStatement(sql))
    }

    override fun isDbLockedByCurrentThread(): Boolean {
        return db.isDbLockedByCurrentThread
    }

    override fun close() {
        db.close()
    }

    override fun getRawDatabase(): Any {
        return db
    }

}
    
