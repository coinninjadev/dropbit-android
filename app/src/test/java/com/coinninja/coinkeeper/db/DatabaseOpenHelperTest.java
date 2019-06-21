package com.coinninja.coinkeeper.db;

import android.content.Context;

import org.greenrobot.greendao.database.Database;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseOpenHelperTest {
    @Mock
    private MigrationExecutor migrationExecutor;

    @Mock
    private Context context;

    @Mock
    private Database database;

    @InjectMocks
    private DatabaseOpenHelper databaseOpenHelper;

    @After
    public void tearDown() {
        context = null;
        migrationExecutor = null;
        database = null;
        databaseOpenHelper = null;
    }

    @Test
    public void upgrading_to_same_version_is_NOOP() {

        databaseOpenHelper.onUpgrade(database, 5, 5);

        verifyZeroInteractions(migrationExecutor);
    }


    @Test
    public void delegates_to_executor() {
        int newVersion = 5;
        int oldVersion = 4;
        databaseOpenHelper.onUpgrade(database, oldVersion, newVersion);

        verify(migrationExecutor).performUpgrade(database, oldVersion, newVersion);

    }

}