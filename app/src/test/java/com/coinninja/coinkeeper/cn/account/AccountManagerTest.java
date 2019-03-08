package com.coinninja.coinkeeper.cn.account;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountManagerTest {

    @Mock
    HDWallet hdWallet;

    @Mock
    private WalletHelper walletHelper;
    @Mock
    private AddressCache addressCache;
    @Mock
    private AddressHelper addressHelper;

    @InjectMocks
    private AccountManager accountManager;

    @After
    public void tearDown() {
        walletHelper = null;
        addressHelper = null;
        addressCache = null;
        accountManager = null;
    }

    @Test
    public void instructs_address_cache_to_build_for_both_internal_and_external_chains() {
        accountManager.cacheAddresses();

        verify(addressCache).cacheAddressesFor(HDWallet.EXTERNAL);
        verify(addressCache).cacheAddressesFor(HDWallet.INTERNAL);
    }


    @Test
    public void reportLargestChangeIndexConsumed_when_less_than_current() {
        when(walletHelper.getCurrentInternalIndex()).thenReturn(22);
        accountManager.reportLargestChangeIndexConsumed(20);
        verify(walletHelper, times(0)).setInternalIndex(anyInt());
    }

    @Test
    public void reportLargestChangeIndexConsumed_when_same_as_current() {
        when(walletHelper.getCurrentInternalIndex()).thenReturn(22);
        accountManager.reportLargestChangeIndexConsumed(22);
        verify(walletHelper).setInternalIndex(23);
    }

    @Test
    public void reportLargestChangeIndexConsumed_when_greater_than_current() {
        when(walletHelper.getCurrentInternalIndex()).thenReturn(22);
        accountManager.reportLargestChangeIndexConsumed(23);
        verify(walletHelper).setInternalIndex(24);
    }

    @Test
    public void reportLargestReceiveIndexConsumed_when_less_than_current() {
        when(walletHelper.getCurrentExternalIndex()).thenReturn(22);
        accountManager.reportLargestReceiveIndexConsumed(20);
        verify(walletHelper, times(0)).setExternalIndex(anyInt());
    }

    @Test
    public void reportLargestReceiveIndexConsumed_when_same_as_current() {
        when(walletHelper.getCurrentExternalIndex()).thenReturn(22);
        accountManager.reportLargestReceiveIndexConsumed(22);
        verify(walletHelper).setExternalIndex(23);
    }

    @Test
    public void reportLargestReceiveIndexConsumed_when_greater_than_current() {
        when(walletHelper.getCurrentExternalIndex()).thenReturn(22);
        accountManager.reportLargestReceiveIndexConsumed(23);
        verify(walletHelper).setExternalIndex(24);
    }

    @Test
    public void returns_first_receive_address_when_address_cache_empty() {
        String address = "--address--";
        List<Address> addresses = new ArrayList<>();
        when(addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses);
        when(hdWallet.getExternalAddress(0)).thenReturn(address);

        assertThat(accountManager.getNextReceiveAddress(), equalTo(address));
    }

    @Test
    public void returns_next_change_address_from_hd_wallet_when_cache_is_empty() {
        List<Address> addresses = new ArrayList<>();
        when(addressHelper.getUnusedAddressesFor(HDWallet.INTERNAL)).thenReturn(addresses);

        assertThat(accountManager.getNextChangeIndex(), equalTo(0));
    }

    @Test
    public void returns_next_receive_address_from_hd_wallet_when_cache_is_empty() {
        List<Address> addresses = new ArrayList<>();
        when(addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses);

        assertThat(accountManager.getNextChangeIndex(), equalTo(0));
    }

    @Test
    public void returns_next_change_address() {
        Address address1 = new Address();
        address1.setAddress("---- address 1 ----");
        address1.setIndex(7);
        Address address2 = new Address();
        address2.setAddress("---- address 2 ----");
        address2.setIndex(14);
        List<Address> addresses = new ArrayList<>();
        addresses.add(address1);
        addresses.add(address2);

        when(addressHelper.getUnusedAddressesFor(HDWallet.INTERNAL)).thenReturn(addresses);

        assertThat(accountManager.getNextChangeIndex(), equalTo(7));
    }

    @Test
    public void returns_next_receive_address() {
        Address address1 = new Address();
        address1.setAddress("---- address 1 ----");
        address1.setIndex(5);
        Address address2 = new Address();
        address2.setAddress("---- address 2 ----");
        address2.setIndex(14);
        List<Address> addresses = new ArrayList<>();
        addresses.add(address1);
        addresses.add(address2);

        when(addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses);

        assertThat(accountManager.getNextReceiveIndex(), equalTo(5));
    }

    @Test
    public void returns_next_available_receive_address() {
        Address address1 = new Address();
        address1.setAddress("---- address 1 ----");
        address1.setIndex(0);
        Address address2 = new Address();
        address2.setAddress("---- address 2 ----");
        address2.setIndex(14);
        List<Address> addresses = new ArrayList<>();
        addresses.add(address1);
        addresses.add(address2);

        when(addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses);
        String nextAddress = "---- address 1 ----";
        String address = accountManager.getNextReceiveAddress();

        assertThat(address, equalTo(nextAddress));
    }

    @Test
    public void returns_array_of_addresses_for_requested_chain_and_block_size() {
        List<Address> addresses = new ArrayList<>();
        addresses.add(new Address(1L, "-- addr 1 --", 1L, 0, HDWallet.EXTERNAL));
        addresses.add(new Address(4L, "-- addr 4 --", 1L, 4, HDWallet.EXTERNAL));
        addresses.add(new Address(17L, "-- addr 7 --", 1L, 7, HDWallet.EXTERNAL));
        addresses.add(new Address(18L, "-- addr 8 --", 1L, 8, HDWallet.EXTERNAL));
        when(addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses);

        HashMap<String, String> block = accountManager.unusedAddressesToPubKey(HDWallet.EXTERNAL, 3);

        assertThat(block.keySet().size(), equalTo(3));
        assertThat(block.get("-- addr 1 --"), equalTo(null));
        assertThat(block.get("-- addr 4 --"), equalTo(null));
        assertThat(block.get("-- addr 7 --"), equalTo(null));
    }

    @Test
    public void returns_smaller_array_of_addresses_for_requested_chain_and_block_size_when_unused_limited() {
        List<Address> addresses = new ArrayList<>();
        addresses.add(new Address(1L, "-- addr 1 --", 1L, 0, HDWallet.EXTERNAL));
        addresses.add(new Address(4L, "-- addr 4 --", 1L, 4, HDWallet.EXTERNAL));
        when(addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses);

        HashMap<String, String> block = accountManager.unusedAddressesToPubKey(HDWallet.EXTERNAL, 3);

        assertThat(block.keySet().size(), equalTo(2));
        assertThat(block.get("-- addr 1 --"), equalTo(null));
        assertThat(block.get("-- addr 4 --"), equalTo(null));
    }

}