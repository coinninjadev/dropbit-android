package com.coinninja.coinkeeper.util;

import android.app.Application;
import android.content.Context;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.FundedCallback;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.PaymentUtil.PaymentMethod;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.IS_BC1;
import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NOT_BASE58;
import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class PaymentUtilTest {

    private static final String BTC_ADDRESS = "3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX";
    private static final String BASE58_BAD_ADDRESS = "3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkXEEEEEE";
    private static final String INVALID_BTC_ADDRESS = "---btc-address---";
    private static final String DISPLAY_NAME = "Joe Smoe";
    private final PhoneNumber PHONE_NUMBER = new PhoneNumber("+13305551111");
    private static final String BC1_ADDRESS = "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4";

    private Application application;
    private PaymentUtil paymentUtil;
    private PaymentHolder paymentHolder;
    private Contact contact;
    private Context context;

    @Mock
    private BitcoinUtil bitcoinUtil;
    @Mock
    private FundingUTXOs.Builder fundingUTXOsBuilder;
    @Mock
    private FundingUTXOs fundingUTXOs;
    @Mock
    private TargetStatHelper targetStatHelper;
    @Mock
    private FundedCallback fundedCallback;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        paymentHolder = new PaymentHolder(new USDCurrency(5000.00d),
                new TransactionFee(10L, 5L, 1L));
        paymentHolder.setSpendableBalance(new BTCCurrency("1.0"));
        paymentHolder.loadPaymentFrom(new USDCurrency(25d));
        contact = new Contact(PHONE_NUMBER, DISPLAY_NAME, false);
        application = RuntimeEnvironment.application;
        context = application.getApplicationContext();

        when(bitcoinUtil.isValidBTCAddress(BTC_ADDRESS)).thenReturn(true);
        when(bitcoinUtil.isValidBTCAddress(BASE58_BAD_ADDRESS)).thenReturn(false);
        when(bitcoinUtil.isValidBTCAddress(BC1_ADDRESS)).thenReturn(false);
        when(bitcoinUtil.isValidBTCAddress(INVALID_BTC_ADDRESS)).thenReturn(false);

        paymentUtil = new PaymentUtil(context, bitcoinUtil, targetStatHelper, fundingUTXOsBuilder);
        paymentUtil.setPaymentHolder(paymentHolder);
        paymentUtil.setUSDPayment(25d);
        when(bitcoinUtil.isValidBase58Address(anyString())).thenReturn(true);
    }

    @Test
    public void resetting_payment_util_nulls_out_address_and_fundingUTXO() {
        paymentUtil.fundingUTXOs = fundingUTXOs;
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentUtil.reset();

        assertNull(paymentUtil.fundingUTXOs);
        assertNull(paymentUtil.getAddress());
    }

    @Test
    public void BC1_addresses_are_not_supported() {
        paymentUtil.setAddress(BC1_ADDRESS);
        when(bitcoinUtil.getInvalidReason()).thenReturn(IS_BC1);

        assertFalse(paymentUtil.isValid());
        assertThat(paymentUtil.getErrorMessage(), equalTo(context.getResources().getString(R.string.bc1_error_message)));
    }

    @Test
    public void bad_base58_address_check_test() {
        when(bitcoinUtil.isValidBase58Address(BASE58_BAD_ADDRESS)).thenReturn(false);
        when(bitcoinUtil.getInvalidReason()).thenReturn(NOT_BASE58);

        paymentUtil.setAddress(BASE58_BAD_ADDRESS);

        assertFalse(paymentUtil.isValidPaymentMethod());
        assertThat(paymentUtil.getErrorMessage(), equalTo("Address Failed Base 58 check"));
    }

    @Test
    public void good_base58_address_check_test() {
        when(bitcoinUtil.isValidBase58Address(BASE58_BAD_ADDRESS)).thenReturn(true);

        paymentUtil.setAddress(BTC_ADDRESS);

        assertTrue(paymentUtil.isValidPaymentMethod());
        assertTrue(paymentUtil.getErrorMessage().isEmpty());
    }

    @Test
    public void confirms_insufficient_funding_with_given_fees() {
        long fee = 100l;
        paymentUtil.setAddress(BTC_ADDRESS);
        BTCCurrency btcCurrency = paymentUtil.setUSDPayment(25d);
        long spendableBalance = btcCurrency.toSatoshis() + fee + 1;
        paymentHolder.setSpendableBalance(new BTCCurrency(spendableBalance));
        paymentUtil.checkFunding(fundedCallback);
        when(fundingUTXOs.getSatoshisFeesSpending()).thenReturn(fee);
        when(fundingUTXOs.getSatoshisSpending()).thenReturn(btcCurrency.toSatoshis());
        long totalSpending = btcCurrency.toSatoshis() + fee;
        when(fundingUTXOs.getSatoshisTotalSpending()).thenReturn(totalSpending);
        long fundedTotal = btcCurrency.toSatoshis() - 99;
        when(fundingUTXOs.getSatoshisFundedTotal()).thenReturn(fundedTotal);

        paymentUtil.onComplete(fundingUTXOs);

        assertFalse(paymentUtil.isFunded());
        assertThat(paymentUtil.getErrorMessage(), equalTo("Attempting to send " + new BTCCurrency(totalSpending).toFormattedCurrency() + ". Not enough spendable funds\nAvailable " + new BTCCurrency(fundedTotal).toFormattedCurrency() + " $25.00"));
    }

    @Test
    public void confirms_funding_with_given_fees() {
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentHolder.setSpendableBalance(paymentUtil.setUSDPayment(50d));
        BTCCurrency btcCurrency = paymentUtil.setUSDPayment(25d);
        paymentUtil.checkFunding(fundedCallback);
        long fee = 100l;
        when(fundingUTXOs.getSatoshisFeesSpending()).thenReturn(fee);
        when(fundingUTXOs.getSatoshisSpending()).thenReturn(btcCurrency.toSatoshis());
        when(fundingUTXOs.getSatoshisTotalSpending()).thenReturn(btcCurrency.toSatoshis() + fee);
        when(fundingUTXOs.getSatoshisFundedTotal()).thenReturn(paymentHolder.getSpendableBalance().toSatoshis());

        paymentUtil.onComplete(fundingUTXOs);

        assertTrue(paymentUtil.isFunded());
    }

    @Test
    public void forwards_funding_utxo_to_caller() {
        paymentHolder.setSpendableBalance(paymentUtil.setUSDPayment(25d));
        paymentUtil.checkFunding(fundedCallback);

        paymentUtil.onComplete(fundingUTXOs);

        verify(fundedCallback).onComplete(fundingUTXOs);
    }

    @Test
    public void can_send_more_than_limit_to_address() {
        paymentUtil.setUSDPayment(101.d);
        paymentUtil.setAddress(BTC_ADDRESS);

        assertTrue(paymentUtil.isValid());
    }

    @Test
    public void will_not_limit_amount_can_send_to_verified_contact() {
        paymentUtil.setUSDPayment(101.d);
        contact.setVerified(true);
        paymentUtil.setContact(contact);

        assertTrue(paymentUtil.isValid());

        assertThat(paymentUtil.getErrorMessage(),
                equalTo(""));
    }

    @Test
    public void will_limit_amount_can_send_to_contact_invite() {
        paymentUtil.setUSDPayment(101.d);
        paymentUtil.setContact(contact);

        assertFalse(paymentUtil.isValid());

        assertThat(paymentUtil.getErrorMessage(),
                equalTo(application.getString(R.string.payment_error_too_much_sent_to_contact)));
    }

    @Test
    public void payment_valid_when_enough_funds_to_cover_payment() {
        paymentUtil.setAddress(BTC_ADDRESS);
        BTCCurrency spendableBalance = paymentUtil.setUSDPayment(15d);
        paymentHolder.setSpendableBalance(spendableBalance);
        BTCCurrency payment = paymentUtil.setUSDPayment(25d);

        assertFalse(paymentUtil.isValid());

        String error = "Attempting to send " + payment.toFormattedCurrency() + ". Not enough spendable funds\nAvailable " + paymentHolder.getSpendableBalance().toFormattedCurrency() + " $15.00";

        assertThat(paymentUtil.getErrorMessage(), equalTo(error));

    }

    @Test
    public void valid_payment_when_btc_amount_present_with_address() {
        paymentUtil.setAddress(BTC_ADDRESS);

        assertTrue(paymentUtil.isValid());
    }

    @Test
    public void can_not_send_less_than_one_usd() {
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentUtil.setUSDPayment(0.99d);

        assertFalse(paymentUtil.isValid());

        assertThat(paymentUtil.getErrorMessage(), equalTo(application.getResources()
                .getString(R.string.pay_error_too_little_transaction)));
    }

    @Test
    public void invalid_payment_amount_has_reason() {
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentUtil.setUSDPayment(0d);

        assertFalse(paymentUtil.isValid());

        assertThat(paymentUtil.getErrorMessage(), equalTo(application.getResources()
                .getString(R.string.pay_error_invalid_amount)));
    }

    @Test
    public void valid_payment_is_valid() {
        paymentUtil.setAddress(BTC_ADDRESS);

        assertTrue(paymentUtil.isValid());
    }

    @Test
    public void invalid_payment_method_makes_payment_not_valid() {
        paymentUtil.setAddress(INVALID_BTC_ADDRESS);
        when(bitcoinUtil.getInvalidReason()).thenReturn(NOT_STANDARD_BTC_PATTERN);

        assertFalse(paymentUtil.isValid());
    }

    @Test
    public void updating_price_to_spend_returns_converted_btc() {
        BTCCurrency btc = paymentUtil.setUSDPayment(5d);

        assertThat(btc.toSatoshis(), equalTo(100000L));
    }

    @Test
    public void invalid_address_and_contact_message() {
        paymentUtil.setContact(null);

        assertThat(paymentUtil.getErrorMessage(), equalTo(application.getResources()
                .getString(R.string.pay_error_add_valid_bitcoin_address)));
    }

    @Test
    public void invalid_address_provides_error_message() {
        paymentUtil.setAddress(INVALID_BTC_ADDRESS);
        when(bitcoinUtil.getInvalidReason()).thenReturn(NOT_STANDARD_BTC_PATTERN);

        paymentUtil.isValid();

        assertThat(paymentUtil.getErrorMessage(), equalTo("Invalid BTC address"));
    }

    @Test
    public void setting_invalid_address_is_not_valid() {
        paymentUtil.setAddress(INVALID_BTC_ADDRESS);
        when(bitcoinUtil.getInvalidReason()).thenReturn(NOT_STANDARD_BTC_PATTERN);

        paymentUtil.isValid();

        assertThat(paymentUtil.getPaymentMethod(), equalTo(PaymentMethod.INVALID));
    }

    @Test
    public void no_contact_is_invalid_method() {
        paymentUtil.setContact(null);
        assertThat(paymentUtil.getPaymentMethod(), equalTo(PaymentMethod.INVALID));
    }

    @Test
    public void no_address_is_invalid_method() {
        paymentUtil.setAddress(null);
        assertThat(paymentUtil.getPaymentMethod(), equalTo(PaymentMethod.INVALID));
    }

    @Test
    public void setting_verified_contact_defines_payment_method() {
        contact.setVerified(true);

        paymentUtil.setContact(contact);

        assertThat(paymentUtil.getPaymentMethod(), equalTo(PaymentMethod.VERIFIED_CONTACT));
    }

    @Test
    public void setting_contact_defines_payment_method() {
        paymentUtil.setAddress(BTC_ADDRESS);

        paymentUtil.setContact(contact);

        assertThat(paymentUtil.getPaymentMethod(), equalTo(PaymentMethod.INVITE));
    }

    @Test
    public void setting_address_defines_payment_method() {
        paymentUtil.setContact(contact);

        paymentUtil.setAddress(BTC_ADDRESS);

        assertThat(paymentUtil.getPaymentMethod(), equalTo(PaymentMethod.ADDRESS));
    }

    @Test
    public void accepts_contact_for_receiving_payment() {
        paymentUtil.setContact(contact);

        assertThat(paymentUtil.getContact(), equalTo(contact));
    }

    @Test
    public void accepts_null_contacts() {
        paymentUtil.setContact(null);

        assertNull(paymentUtil.getContact());
    }

    @Test
    public void empty_addresses_are_null() {
        paymentUtil.setAddress("");

        assertNull(paymentUtil.getAddress());
    }

    @Test
    public void accepts_address_for_payment() {
        paymentUtil.setAddress(BTC_ADDRESS);

        assertThat(paymentUtil.getAddress(), equalTo(BTC_ADDRESS));
    }

    @Test
    public void payment_method_intially_invalid() {
        assertThat(paymentUtil.getPaymentMethod(), equalTo(PaymentMethod.INVALID));
    }

    @Test
    public void invalid_when_satoshisFundedTotal_negative_from_long_overflow() {
        paymentUtil.setUSDPayment(101.d);
        paymentUtil.setAddress(BTC_ADDRESS);
        when(fundingUTXOs.getSatoshisFundedTotal()).thenReturn(-1L);
        paymentHolder.setSpendableBalance(paymentUtil.setUSDPayment(25d));
        paymentUtil.setFundingUTXOs(fundingUTXOs);

        assertFalse(paymentUtil.isValid());
    }

    @Test
    public void invalid_when_satoshisFeesSpending_negative_from_long_overflow() {
        paymentUtil.setUSDPayment(101.d);
        paymentUtil.setAddress(BTC_ADDRESS);
        when(fundingUTXOs.getSatoshisFeesSpending()).thenReturn(-1L);
        paymentHolder.setSpendableBalance(paymentUtil.setUSDPayment(25d));
        paymentUtil.setFundingUTXOs(fundingUTXOs);

        assertFalse(paymentUtil.isValid());
    }

    @Test
    public void invalid_when_satoshisSpending_negative_from_long_overflow() {
        paymentUtil.setUSDPayment(101.d);
        paymentUtil.setAddress(BTC_ADDRESS);
        when(fundingUTXOs.getSatoshisSpending()).thenReturn(-1L);
        paymentHolder.setSpendableBalance(paymentUtil.setUSDPayment(25d));
        paymentUtil.setFundingUTXOs(fundingUTXOs);

        assertFalse(paymentUtil.isValid());
    }

    @Test
    public void invalid_when_satoshisTotalSpending_negative_from_long_overflow() {
        paymentUtil.setUSDPayment(101.d);
        paymentUtil.setAddress(BTC_ADDRESS);
        when(fundingUTXOs.getSatoshisTotalSpending()).thenReturn(-1L);
        paymentHolder.setSpendableBalance(paymentUtil.setUSDPayment(25d));
        paymentUtil.setFundingUTXOs(fundingUTXOs);

        assertFalse(paymentUtil.isValid());
    }


    @Test
    public void invalid_when_satoshisFundedTotal_MaxValue() {
        paymentUtil.setUSDPayment(101.d);
        paymentUtil.setAddress(BTC_ADDRESS);
        when(fundingUTXOs.getSatoshisFundedTotal()).thenReturn(BTCCurrency.MAX_SATOSHI);
        paymentHolder.setSpendableBalance(paymentUtil.setUSDPayment(25d));
        paymentUtil.setFundingUTXOs(fundingUTXOs);

        assertFalse(paymentUtil.isValid());
    }

    @Test
    public void invalid_when_satoshisFeesSpending_MaxValue() {
        paymentUtil.setUSDPayment(101.d);
        paymentUtil.setAddress(BTC_ADDRESS);
        when(fundingUTXOs.getSatoshisFeesSpending()).thenReturn(BTCCurrency.MAX_SATOSHI);
        paymentHolder.setSpendableBalance(paymentUtil.setUSDPayment(25d));
        paymentUtil.setFundingUTXOs(fundingUTXOs);

        assertFalse(paymentUtil.isValid());
    }

    @Test
    public void invalid_when_satoshisSpending_MaxValue() {
        paymentUtil.setUSDPayment(101.d);
        paymentUtil.setAddress(BTC_ADDRESS);
        when(fundingUTXOs.getSatoshisSpending()).thenReturn(BTCCurrency.MAX_SATOSHI);
        paymentHolder.setSpendableBalance(paymentUtil.setUSDPayment(25d));
        paymentUtil.setFundingUTXOs(fundingUTXOs);

        assertFalse(paymentUtil.isValid());
    }

    @Test
    public void invalid_when_satoshisTotalSpending_MaxValue() {
        paymentUtil.setUSDPayment(101.d);
        paymentUtil.setAddress(BTC_ADDRESS);
        when(fundingUTXOs.getSatoshisTotalSpending()).thenReturn(BTCCurrency.MAX_SATOSHI);
        paymentHolder.setSpendableBalance(paymentUtil.setUSDPayment(25d));
        paymentUtil.setFundingUTXOs(fundingUTXOs);

        assertFalse(paymentUtil.isValid());
    }

}