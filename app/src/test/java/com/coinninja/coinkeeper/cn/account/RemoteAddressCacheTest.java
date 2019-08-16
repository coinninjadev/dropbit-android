package com.coinninja.coinkeeper.cn.account;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.dto.AddressDTO;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress;
import com.coinninja.coinkeeper.util.RemoteAddressLocalCache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    @Mock
    RemoteAddressLocalCache remoteAddressLocalCache;

    @InjectMocks
    private RemoteAddressCache remoteAddressCache;
    private HashMap<String, AddressDTO> addressToPubKey = new HashMap<>();

    private String addressPrefix = "-- addr ";
    private String addressPubKeyPrefix = "-- addr pub key ";

    private String addressOne = addressPrefix + "0";
    private AddressDTO addressDTOOne;

    private String addressTwo = addressPrefix + "1";
    private AddressDTO addressDTOTwo;

    private String addressThree = addressPrefix + "2";
    private AddressDTO addressDTOThree;

    private String addressFour = addressPrefix + "3";
    private AddressDTO addressDTOFour;

    private String addressFive = addressPrefix + "4";
    private AddressDTO addressDTOFive;

    private List<CNWalletAddress> cachedAddresses = new ArrayList<>();

    private AddressDTO setupAddress(String address, String pubKey) {
        Address newAddress = mock(Address.class);
        when(newAddress.getDerivationPath()).thenReturn(mock(DerivationPath.class));
        when(newAddress.getAddress()).thenReturn(address);
        return new AddressDTO(newAddress, pubKey);
    }

    @Before
    public void setUp() {
        addressDTOOne = setupAddress(addressOne, addressPubKeyPrefix + "0");
        addressDTOTwo = setupAddress(addressTwo, addressPubKeyPrefix + "1");
        addressDTOThree = setupAddress(addressThree, addressPubKeyPrefix + "2");
        addressDTOFour = setupAddress(addressFour, addressPubKeyPrefix + "3");
        addressDTOFive = setupAddress(addressFive, addressPubKeyPrefix + "4");
        addressToPubKey.put(addressOne, addressDTOOne);
        addressToPubKey.put(addressTwo, addressDTOTwo);
        addressToPubKey.put(addressThree, addressDTOThree);
        addressToPubKey.put(addressFour, addressDTOFour);
        addressToPubKey.put(addressFive, addressDTOFive);

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

        verify(apiClient).addAddress(addressOne, addressDTOOne.getUncompressedPublicKey());
        verify(apiClient).addAddress(addressTwo, addressDTOTwo.getUncompressedPublicKey());
        verify(apiClient).addAddress(addressThree, addressDTOThree.getUncompressedPublicKey());
        verify(apiClient).addAddress(addressFour, addressDTOFour.getUncompressedPublicKey());
        verify(apiClient).addAddress(addressFive, addressDTOFive.getUncompressedPublicKey());
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

        verify(apiClient).addAddress(addressFour, addressDTOFour.getUncompressedPublicKey());
        verify(apiClient, times(1)).addAddress(anyString(), anyString());
    }

}
