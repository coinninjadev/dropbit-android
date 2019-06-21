package com.coinninja.coinkeeper.db

import android.content.Context
import com.coinninja.coinkeeper.db.schema.V31_Schema
import com.coinninja.coinkeeper.model.db.DaoMaster
import org.greenrobot.greendao.database.Database


class TestOpenHelper(context: Context?, name: String?) : DaoMaster.DevOpenHelper(context, name) {
    val writableV31Db: Database
        get() {
            val db = writableDb
            DaoMaster.dropAllTables(db, true)
            V31_Schema().create(db)
            return db
        }

    companion object {
        val dbName = "test-coin-keeper"
    }
}