package com.coinninja.coinkeeper.cn.account;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.wallet.data.TestData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddressCacheTest {
    private static final int numAddressesToCache = 10;

    @Mock
    private HDWallet hdWallet;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private AddressHelper addressHelper;

    private AddressCache addressCache;

    @Before
    public void setUp() {
        addressCache = new AddressCache(hdWallet, addressHelper, walletHelper, numAddressesToCache);
    }

    @After
    public void tearDown() {
        hdWallet = null;
        walletHelper = null;
        addressHelper = null;
        addressCache = null;
    }

    @Test
    public void requests_current_index_plus_padding() {
        int externalIndex = 22;
        int internalIndex = 2;
        when(walletHelper.getCurrentExternalIndex()).thenReturn(externalIndex);
        when(walletHelper.getCurrentInternalIndex()).thenReturn(internalIndex);
        when(hdWallet.fillBlock(anyInt(), anyInt(), anyInt())).thenReturn(new String[0]);

        addressCache.cacheAddressesFor(HDWallet.EXTERNAL);

        verify(hdWallet).fillBlock(HDWallet.EXTERNAL, 0, externalIndex + numAddressesToCache);

        addressCache.cacheAddressesFor(HDWallet.INTERNAL);

        verify(hdWallet).fillBlock(HDWallet.INTERNAL, 0, internalIndex + numAddressesToCache);
    }

    @Test
    public void caches_addresses_generated_by_hd_wallet() {
        int externalIndex = 5;
        when(walletHelper.getCurrentExternalIndex()).thenReturn(externalIndex);
        String[] addresses = Arrays.copyOfRange(TestData.INSTANCE.getEXTERNAL_ADDRESSES(), 0, 15);
        when(hdWallet.fillBlock(HDWallet.EXTERNAL, 0, 15)).thenReturn(addresses);

        addressCache.cacheAddressesFor(HDWallet.EXTERNAL);

        for (int i = 0; i < addresses.length; i++) {
            verify(addressHelper).saveAddress(HDWallet.EXTERNAL, i, addresses[i]);
        }

        verify(addressHelper, times(15)).saveAddress(eq(HDWallet.EXTERNAL), anyInt(), anyString());
    }

    @Test
    public void creates_internal_cache_only_when_necessary() {
        int internalIndex = 2;
        when(walletHelper.getCurrentInternalIndex()).thenReturn(internalIndex);
        when(addressHelper.getAddressCountFor(HDWallet.INTERNAL)).thenReturn(12);

        addressCache.cacheAddressesFor(HDWallet.INTERNAL);

        verify(hdWallet, times(0)).fillBlock(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void creates_external_cache_only_when_necessary() {
        int externalIndex = 22;
        when(walletHelper.getCurrentExternalIndex()).thenReturn(externalIndex);
        when(addressHelper.getAddressCountFor(HDWallet.EXTERNAL)).thenReturn(32);

        addressCache.cacheAddressesFor(HDWallet.EXTERNAL);

        verify(hdWallet, times(0)).fillBlock(anyInt(), anyInt(), anyInt());
    }


}

