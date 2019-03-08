package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class InviteContactRunnerTest {

    @Mock
    private SignedCoinKeeperApiClient client;
    @Mock
    private InviteContactRunner.OnInviteListener onInviteListener;
    @Mock
    CNWalletManager cnWalletManager;

    private USDCurrency bitcoinUSDPrice;
    private long satoshisSending;
    private String sendBtcToPhoneNumber;
    private InviteContactRunner inviteContactRunner;
    private Contact myContact;
    private final PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();
    private PhoneNumber toPhoneNumber;
    private PhoneNumber accountPhoneNumber;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        String myAccountPhoneNumber = "2223334444";
        sendBtcToPhoneNumber = "2165454564";
        toPhoneNumber = new PhoneNumber(sendBtcToPhoneNumber);
        accountPhoneNumber = new PhoneNumber(myAccountPhoneNumber);
        satoshisSending = 4568949465L;
        bitcoinUSDPrice = new USDCurrency("8000");

        myContact = new Contact(accountPhoneNumber, "send_to_self", true);
        when(cnWalletManager.getContact()).thenReturn(myContact);
        inviteContactRunner = new InviteContactRunner(client, cnWalletManager, phoneNumberUtil);
    }

    @After
    public void tearDown() throws Exception {
        client = null;
        onInviteListener = null;
        cnWalletManager = null;
        bitcoinUSDPrice = null;
        sendBtcToPhoneNumber = null;
        inviteContactRunner = null;
        myContact = null;
    }

    @Test
    public void send_invite_to_contact_test() {
        Contact sendToPhoneNumber = new Contact(toPhoneNumber, "Bill Bob", false);
        InvitedContact contact = mock(InvitedContact.class);

        Response mockResponse = Response.success(contact);

        when(client.invitePhoneNumber(anyLong(), anyLong(), any(), any())).thenReturn(mockResponse);
        inviteContactRunner.setOnInviteListener(onInviteListener);
        inviteContactRunner.setSatoshisSending(satoshisSending);
        inviteContactRunner.setUSAExchangeCurrency(bitcoinUSDPrice);

        inviteContactRunner.execute(sendToPhoneNumber);


        PhoneNumber senderPhoneNumber = myContact.getPhoneNumber();
        PhoneNumber receiverPhoneNumber = sendToPhoneNumber.getPhoneNumber();
        verify(client).invitePhoneNumber(36551596l, 4568949465l,
                senderPhoneNumber, receiverPhoneNumber);
        verify(onInviteListener).onInviteSuccessful(contact);
    }

    @Test
    public void send_invite_to_contact_fail() {
        Contact sendToPhoneNumber = new Contact(toPhoneNumber, "Bill Bob", false);

        ResponseBody body = ResponseBody.create(MediaType.parse("text"), "bad request");
        Response mockResponse = Response.error(400, body);

        when(client.invitePhoneNumber(anyLong(), anyLong(), any(), any())).thenReturn(mockResponse);
        inviteContactRunner.setOnInviteListener(onInviteListener);
        inviteContactRunner.setSatoshisSending(satoshisSending);
        inviteContactRunner.setUSAExchangeCurrency(bitcoinUSDPrice);

        inviteContactRunner.execute(sendToPhoneNumber);

        PhoneNumber senderPhoneNumber = myContact.getPhoneNumber();
        PhoneNumber receiverPhoneNumber = sendToPhoneNumber.getPhoneNumber();
        verify(client).invitePhoneNumber(36551596l, 4568949465l,
                senderPhoneNumber, receiverPhoneNumber);

        verify(onInviteListener).onInviteError(Intents.ACTION_DROPBIT__ERROR_UNKNOWN, "bad request");
    }

    @Test
    public void send_dropbit_rate_limit_error() {
        Contact sendToPhoneNumber = new Contact(toPhoneNumber, "Bill Bob", false);

        ResponseBody body = ResponseBody.create(MediaType.parse("text"), "rate limit error");
        Response mockResponse = Response.error(429, body);

        when(client.invitePhoneNumber(anyLong(), anyLong(), any(), any())).thenReturn(mockResponse);
        inviteContactRunner.setOnInviteListener(onInviteListener);
        inviteContactRunner.setSatoshisSending(satoshisSending);
        inviteContactRunner.setUSAExchangeCurrency(bitcoinUSDPrice);

        inviteContactRunner.execute(sendToPhoneNumber);

        PhoneNumber senderPhoneNumber = myContact.getPhoneNumber();
        PhoneNumber receiverPhoneNumber = sendToPhoneNumber.getPhoneNumber();
        verify(client).invitePhoneNumber(36551596l, 4568949465l,
                senderPhoneNumber, receiverPhoneNumber);

        verify(onInviteListener).onInviteError(Intents.ACTION_DROPBIT__ERROR_RATE_LIMIT, "rate limit error");
    }
}