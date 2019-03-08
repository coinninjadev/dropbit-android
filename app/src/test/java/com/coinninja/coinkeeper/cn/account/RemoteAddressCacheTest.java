package com.coinninja.coinkeeper.cn.account;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Arrays;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteAddressCacheTest {

    @Mock
    AccountManager accountManager;

    @Mock
    private SignedCoinKeeperApiClient apiClient;

    @InjectMocks
    private RemoteAddressCache remoteAddressCache;
    private HashMap<String, String> addressToPubKey = new HashMap<String, String>();

    private String addressPrefix = "-- addr ";
    private String addressPubKeyPrefix = "-- addr pub key ";

    private String addressOne = addressPrefix + "0";
    private String addressOnePubKey = addressPubKeyPrefix + "0";

    private String addressTwo = addressPrefix + "1";
    private String addressTwoPubKey = addressPubKeyPrefix + "1";

    private String addressThree = addressPrefix + "2";
    private String addressThreePubKey = addressPubKeyPrefix + "2";

    private String addressFour = addressPrefix + "3";
    private String addressFourPubKey = addressPubKeyPrefix + "3";

    private String addressFive = addressPrefix + "4";
    private String addressFivePubKey = addressPubKeyPrefix + "4";

    private List<CNWalletAddress> cachedAddresses = new ArrayList<>();

    @Before
    public void setUp() {
        addressToPubKey.put(addressOne, addressOnePubKey);
        addressToPubKey.put(addressTwo, addressTwoPubKey);
        addressToPubKey.put(addressThree, addressThreePubKey);
        addressToPubKey.put(addressFour, addressFourPubKey);
        addressToPubKey.put(addressFive, addressFivePubKey);

        when(accountManager.unusedAddressesToPubKey(HDWallet.EXTERNAL, 5)).thenReturn(addressToPubKey);

        for (Integer i = 0; i < 6; i++) {
            CNWalletAddress address = mock(CNWalletAddress.class);
            when(address.getAddress()).thenReturn(addressPrefix + i.toString());

            if (i != 3) {
                when(address.getPublicKey()).thenReturn(addressPubKeyPrefix + i.toString());
            }

            cachedAddresses.add(address);
        }

        when(apiClient.getCNWalletAddresses()).thenReturn(Response.success(cachedAddresses));
    }

    @After
    public void tearDown() {
        accountManager = null;
        apiClient = null;
        remoteAddressCache = null;
        addressToPubKey = null;
    }

    @Test
    public void only_cache_addresses_when_verified() {
        Response<Object> response = Response.error(401, ResponseBody.create(MediaType.parse("text/html"), ""));
        when(apiClient.getCNWalletAddresses()).
                thenReturn(response);

        remoteAddressCache.cacheAddresses();

        verify(apiClient, times(0)).removeAddress(anyString());
        verify(apiClient, times(0)).addAddress(anyString());
    }

    @Test
    public void sends_five_addresses_when_no_addresses_cached() {
        when(apiClient.getCNWalletAddresses()).thenReturn(Response.success(new ArrayList<CNWalletAddress>()));

        remoteAddressCache.cacheAddresses();

        verify(apiClient).addAddress(addressOne, addressOnePubKey);
        verify(apiClient).addAddress(addressTwo, addressTwoPubKey);
        verify(apiClient).addAddress(addressThree, addressThreePubKey);
        verify(apiClient).addAddress(addressFour, addressFourPubKey);
        verify(apiClient).addAddress(addressFive, addressFivePubKey);
    }

    @Test
    public void removes_addresses_that_do_not_align_with_unused_block() {
        remoteAddressCache.cacheAddresses();

        verify(apiClient).removeAddress("-- addr 5");
    }

    @Test
    public void removes_addresses_that_do_not_have_a_public_key() {
        remoteAddressCache.cacheAddresses();

        verify(apiClient).removeAddress("-- addr 3");
    }

    @Test
    public void adds_address_when_lists_do_not_match() {
        cachedAddresses.remove(3);
        remoteAddressCache.cacheAddresses();

        verify(apiClient).addAddress(addressFour, addressFourPubKey);
        verify(apiClient, times(1)).addAddress(anyString(), anyString());
    }
}
