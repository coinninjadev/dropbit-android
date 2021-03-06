package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper;
import com.coinninja.coinkeeper.model.db.Address;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.dto.AddressDTO;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.coinninja.cn.libbitcoin.model.DerivationPath;
import app.dropbit.commons.currency.BTCCurrency;
import edu.emory.mathcs.backport.java.util.Arrays;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IncomingInviteResponderTest {
    String response = "{\n" +
            "\"id\": \"6d1d7318-81b9-492c-b3f3-9d1b24f91d14\",\n" +
            "\"created_at\": 1531921356,\n" +
            "\"updated_at\": 1531921356,\n" +
            "\"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
            "\"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
            "}";

    @Mock
    private SignedCoinKeeperApiClient apiClient;
    @Mock
    private InviteTransactionSummaryHelper inviteTransactionSummaryHelper;
    @Mock
    private InternalNotificationHelper notificationHelper;
    @Mock
    private Analytics analytics;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    private AccountManager accountManager;
    @Mock
    private CNLogger cnLogger;
    @Mock
    private HDWalletWrapper hdWalletWrapper;

    @InjectMocks
    private IncomingInviteResponder incomingInviteResponder;

    private String sampleInviteServerID = "6d1d7318-81b9-492c-b3f3-9d1b24f91d14";

    private String addressOne = "-- addr 1 --";
    private String addressOnePubKey = "zw98gha3498ghezrgha98we4erghSDKJFGASKJHDA";
    private String addressTwo = "-- addr 2 --";
    private String addressTwoPubKey = "sv8h243098fhwq398fh2398rdfhwasduvfnbw9834rth";

    private ArrayList<String> unusedAddresses = new ArrayList(Arrays.asList(new String[]{addressOne, addressTwo}));
    private HashMap<String, AddressDTO> addresstoDTO = new HashMap<>();
    private List<InviteTransactionSummary> sampleInvites;


    public static final String FORMATTED_PHONE = "(222) 111-3333";

    private AddressDTO setupAddress(String address, String pubKey) {
        Address newAddress = mock(Address.class);
        when(newAddress.getAddress()).thenReturn(address);
        when(newAddress.getDerivationPath()).thenReturn(mock(DerivationPath.class));
        return new AddressDTO(newAddress, pubKey);
    }

    @Before
    public void setUp() {
        addresstoDTO.put(addressOne, setupAddress(addressOne, addressOnePubKey));
        addresstoDTO.put(addressTwo, setupAddress(addressTwo, addressTwoPubKey));
        sampleInvites = buildSampleInvites(sampleInviteServerID);
        when(accountManager.unusedAddressesToPubKey(HDWalletWrapper.EXTERNAL, sampleInvites.size())).thenReturn(addresstoDTO);
        when(walletHelper.getIncompleteReceivedInvites()).thenReturn(sampleInvites);
    }

    @Test
    public void send_generate_to_server_for_lightning_invite() {
        List<InviteTransactionSummary> sampleInvites = new ArrayList<>();
        InviteTransactionSummary sampleInvite = mock(InviteTransactionSummary.class);
        when(sampleInvite.getServerId()).thenReturn("--server--id--");
        when(sampleInvite.getValueSatoshis()).thenReturn(4521568L);
        when(sampleInvite.getLocaleFriendlyDisplayIdentityForSender()).thenReturn(FORMATTED_PHONE);
        when(sampleInvite.getType()).thenReturn(Type.LIGHTNING_RECEIVED);
        sampleInvites.add(sampleInvite);
        when(walletHelper.getIncompleteReceivedInvites()).thenReturn(sampleInvites);
        Wallet wallet = mock(Wallet.class);
        when(walletHelper.getPrimaryWallet()).thenReturn(wallet);
        when(walletHelper.getPrimaryWallet().getPurpose()).thenReturn(84);
        when(hdWalletWrapper.getVerificationKey()).thenReturn("--42-pub-key--");
        when(accountManager.unusedAddressesToPubKey(HDWalletWrapper.EXTERNAL, sampleInvites.size())).thenReturn(addresstoDTO);
        when(apiClient.sendAddressForInvite(anyString(), anyString(), anyString(), anyString())).thenReturn(getResponse(response));

        incomingInviteResponder.run();

        verify(apiClient).sendAddressForInvite("--server--id--", "generate",
                "--42-pub-key--", "lightning");
    }

    @Test
    public void send_address_to_server_show_internal_notification_test() {
        when(apiClient.sendAddressForInvite(anyString(), anyString(), anyString(), anyString())).thenReturn(getResponse(response));
        String expectedNotificationMessage =
                "We have sent a Bitcoin address to "
                        + FORMATTED_PHONE
                        + " for "
                        + new BTCCurrency(0.04521568).toFormattedCurrency()
                        + " to be sent.";

        incomingInviteResponder.run();

        verify(inviteTransactionSummaryHelper).updateInviteAddressTransaction(sampleInviteServerID, unusedAddresses.get(1));
        verify(notificationHelper).addNotifications(expectedNotificationMessage);
    }

    @Test
    public void reports_address_provided() {
        when(apiClient.sendAddressForInvite(anyString(), anyString(), anyString(), anyString())).thenReturn(getResponse(response));
        incomingInviteResponder.run();

        verify(analytics).trackEvent(Analytics.Companion.EVENT_DROPBIT_ADDRESS_PROVIDED);
    }

    @Test
    public void send_address_to_server_server_fails_nothing_happens_test() {
        when(apiClient.sendAddressForInvite(anyString(), anyString(), anyString(), anyString())).thenReturn(getBadResponse(404));

        incomingInviteResponder.run();

        verify(inviteTransactionSummaryHelper, times(0)).updateInviteAddressTransaction(anyString(), anyString());
        verify(notificationHelper, times(0)).addNotifications(anyString());
        verify(cnLogger).logError(anyString(), anyString(), any());
    }

    private List<InviteTransactionSummary> buildSampleInvites(String sampleInviteServerID) {

        List<InviteTransactionSummary> sampleInvites = new ArrayList<>();

        InviteTransactionSummary sampleInvite = mock(InviteTransactionSummary.class);
        Long sampleValueSatoshis = 4521568l;

        when(sampleInvite.getServerId()).thenReturn(sampleInviteServerID);
        when(sampleInvite.getValueSatoshis()).thenReturn(sampleValueSatoshis);
        when(sampleInvite.getLocaleFriendlyDisplayIdentityForSender()).thenReturn(FORMATTED_PHONE);
        sampleInvites.add(sampleInvite);
        return sampleInvites;
    }

    private Response getResponse(String json) {
        return Response.success(new Gson().fromJson(json, CNWalletAddress.class));
    }

    private Response getBadResponse(int code) {
        return Response.error(code,
                ResponseBody.create(
                        MediaType.parse("application/json"), ""));
    }
}