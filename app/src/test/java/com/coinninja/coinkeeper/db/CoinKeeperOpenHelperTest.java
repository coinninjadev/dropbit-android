package com.coinninja.coinkeeper.db;

import org.greenrobot.greendao.database.Database;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class CoinKeeperOpenHelperTest {

    @Mock
    MigrationExecutor migrationExecutor;

    @Mock
    Database database;

    private CoinKeeperOpenHelper coinKeeperOpenHelper;

    @Before
    public void setUp() {

        coinKeeperOpenHelper = new CoinKeeperOpenHelper(null, "", migrationExecutor);
    }

    @Test
    public void upgrading_to_same_version_is_NOOP() {

        coinKeeperOpenHelper.onUpgrade(database, 5, 5);

        verifyZeroInteractions(migrationExecutor);
    }


    @Test
    public void delegates_to_executor() {
        int newVersion = 5;
        int oldVersion = 4;
        coinKeeperOpenHelper.onUpgrade(database, oldVersion, newVersion);

        verify(migrationExecutor).performUpgrade(database, oldVersion, newVersion);

    }
}