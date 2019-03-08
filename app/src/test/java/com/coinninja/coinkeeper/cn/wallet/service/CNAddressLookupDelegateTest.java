package com.coinninja.coinkeeper.cn.wallet.service;

import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.Hasher;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CNAddressLookupDelegateTest {
    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @Mock
    private ServiceWorkUtil serviceWorkUtil;

    @Mock
    private Hasher hasher;

    @Mock
    private Contact contact;
    @Mock
    private CNAddressLookupDelegate.CNAddressLookupCompleteCallback callback;

    @InjectMocks
    private CNAddressLookupDelegate cnAddressLookupDelegate;

    private String phoneNumberHash = "--hash--";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        callback = null;
        cnAddressLookupDelegate = null;
        hasher = null;
        contact = null;
        serviceWorkUtil = null;
        localBroadCastUtil = null;
        phoneNumberHash = null;
    }

    @Test
    public void starts_observing_for_address_results_when_looking_up_contacts() {
        when(contact.getHash()).thenReturn(phoneNumberHash);

        cnAddressLookupDelegate.fetchAddressFor(contact, callback);

        verify(serviceWorkUtil).lookupAddressForPhoneNumberHash(phoneNumberHash);
        verify(localBroadCastUtil).registerReceiver(cnAddressLookupDelegate, cnAddressLookupDelegate.intentFilter);
    }

    @Test
    public void starts_observing_for_address_results_when_looking_up_by_phone_number() throws NumberParseException {
        PhoneNumber phoneNumber = new PhoneNumber("+13305551111");
        when(hasher.hash("13305551111")).thenReturn(phoneNumberHash);

        cnAddressLookupDelegate.fetchAddressFor(phoneNumber, callback);

        verify(serviceWorkUtil).lookupAddressForPhoneNumberHash(phoneNumberHash);
        verify(localBroadCastUtil).registerReceiver(cnAddressLookupDelegate, cnAddressLookupDelegate.intentFilter);
    }

    @Test
    public void stops_listening_when_result_fetched() {
        cnAddressLookupDelegate.onReceive(mock(Context.class), new Intent());
        verify(localBroadCastUtil).unregisterReceiver(cnAddressLookupDelegate);
    }

    @Test
    public void stops_listening_when_told_to_clean_up() {
        cnAddressLookupDelegate.teardown();

        verify(localBroadCastUtil).unregisterReceiver(cnAddressLookupDelegate);
    }

    @Test
    public void invokes_callback_when_fetching_via_contact() {
        cnAddressLookupDelegate.fetchAddressFor(contact, callback);
        AddressLookupResult result = new AddressLookupResult("hash", "address", "pubkey");
        Intent intent = new Intent(Intents.ACTION_WALLET_ADDRESS_RETRIEVED);
        intent.putExtra(Intents.EXTRA_ADDRESS_LOOKUP_RESULT, result);

        cnAddressLookupDelegate.onReceive(mock(Context.class), intent);

        verify(callback).onAddressLookupComplete(result);
    }

    @Test
    public void invokes_callback_when_fetching_via_phone_number() throws NumberParseException {
        PhoneNumber phoneNumber = new PhoneNumber("+13305551111");
        cnAddressLookupDelegate.fetchAddressFor(phoneNumber, callback);
        AddressLookupResult result = new AddressLookupResult("hash", "address", "pubkey");
        Intent intent = new Intent(Intents.ACTION_WALLET_ADDRESS_RETRIEVED);
        intent.putExtra(Intents.EXTRA_ADDRESS_LOOKUP_RESULT, result);

        cnAddressLookupDelegate.onReceive(mock(Context.class), intent);

        verify(callback).onAddressLookupComplete(result);
    }
}