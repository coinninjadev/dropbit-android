package com.coinninja.coinkeeper.util.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InternetUtilTest {

    @Mock
    Context context;

    @Mock
    ConnectivityManager connectivityManager;

    @Mock
    NetworkInfo activeNetwork;
    private InternetUtil util;

    @Before
    public void setUp() {
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(activeNetwork);
        when(activeNetwork.isConnected()).thenReturn(true);

        util = InternetUtil.newInstance(context);
    }

    @Test
    public void returns_with_internet_when_connected() {
        assertTrue(util.hasInternet());
    }

    @Test
    public void returns_false_when_not_connected() {
        when(activeNetwork.isConnected()).thenReturn(false);
        assertFalse(util.hasInternet());
    }

    @Test
    public void communicates_no_internet_when_info_is_null() {
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(null);

        assertFalse(util.hasInternet());
    }

    @Test
    public void init_with_context() {
        assertNotNull(util.getConnectivityManager());
    }

}