package com.coinninja.coinkeeper.model;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.InfoV1;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.MetaV1;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.ProfileV1;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.TransactionNotificationV1;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TransactionNotificationMapperTest {

    private static final String MEMO = "Here's your 5 dollars \uD83D\uDCB8";
    private static final String DISPLAY_NAME = "JoeJoe";
    private static final String AVATAR = "aW5zZXJ0IGF2YXRhciBoZXJlCg==";
    private static final String HANDLE = "handle";
    private static final String TXID = "txid";
    private static final long AMOUNT = 599L;
    private static final String CURRENCY = "USD";
    private static final int COUNTRY_CODE = 1;
    private static final int VERSION = COUNTRY_CODE;
    private static final String PHONE_NUMBER = "555555555";
    private static final CNPhoneNumber PHONE = new CNPhoneNumber(COUNTRY_CODE, PHONE_NUMBER);
    private static final String PHONE_NUMBER_STRING = "3305551111";
    private static final String CONTACT_PHONE_STRING = "+13305551111";

    private TransactionNotificationMapper mapper;

    @Mock
    private PhoneNumberUtil phoneNumberUtil;

    @Mock
    private PhoneNumber phoneNumber;
    @Mock
    private PhoneNumber contactPhoneNumber;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mapper = new TransactionNotificationMapper(phoneNumberUtil);
        when(phoneNumber.getCountryCode()).thenReturn(COUNTRY_CODE);
        when(phoneNumber.getNationalNumber()).thenReturn(Long.parseLong(PHONE_NUMBER_STRING));
        when(contactPhoneNumber.toString()).thenReturn(CONTACT_PHONE_STRING);
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
        TransactionData transactionData = new TransactionData(
                outputs,
                10000000L,
                100L,
                400000L,
                mock(DerivationPath.class),
                "--pay-address--");
        BroadcastTransactionDTO broadcastTransactionDTO = new BroadcastTransactionDTO(transactionData,
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
        assertEquals(v1.getInfo().getAmount(), completedBroadcastDTO.getTransactionData().getAmount());
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