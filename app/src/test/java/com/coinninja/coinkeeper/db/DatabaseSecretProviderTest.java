package com.coinninja.coinkeeper.db;

import android.os.Build;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.util.Hasher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class DatabaseSecretProviderTest {

    private String appSecret = "--app-secret--";
    private String defaultSecret = "--default-secret--";

    @Mock
    private Hasher hasher;

    private DatabaseSecretProvider databaseSecretProvider;

    @After
    public void tearDown() {
        appSecret = null;
        defaultSecret = null;
        hasher = null;
        databaseSecretProvider = null;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        databaseSecretProvider = new DatabaseSecretProvider(hasher, appSecret, defaultSecret);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.O)
    public void hashes_app_secret() {
        String hashedSecret = "--hashed-secret--";
        when(hasher.hash(appSecret)).thenReturn(hashedSecret);

        assertThat(databaseSecretProvider.getSecret(), equalTo(hashedSecret));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void uses_default_on_lower_apis() {
        assertThat(databaseSecretProvider.getSecret(), equalTo(defaultSecret));
    }

    @Test
    public void provides_default() {
        String hashedSecret = "--hashed-secret--";
        when(hasher.hash(appSecret)).thenReturn(hashedSecret);

        assertThat(databaseSecretProvider.getDefault(), equalTo(defaultSecret));
    }


}