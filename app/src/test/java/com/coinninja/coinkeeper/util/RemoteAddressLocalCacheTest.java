package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.model.dto.AddressDTO;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class RemoteAddressLocalCacheTest {

    @Mock
    PreferencesUtil preferencesUtil;
    private List<AddressDTO> addressWrappers = new ArrayList();
    RemoteAddressLocalCache remoteAddressLocalCache;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        remoteAddressLocalCache = new RemoteAddressLocalCache(preferencesUtil);
    }

    @After
    public void tearDown() {
        preferencesUtil = null;
        addressWrappers = null;
        remoteAddressLocalCache = null;
    }

    @Test
    public void set_remote_address_with_null_does_not_call_save() {
        remoteAddressLocalCache.setLocalRemoteAddressCache(null);

        verify(preferencesUtil, times(0)).savePreference(anyString(), anyString());
    }

    @Test
    public void set_remote_address_with_null_preferences_not_call_save() {
        remoteAddressLocalCache = new RemoteAddressLocalCache(null);
        remoteAddressLocalCache.setLocalRemoteAddressCache(addressWrappers);

        verify(preferencesUtil, times(0)).savePreference(anyString(), anyString());
    }

    @Test
    public void set_remote_address_with_null_preferences_not_call_get() {
        remoteAddressLocalCache = new RemoteAddressLocalCache(null);
        remoteAddressLocalCache.getLocalRemoteAddressCache();

        verify(preferencesUtil, times(0)).getString(anyString(), anyString());
    }

}