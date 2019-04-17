package com.coinninja.coinkeeper.util;

import android.content.Context;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.PaymentUtil.PaymentMethod;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.After;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class PaymentUtilTest {

    private static final String BTC_ADDRESS = "3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX";
    private static final String BASE58_BAD_ADDRESS = "3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkXEEEEEE";
    private static final String INVALID_BTC_ADDRESS = "---btc-address---";
    private static final String DISPLAY_NAME = "Joe Smoe";
    private static final String BC1_ADDRESS = "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4";
    private static final PhoneNumber PHONE_NUMBER = new PhoneNumber("+13305551111");

    private DefaultCurrencies defaultCurrencies;
    private PaymentUtil paymentUtil;
    private PaymentHolder paymentHolder;
    private Contact contact;
    private Context context;
    @Mock
    private TransactionFundingManager transactionFundingManager;
    @Mock
    private BitcoinUtil bitcoinUtil;
    private USDCurrency usdCurrency = new USDCurrency(2.d);
    private BTCCurrency btcCurrency = new BTCCurrency(1.d);
    private TransactionData validTransactionData = new TransactionData(new UnspentTransactionOutput[1],
            10000L, 1000L, 0, mock(DerivationPath.class), "");
    private TransactionData invalidTransactionData = new TransactionData(new UnspentTransactionOutput[0],
            0, 0, 0, mock(DerivationPath.class), "");
    private TransactionFee transactionFee;

    @After
    public void tearDown() {
        defaultCurrencies = null;
        paymentHolder = null;
        paymentUtil = null;
        contact = null;
        context = null;
        transactionFundingManager = null;
        bitcoinUtil = null;
        usdCurrency = null;
        btcCurrency = null;
        validTransactionData = null;
        invalidTransactionData = null;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        defaultCurrencies = new DefaultCurrencies(usdCurrency, btcCurrency);
        contact = new Contact(PHONE_NUMBER, DISPLAY_NAME, false);
        context = RuntimeEnvironment.application.getApplicationContext();

        when(bitcoinUtil.isValidBTCAddress(BTC_ADDRESS)).thenReturn(true);
        when(bitcoinUtil.isValidBTCAddress(BASE58_BAD_ADDRESS)).thenReturn(false);
        when(bitcoinUtil.isValidBTCAddress(BC1_ADDRESS)).thenReturn(false);
        when(bitcoinUtil.isValidBTCAddress(INVALID_BTC_ADDRESS)).thenReturn(false);

        paymentUtil = new PaymentUtil(context, bitcoinUtil, transactionFundingManager);
        paymentHolder = new PaymentHolder(new USDCurrency(5000.00d));
        paymentHolder.setDefaultCurrencies(defaultCurrencies);
        paymentHolder.setSpendableBalance(new BTCCurrency("1.0"));
        paymentHolder.updateValue(new USDCurrency(25d));
        transactionFee = new TransactionFee(5, 10, 15);
        paymentUtil.setTransactionFee(transactionFee);
        paymentUtil.setPaymentHolder(paymentHolder);
        when(bitcoinUtil.isValidBase58Address(anyString())).thenReturn(true);
    }

    @Test
    public void resetting_payment_util_nulls_out_address_and_fundingUTXO() {
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentUtil.reset();

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
        assertThat(paymentHolder.getPaymentAddress(), equalTo(BTC_ADDRESS));
    }

    @Test
    public void confirms_insufficient_funding_with_given_fees() {
        paymentUtil.setAddress(BTC_ADDRESS);
        BTCCurrency btcCurrency = (BTCCurrency) paymentUtil.getPaymentHolder().updateValue(new USDCurrency(25d));
        long spendableBalance = btcCurrency.toSatoshis() - 100L;
        paymentHolder.setSpendableBalance(new BTCCurrency(spendableBalance));

        when(transactionFundingManager.buildFundedTransactionData(eq(transactionFee), anyLong())).
                thenReturn(invalidTransactionData);

        assertFalse(paymentUtil.checkFunding());
        assertThat(paymentUtil.getErrorMessage(), equalTo("Attempting to send "
                + btcCurrency.toFormattedCurrency() + ". Not enough spendable funds\nAvailable "
                + new BTCCurrency(spendableBalance).toFormattedCurrency() + " $25.00"));
    }

    @Test
    public void confirms_funding_with_given_fees() {
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentHolder.setSpendableBalance((BTCCurrency) paymentUtil.getPaymentHolder().updateValue(new USDCurrency(50d)));
        when(transactionFundingManager.buildFundedTransactionData(eq(transactionFee), anyLong())).
                thenReturn(validTransactionData);

        assertTrue(paymentUtil.checkFunding());
        assertTrue(paymentUtil.isFunded());
    }


    @Test
    public void can_send_more_than_limit_to_address() {
        paymentUtil.getPaymentHolder().updateValue(new USDCurrency(25d));
        paymentUtil.setAddress(BTC_ADDRESS);
        when(transactionFundingManager.buildFundedTransactionData(eq(transactionFee), anyLong())).thenReturn(validTransactionData);

        assertTrue(paymentUtil.checkFunding());
        assertTrue(paymentUtil.isValid());
    }

    @Test
    public void will_not_limit_amount_can_send_to_verified_contact() {
        paymentUtil.getPaymentHolder().updateValue(new USDCurrency(101.d));
        contact.setVerified(true);
        paymentUtil.setContact(contact);
        when(transactionFundingManager.buildFundedTransactionData(eq(transactionFee), anyLong()))
                .thenReturn(validTransactionData);

        assertTrue(paymentUtil.checkFunding());
        assertTrue(paymentUtil.isValid());
        assertThat(paymentUtil.getErrorMessage(), equalTo(""));
    }

    @Test
    public void will_limit_amount_can_send_to_contact_invite() {
        paymentUtil.getPaymentHolder().updateValue(new USDCurrency(101.d));
        paymentUtil.setContact(contact);

        assertFalse(paymentUtil.isValid());

        assertThat(paymentUtil.getErrorMessage(),
                equalTo(context.getString(R.string.payment_error_too_much_sent_to_contact)));
    }

    @Test
    public void can_not_send_less_than_one_usd() {
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentHolder.updateValue(new USDCurrency(0.99d));

        assertFalse(paymentUtil.isValid());

        assertThat(paymentUtil.getErrorMessage(), equalTo(context.getResources()
                .getString(R.string.pay_error_too_little_transaction)));
    }

    @Test
    public void invalid_payment_amount_has_reason() {
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentUtil.getPaymentHolder().updateValue(new USDCurrency(0d));

        assertFalse(paymentUtil.isValid());

        assertThat(paymentUtil.getErrorMessage(), equalTo(context.getResources()
                .getString(R.string.pay_error_invalid_amount)));
    }

    @Test
    public void valid_payment_is_valid() {
        paymentUtil.setAddress(BTC_ADDRESS);
        when(transactionFundingManager.buildFundedTransactionData(any(), anyLong())).thenReturn(validTransactionData);

        paymentUtil.checkFunding();

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
        BTCCurrency btc = (BTCCurrency) paymentUtil.getPaymentHolder().updateValue(new USDCurrency(5d));

        assertThat(btc.toSatoshis(), equalTo(100000L));
    }

    @Test
    public void invalid_address_and_contact_message() {
        paymentUtil.setContact(null);

        assertThat(paymentUtil.getErrorMessage(), equalTo(context.getResources()
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
    public void payment_method_initially_invalid() {
        assertThat(paymentUtil.getPaymentMethod(), equalTo(PaymentMethod.INVALID));
    }

    @Test
    public void is_valid_checks_payment_type_and_payment_amount() {
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentHolder.updateValue(new BTCCurrency(0.d));
        assertFalse(paymentUtil.isValid());

        paymentHolder.updateValue(new BTCCurrency(1.d));
        assertTrue(paymentUtil.isValid());

        verifyZeroInteractions(transactionFundingManager);
    }

    @Test
    public void check_funding_validates_funding() {
        when(transactionFundingManager.buildFundedTransactionData(eq(transactionFee), anyLong()))
                .thenReturn(validTransactionData);

        assertTrue(paymentUtil.checkFunding());
    }

    @Test
    public void invalid_when_satoshisSpending_negative_from_long_overflow() {
        paymentUtil.getPaymentHolder().updateValue(new USDCurrency(101.d));
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentHolder.setSpendableBalance((BTCCurrency) paymentUtil.getPaymentHolder().updateValue(new USDCurrency(25d)));
        paymentHolder.setTransactionData(validTransactionData);

        validTransactionData.setAmount(-1);
        paymentHolder.setTransactionData(validTransactionData);
        assertFalse(paymentUtil.isFunded());

        validTransactionData.setAmount(1);
        validTransactionData.setFeeAmount(-1);
        paymentHolder.setTransactionData(validTransactionData);
        assertFalse(paymentUtil.isFunded());

        validTransactionData.setFeeAmount(1);
        validTransactionData.setChangeAmount(-1);
        paymentHolder.setTransactionData(validTransactionData);
        assertFalse(paymentUtil.isFunded());
    }

    @Test
    public void funding_max_calculates_max() {
        when(transactionFundingManager.buildFundedTransactionData(any(TransactionFee.class))).thenReturn(validTransactionData);

        assertTrue(paymentUtil.fundMax());

        assertThat(paymentHolder.getTransactionData(), equalTo(validTransactionData));
    }

    @Test
    public void can_clear_funding() {
        when(transactionFundingManager.buildFundedTransactionData(any(TransactionFee.class))).thenReturn(validTransactionData);
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentUtil.fundMax();

        paymentUtil.clearFunding();

        assertFalse(paymentUtil.isFunded());
        TransactionData transactionData = paymentHolder.getTransactionData();
        assertThat(transactionData.getUtxos().length, equalTo(0));
        assertThat(transactionData.getAmount(), equalTo(0L));
        assertThat(transactionData.getChangeAmount(), equalTo(0L));
        assertThat(transactionData.getFeeAmount(), equalTo(0L));
        assertThat(transactionData.getPaymentAddress(), equalTo(BTC_ADDRESS));
    }

    @Test
    public void ignores_floor_amount_when_sending_max() {
        paymentHolder.setEvaluationCurrency(new USDCurrency(1000.00d));
        paymentHolder.updateValue(new USDCurrency(.99d));
        when(transactionFundingManager.buildFundedTransactionData(any(TransactionFee.class))).thenReturn(validTransactionData);
        paymentUtil.setAddress(BTC_ADDRESS);
        paymentUtil.fundMax();

        assertTrue(paymentUtil.isValid());
        assertTrue(paymentUtil.isFunded());
    }

    @Test
    public void checking_funding_uses_send_max_flag() {
        paymentHolder.setEvaluationCurrency(new USDCurrency(1000.00d));
        paymentHolder.updateValue(new USDCurrency(5.99d));
        paymentUtil.setAddress(BTC_ADDRESS);
        when(transactionFundingManager.buildFundedTransactionData(any(TransactionFee.class))).thenReturn(validTransactionData);
        when(transactionFundingManager.buildFundedTransactionData(any(TransactionFee.class), anyLong())).thenReturn(invalidTransactionData);
        paymentUtil.fundMax();

        paymentUtil.checkFunding();

        assertTrue(paymentUtil.isValid());
        assertTrue(paymentUtil.isFunded());
        verify(transactionFundingManager).buildFundedTransactionData(any());
        verify(transactionFundingManager, times(0)).buildFundedTransactionData(any(), anyLong());
    }

    @Test
    public void keeps_values_when_setting() {
        when(transactionFundingManager.buildFundedTransactionData(any(TransactionFee.class))).thenReturn(validTransactionData);

        paymentUtil.fundMax();
        paymentUtil.setAddress(BTC_ADDRESS);

        assertThat(paymentHolder.getTransactionData(), equalTo(validTransactionData));
        assertThat(paymentHolder.getPaymentAddress(), equalTo(BTC_ADDRESS));

        paymentHolder.clearPayment();

        paymentUtil.setAddress(BTC_ADDRESS);
        paymentUtil.fundMax();
        assertThat(paymentHolder.getTransactionData(), equalTo(validTransactionData));
        assertThat(paymentHolder.getPaymentAddress(), equalTo(BTC_ADDRESS));
    }
}