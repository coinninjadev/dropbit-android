package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class SentInvitesStatusGetterTest {
    private Gson gson = new Gson();
    private SentInvitesStatusGetter runner;
    @Mock
    private SignedCoinKeeperApiClient apiClient;
    @Mock
    private TransactionHelper transactionHelper;
    @Mock
    private InternalNotificationHelper internalNotificationHelper;
    private PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();

    private TestCoinKeeperApplication application;
    private DropBitInvitation invite;

    private InviteTransactionSummary newSummary;
    private InviteTransactionSummary oldSummary;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        runner = new SentInvitesStatusGetter(application, internalNotificationHelper, apiClient, transactionHelper, phoneNumberUtil);
    }

    @Test
    public void update_new_invites() {
        String status = "new";
        mockSendForStatus(status, false);

        runner.run();

        verify(transactionHelper).updateInviteAddressTransaction(invite);
    }

    @Test
    public void does_nothing_when_server_out() {
        when(apiClient.getSentInvites()).thenReturn(getBadResponse());

        runner.run();

        verify(transactionHelper, never()).updateInviteAddressTransaction(any(DropBitInvitation.class));
    }

    @Test
    public void updates_completed_invites() {
        String status = "completed";
        mockSendForStatus(status, false);

        runner.run();

        verify(transactionHelper).updateInviteAddressTransaction(invite);
    }

    @Test
    public void updates_canceled_invites() {
        String status = "canceled";
        mockSendForStatus(status, false);

        runner.run();

        verify(transactionHelper).updateInviteAddressTransaction(invite);
    }

    @Test
    public void updates_expired_invites() {
        String status = "expired";
        mockSendForStatus(status, false);

        runner.run();

        verify(transactionHelper).updateInviteAddressTransaction(invite);
    }

    @Test
    public void sets_internal_notification_for_user_when_invite_expires_for_send() {
        String formatedPhone = "(330) 555-1111";
        String status = "expired";
        mockSendForStatus(status, true);

        runner.run();
        verify(internalNotificationHelper).addNotifications(
                application.getString(R.string.invite_send_expired_message, formatedPhone));


    }

    @Test
    public void sets_internal_notification_for_user_when_invite_is_canceled_for_send() {
        String status = "canceled";
        mockSendForStatus(status, true);

        runner.run();
        BTCCurrency btc = new BTCCurrency(1000L);
        btc.setCurrencyFormat(BTCCurrency.ALT_CURRENCY_FORMAT);

        verify(internalNotificationHelper).addNotifications(
                application.getString(R.string.invite_send_canceled_message,
                        "(330) 555-1111", btc.toFormattedCurrency()));

    }

    private void mockSendForStatus(String status, Boolean simulateChange) {
        Response response = getResponse(status);
        invite = (DropBitInvitation) ((List) response.body()).get(0);
        newSummary = getDao(invite);
        newSummary.setType(Type.SENT);
        newSummary.setValueSatoshis(1000L);
        PhoneNumberUtil util = new PhoneNumberUtil();
        PhoneNumber receiverPhoneNumber = new PhoneNumber("+13305551111");
        newSummary.setReceiverPhoneNumber(receiverPhoneNumber);
        oldSummary = getDao(invite);
        oldSummary.setType(Type.SENT);
        oldSummary.setValueSatoshis(1000L);
        oldSummary.setReceiverPhoneNumber(receiverPhoneNumber);
        if (simulateChange) {
            oldSummary.setBtcState(BTCState.UNFULFILLED);
            oldSummary.setReceiverPhoneNumber(receiverPhoneNumber);
        }
        when(apiClient.getSentInvites()).thenReturn(response);
        when(transactionHelper.getInviteTransactionSummary(invite)).thenReturn(oldSummary);
        when(transactionHelper.updateInviteAddressTransaction(invite)).thenReturn(newSummary);
    }

    private Response getResponse(String status) {
        List<DropBitInvitation> invites = gson.fromJson(response, new TypeToken<List<DropBitInvitation>>() {
        }.getType());

        for (DropBitInvitation invite : invites) {
            invite.setStatus(status);
        }
        return Response.success(invites);
    }

    private Response getBadResponse() {
        return Response.error(500,
                ResponseBody.create(
                        MediaType.parse("application/json"), ""));
    }

    private InviteTransactionSummary getDao(DropBitInvitation dropBitInvitation) {
        InviteTransactionSummary invite = new InviteTransactionSummary();
        if ("expired".equals(dropBitInvitation.getStatus())) {
            invite.setBtcState(BTCState.EXPIRED);
        } else if ("completed".equals(dropBitInvitation.getStatus())) {
            invite.setBtcState(BTCState.FULFILLED);
        } else if ("canceled".equals(dropBitInvitation.getStatus())) {
            invite.setBtcState(BTCState.CANCELED);
        } else if ("new".equals(dropBitInvitation.getStatus())) {
            invite.setBtcState(BTCState.UNFULFILLED);
        }

        return invite;
    }


    String response = "[\n" +
            "  {\n" +
            "    \"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
            "    \"created_at\": 1531921356,\n" +
            "    \"updated_at\": 1531921356,\n" +

            "    \"address\": \"\",\n" +
            "    \"metadata\": {\n" +
            "      \"amount\": {\n" +
            "        \"btc\": 120000000,\n" +
            "        \"usd\": 8292280\n" +
            "      },\n" +
            "      \"sender\": {\n" +
            "        \"country_code\": 1,\n" +
            "        \"phone_number\": \"5554441234\"\n" +
            "      },\n" +
            "      \"receiver\": {\n" +
            "        \"country_code\": 1,\n" +
            "        \"phone_number\": \"5554441234\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
            "    \"status\": \"new\",\n" +
            "    \"txid\": \"\",\n" +
            "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
            "  }\n" +
            "]";

}