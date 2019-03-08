package com.coinninja.coinkeeper.model;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.InfoV1;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.MetaV1;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.ProfileV1;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.TransactionNotificationV1;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class TransactionNotificationMapperTest {

    private static final String MEMO = "Here's your 5 dollars \uD83D\uDCB8";
    private static final String DISPLAY_NAME = "JoeJoe";
    private static final String AVATAR = "aW5zZXJ0IGF2YXRhciBoZXJlCg==";
    private static final String HANDLE = "handle";
    private static final String TXID = "txid";
    private static final long AMOUNT = 599L;
    private static final String CURRENCY = "USD";
    public static final int COUNTRY_CODE = 1;
    private static final int VERSION = COUNTRY_CODE;
    private static final String PHONE_NUMBER = "555555555";
    private static final CNPhoneNumber PHONE = new CNPhoneNumber(COUNTRY_CODE, PHONE_NUMBER);
    public static final String PHONE_NUMBER_STRING = "3305551111";
    public static final String CONTACT_PHONE_STRING = "+13305551111";

    private TransactionNotificationMapper mapper;
    private final PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();

    private PhoneNumber phoneNumber;
    private PhoneNumber contactPhoneNumber = new PhoneNumber(CONTACT_PHONE_STRING);

    @Before
    public void setUp() throws Exception {
        mapper = new TransactionNotificationMapper(phoneNumberUtil);
        phoneNumber = new PhoneNumber(COUNTRY_CODE, PHONE_NUMBER_STRING);
    }

    @Test
    public void test_to_v1() {
        TransactionNotification transactionNotification = new TransactionNotification();
        transactionNotification.setTxid(TXID);
        transactionNotification.setAmountCurrency(CURRENCY);
        transactionNotification.setAmount(AMOUNT);
        transactionNotification.setDisplayName(DISPLAY_NAME);
        transactionNotification.setDropbitMeHandle(HANDLE);
        transactionNotification.setAvatar(AVATAR);
        transactionNotification.setMemo(MEMO);

        transactionNotification.setPhoneNumber(new PhoneNumber(PHONE));

        TransactionNotificationV1 v1 = mapper.toV1(transactionNotification);

        assertEquals(v1.getMeta().getVersion(), COUNTRY_CODE);
        assertEquals(v1.getTxid(), TXID);
        assertEquals(v1.getInfo().getCurrency(), CURRENCY);
        assertEquals(v1.getInfo().getAmount(), AMOUNT);
        assertEquals(v1.getInfo().getMemo(), MEMO);
        assertEquals(v1.getProfile().getAvatar(), AVATAR);
        assertEquals(v1.getProfile().getDisplayName(), DISPLAY_NAME);
        assertEquals(v1.getProfile().getHandle(), HANDLE);
        assertEquals(v1.getProfile().getCountryCode(), COUNTRY_CODE);
        assertEquals(v1.getProfile().getPhoneNumber(), PHONE_NUMBER);

    }

    @Test
    public void test_from_TransactionNotificationV1() {

        TransactionNotificationV1 v1 = new TransactionNotificationV1();
        MetaV1 meta = new MetaV1();
        meta.setVersion(VERSION);
        v1.setMeta(meta);
        v1.setTxid(TXID);
        InfoV1 info = new InfoV1();
        info.setAmount(AMOUNT);
        info.setCurrency(CURRENCY);
        info.setMemo(MEMO);
        v1.setInfo(info);

        ProfileV1 profile = new ProfileV1();
        profile.setAvatar(AVATAR);
        profile.setDisplayName(DISPLAY_NAME);
        profile.setCountryCode(COUNTRY_CODE);
        profile.setPhoneNumber(PHONE_NUMBER);
        profile.setHandle(HANDLE);
        profile.setCountryCode(COUNTRY_CODE);
        profile.setPhoneNumber(PHONE_NUMBER);
        v1.setProfile(profile);

        TransactionNotification transactionNotification = mapper.fromV1(v1);

        assertEquals(transactionNotification.getMemo(), MEMO);
        assertEquals(transactionNotification.getDisplayName(), DISPLAY_NAME);
        assertEquals(transactionNotification.getAvatar(), AVATAR);
        assertEquals(transactionNotification.getDropbitMeHandle(), HANDLE);
        assertEquals(transactionNotification.getAmount(), AMOUNT);
        assertEquals(transactionNotification.getAmountCurrency(), CURRENCY);
        assertEquals(transactionNotification.getPhoneNumber(), new PhoneNumber(PHONE));
        assertTrue(transactionNotification.getIsShared());
    }

    @Test
    public void test_to_V1_from_completed_broadcast() {
        UnspentTransactionOutput[] outputs = new UnspentTransactionOutput[0];
        UnspentTransactionHolder holder = new UnspentTransactionHolder(
                10000100L,
                outputs,
                10000000L,
                100L,
                400000L,
                mock(DerivationPath.class),
                "--pay-address--");
        BroadcastTransactionDTO broadcastTransactionDTO = new BroadcastTransactionDTO(holder,
                new Contact(contactPhoneNumber, "Joe", true),
                true,
                "--memo--",
                "--pubkey--"
        );
        CompletedBroadcastDTO completedBroadcastDTO = new CompletedBroadcastDTO(broadcastTransactionDTO, "--txid--");
        Account account = new Account();
        account.setPhoneNumber(phoneNumber);

        TransactionNotificationV1 v1 = mapper.toV1(completedBroadcastDTO, account);

        assertEquals(v1.getMeta().getVersion(), COUNTRY_CODE);
        assertEquals(v1.getTxid(), "--txid--");
        assertEquals(v1.getInfo().getCurrency(), "USD");
        assertEquals(v1.getInfo().getAmount(), completedBroadcastDTO.getHolder().satoshisRequestingToSpend);
        assertEquals(v1.getInfo().getMemo(), "--memo--");
        assertEquals(v1.getProfile().getAvatar(), "");
        assertEquals(v1.getProfile().getDisplayName(), "");
        assertEquals(v1.getProfile().getHandle(), "");
        assertEquals(v1.getProfile().getCountryCode(), COUNTRY_CODE);
        assertEquals(v1.getProfile().getPhoneNumber(), PHONE_NUMBER_STRING);

    }

    @Test
    public void create_v1_from_invite_and_user_account() {
        Account account = mock(Account.class);
        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        TransactionNotification notification = mock(TransactionNotification.class);
        when(notification.getMemo()).thenReturn("--memo--");
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        when(invite.getTransactionNotification()).thenReturn(notification);
        when(invite.getBtcTransactionId()).thenReturn("--txid--");
        when(invite.getValueSatoshis()).thenReturn(10000L);

        TransactionNotificationV1 v1 = mapper.toV1(invite, account);

        assertEquals(v1.getMeta().getVersion(), COUNTRY_CODE);
        assertEquals(v1.getTxid(), "--txid--");
        assertEquals(v1.getInfo().getCurrency(), "USD");
        assertEquals(v1.getInfo().getAmount(), 10000L);
        assertEquals(v1.getInfo().getMemo(), "--memo--");
        assertEquals(v1.getProfile().getAvatar(), "");
        assertEquals(v1.getProfile().getDisplayName(), "");
        assertEquals(v1.getProfile().getHandle(), "");
        assertEquals(v1.getProfile().getCountryCode(), COUNTRY_CODE);
        assertEquals(v1.getProfile().getPhoneNumber(), PHONE_NUMBER_STRING);
    }
}