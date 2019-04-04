package com.coinninja.coinkeeper.view.adapter.util;

import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.SendState;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BindableTransactionTest {

    @Mock
    WalletHelper walletHelper;
    private BindableTransaction bindableTransaction;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00d));
        bindableTransaction = new BindableTransaction(walletHelper);
    }

    @Test
    public void returns_name_as_priority() {
        bindableTransaction.setContactName("--name--");
        bindableTransaction.setContactPhoneNumber("--number--");
        bindableTransaction.setTargetAddress("--address--");

        bindableTransaction.getIdentifiableTarget();
        assertThat(bindableTransaction.getIdentifiableTarget(), equalTo("--name--"));
    }

    @Test
    public void returns_number_formatted() {
        bindableTransaction.setContactName("");
        bindableTransaction.setContactPhoneNumber("+13305551111");
        bindableTransaction.setTargetAddress("--address--");

        bindableTransaction.getIdentifiableTarget();
        assertThat(bindableTransaction.getIdentifiableTarget(), equalTo("(330) 555-1111"));
    }

    @Test
    public void returns_number_when_name_not_available() {
        bindableTransaction.setContactName("");
        bindableTransaction.setContactPhoneNumber("+13305551111");
        bindableTransaction.setTargetAddress("--address--");

        bindableTransaction.getIdentifiableTarget();
        assertThat(bindableTransaction.getIdentifiableTarget(), equalTo("(330) 555-1111"));
    }

    @Test
    @Config(qualifiers = "es-rAU")
    public void returns_number_when_name_not_available_as_international_format() {
        bindableTransaction.setContactName("");
        bindableTransaction.setContactPhoneNumber("+13305551111");
        bindableTransaction.setTargetAddress("--address--");

        bindableTransaction.getIdentifiableTarget();
        assertThat(bindableTransaction.getIdentifiableTarget(), equalTo("+1 330-555-1111"));
    }

    @Test
    public void returns_address_when_no_contact_data() {
        bindableTransaction.setContactName("");
        bindableTransaction.setContactPhoneNumber("");
        bindableTransaction.setTargetAddress("--address--");

        bindableTransaction.getIdentifiableTarget();
        assertThat(bindableTransaction.getIdentifiableTarget(), equalTo("--address--"));
    }

    @Test
    public void calculatesTotalCryptoForSendState() {
        bindableTransaction.setFee(100L);
        bindableTransaction.setValue(1000L);

        bindableTransaction.setSendState(SendState.SEND);
        assertThat(bindableTransaction.totalCryptoForSendState().toLong(), equalTo(1100L));

        bindableTransaction.setSendState(SendState.RECEIVE);
        assertThat(bindableTransaction.totalCryptoForSendState().toLong(), equalTo(1000L));

        bindableTransaction.setSendState(SendState.TRANSFER);
        assertThat(bindableTransaction.totalCryptoForSendState().toLong(), equalTo(100L));
    }

    @Test
    public void calculatesTotalFiatForSendState() {
        bindableTransaction.setFee(1000L);
        bindableTransaction.setValue(100000L);

        bindableTransaction.setSendState(SendState.SEND);
        assertThat(bindableTransaction.totalFiatForSendState().toFormattedString(), equalTo("1.01"));

        bindableTransaction.setSendState(SendState.RECEIVE);
        assertThat(bindableTransaction.totalFiatForSendState().toFormattedString(), equalTo("1"));

        bindableTransaction.setSendState(SendState.TRANSFER);
        assertThat(bindableTransaction.totalFiatForSendState().toFormattedString(), equalTo("0.01"));
    }

    @Test
    public void simplifies_send_state__basic_direction() {
        bindableTransaction.setSendState(SendState.SEND);
        assertThat(bindableTransaction.getBasicDirection(), equalTo(SendState.SEND));
        bindableTransaction.setSendState(SendState.SEND_CANCELED);
        assertThat(bindableTransaction.getBasicDirection(), equalTo(SendState.SEND));
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_SEND);
        assertThat(bindableTransaction.getBasicDirection(), equalTo(SendState.SEND));

        bindableTransaction.setSendState(SendState.RECEIVE);
        assertThat(bindableTransaction.getBasicDirection(), equalTo(SendState.RECEIVE));
        bindableTransaction.setSendState(SendState.RECEIVE_CANCELED);
        assertThat(bindableTransaction.getBasicDirection(), equalTo(SendState.RECEIVE));
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_RECEIVE);
        assertThat(bindableTransaction.getBasicDirection(), equalTo(SendState.RECEIVE));

        bindableTransaction.setSendState(SendState.TRANSFER);
        assertThat(bindableTransaction.getBasicDirection(), equalTo(SendState.TRANSFER));
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_TRANSFER);
        assertThat(bindableTransaction.getBasicDirection(), equalTo(SendState.TRANSFER));
    }
}