package com.coinninja.coinkeeper.util;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.coinkeeper.model.dto.AddressDTO;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteAddressLocalCacheTest {

    private Gson gson = new Gson();
    @Mock
    private PreferencesUtil preferencesUtil;

    private List<AddressDTO> addressWrappers = new ArrayList();

    private RemoteAddressLocalCache remoteAddressLocalCache;

    @Test
    public void retrieves_addressees_from_local_storage() {
        DerivationPath path = new DerivationPath(49, 0, 0, 1, 10);
        AddressDTO address = new AddressDTO("-- address--",
                AddressDTO.toDerivationPathString(path),
                "-pub-key-");
        ArrayList<AddressDTO> cachedAddresses = new ArrayList<>();
        cachedAddresses.add(address);
        when(preferencesUtil.getString(RemoteAddressLocalCache.LOCAL_ADDRESS_CACHE_KEY, "[]"))
                .thenReturn("[{\"address\":\"-- address--\"," +
                        "\"derivationPath\":\"M/49/0/0/1/10\"," +
                        "\"uncompressedPublicKey\":\"-pub-key-\"}]");


        assertThat(remoteAddressLocalCache.getLocalRemoteAddressCache(),
                equalTo(cachedAddresses));
    }

    @Test
    public void returns_empty_array_when_no_addresses_saved_locally() {
        when(preferencesUtil.getString(RemoteAddressLocalCache.LOCAL_ADDRESS_CACHE_KEY, "[]"))
                .thenReturn("[]");

        assertThat(remoteAddressLocalCache.getLocalRemoteAddressCache(),
                equalTo(new ArrayList<AddressDTO>()));
    }

    @Test
    public void saves_addresses_to_shared_preferences() {
        DerivationPath path = new DerivationPath(49, 0, 0, 1, 10);
        AddressDTO address = new AddressDTO("-- address--",
                AddressDTO.toDerivationPathString(path),
                "-pub-key-");
        ArrayList<AddressDTO> addressesToCache = new ArrayList<>();
        addressesToCache.add(address);

        remoteAddressLocalCache.setLocalRemoteAddressCache(addressesToCache);

        verify(preferencesUtil).savePreference(
                RemoteAddressLocalCache.LOCAL_ADDRESS_CACHE_KEY, "[{\"address\":\"-- address--\"," +
                        "\"derivationPath\":\"M/49/0/0/1/10\"," +
                        "\"uncompressedPublicKey\":\"-pub-key-\"}]");
    }

    @Test
    public void accepts_null_or_empty_addresses_to_save() {
        remoteAddressLocalCache.setLocalRemoteAddressCache(null);
        remoteAddressLocalCache.setLocalRemoteAddressCache(new ArrayList<>());

        verify(preferencesUtil, times(2)).removePreference(RemoteAddressLocalCache.LOCAL_ADDRESS_CACHE_KEY);
    }

    @Before
    public void setUp() {
        remoteAddressLocalCache = new RemoteAddressLocalCache(preferencesUtil);
    }

    @After
    public void tearDown() {
        preferencesUtil = null;
        addressWrappers = null;
        remoteAddressLocalCache = null;
    }

}