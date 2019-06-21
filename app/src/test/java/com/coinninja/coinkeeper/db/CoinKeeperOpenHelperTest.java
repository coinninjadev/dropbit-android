package com.coinninja.coinkeeper.db;

import org.greenrobot.greendao.database.Database;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoinKeeperOpenHelperTest {

    @Mock
    DatabaseOpenHelper databaseOpenHelper;

    @Mock
    DatabaseSecretProvider databaseSecretProvider;

    @Mock
    Database database;

    private String appSecret = "--secret--";
    private String defaultSecret = "--default-secret--";

    @Before
    public void setUp() {
        when(databaseSecretProvider.getSecret()).thenReturn(appSecret);
        when(databaseSecretProvider.getDefault()).thenReturn(defaultSecret);
    }

    @Test
    public void opens_non_encrypted_db() {
        CoinKeeperOpenHelper coinKeeperOpenHelper = new CoinKeeperOpenHelper(databaseOpenHelper, databaseSecretProvider, false);
        when(databaseOpenHelper.getWritableDb()).thenReturn(database);

        assertThat(coinKeeperOpenHelper.getWritableDatabase(), equalTo(database));
    }

    @Test
    public void opens_encrypted_db() {
        CoinKeeperOpenHelper coinKeeperOpenHelper = new CoinKeeperOpenHelper(databaseOpenHelper, databaseSecretProvider, true);
        when(databaseOpenHelper.getEncryptedWritableDb(appSecret)).thenReturn(database);

        assertThat(coinKeeperOpenHelper.getWritableDatabase(), equalTo(database));
    }

    @Test
    public void uses_default_encryption_key_when_new_key_breaks() {
        CoinKeeperOpenHelper coinKeeperOpenHelper = new CoinKeeperOpenHelper(databaseOpenHelper, databaseSecretProvider, true);
        when(databaseOpenHelper.getEncryptedWritableDb(appSecret)).thenThrow(new net.sqlcipher.database.SQLiteException());
        when(databaseOpenHelper.getEncryptedWritableDb(defaultSecret)).thenReturn(database);

        assertThat(coinKeeperOpenHelper.getWritableDatabase(), equalTo(database));
    }
}