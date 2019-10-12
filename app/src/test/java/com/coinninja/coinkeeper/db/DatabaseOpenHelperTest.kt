package com.coinninja.coinkeeper.db

import com.nhaarman.mockitokotlin2.mock
import org.greenrobot.greendao.database.Database
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions

class DatabaseOpenHelperTest {
    private fun createHelper(): DatabaseOpenHelper {
        return DatabaseOpenHelper(mock(), mock(), 37)
    }

    @Test
    fun upgrading_to_same_version_is_NOOP() {
        val helper = createHelper()
        helper.onUpgrade(mock<Database>(), 5, 5)
        verifyZeroInteractions(helper.migrationExecutor)
    }

    @Test
    fun delegates_to_executor() {
        val newVersion = 5
        val oldVersion = 4
        val helper = createHelper()
        val db = mock<Database>()
        helper.onUpgrade(db, oldVersion, newVersion)
        verify(helper.migrationExecutor).performUpgrade(db, oldVersion, newVersion)
    }
}