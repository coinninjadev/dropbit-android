package com.coinninja.coinkeeper.db

import androidx.sqlite.db.SupportSQLiteStatement
import org.greenrobot.greendao.database.DatabaseStatement

/**
 * TODO Adapter between saferoom and green robot, delete once fully migrated over to ROOM
 */
class CNStatement(val statement: SupportSQLiteStatement) : DatabaseStatement {
    override fun bindLong(index: Int, value: Long) {
        statement.bindLong(index, value)
    }

    override fun simpleQueryForLong(): Long {
        return statement.simpleQueryForLong()
    }

    override fun bindString(index: Int, value: String?) {
        statement.bindString(index, value)
    }

    override fun bindDouble(index: Int, value: Double) {
        statement.bindDouble(index, value)
    }

    override fun execute() {
        statement.execute()
    }

    override fun executeInsert(): Long {
        return statement.executeInsert()
    }

    override fun clearBindings() {
        statement.clearBindings()
    }

    override fun bindBlob(index: Int, value: ByteArray?) {
        statement.bindBlob(index, value)
    }

    override fun getRawStatement(): Any {
        return statement
    }

    override fun bindNull(index: Int) {
        statement.bindNull(index)
    }

    override fun close() {
        statement.close()
    }

}