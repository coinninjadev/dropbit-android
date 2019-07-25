package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.ExternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.service.client.BlockchainClient;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.BlockchainTX;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.wallet.data.TestData;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FailedBroadcastCleanerTest {

    @Mock
    private CoinKeeperApplication application;
    @Mock
    private TransactionHelper transactionHelper;
    @Mock
    private ExternalNotificationHelper externalNotificationHelper;
    @Mock
    private CoinKeeperApiClient apiClient;
    @Mock
    private BlockchainClient blockchainClient;
    @Mock
    private Analytics analytics;

    @InjectMocks
    private FailedBroadcastCleaner cleaner;

    @After
    public void tearDown() {
        application = null;
        transactionHelper = null;
        externalNotificationHelper = null;
        apiClient = null;
        blockchainClient = null;
        analytics = null;
        cleaner = null;
    }

    @Test
    public void if_there_are_no_pending_transaction_then_do_nothing() {
        List<TransactionSummary> samplePendingTransactions = new ArrayList<>();
        when(transactionHelper.getPendingTransactionsOlderThan(anyLong())).thenReturn(samplePendingTransactions);
        TransactionSummary unAcknowledged = mock(TransactionSummary.class);
        List<TransactionSummary> unAcknowledgedList = new ArrayList<>();
        unAcknowledgedList.add(unAcknowledged);

        cleaner.run();

        verify(transactionHelper, never()).markTransactionSummaryAsFailedToBroadcast(any());
        verify(externalNotificationHelper, never()).saveNotification(any(), any());
        verify(transactionHelper, never()).markTransactionSummaryAsAcknowledged(any());
    }

    @Test
    public void mark_as_failed_test() {
        TransactionSummary unAcknowledged = mock(TransactionSummary.class);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(transactionsInvitesSummary.getInviteTransactionSummary()).thenReturn(null);
        when(unAcknowledged.getTransactionsInvitesSummary()).thenReturn(transactionsInvitesSummary);
        when(unAcknowledged.getTxid()).thenReturn("some transaction id");
        List<TransactionSummary> unAcknowledgedList = new ArrayList<>();
        unAcknowledgedList.add(unAcknowledged);

        cleaner.markAsFailedToBroadcast(unAcknowledgedList);

        verify(transactionHelper).markTransactionSummaryAsFailedToBroadcast("some transaction id");
    }


    @Test
    public void tracks_failed_to_broadcast_for_tx() {
        TransactionSummary unAcknowledged = mock(TransactionSummary.class);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(transactionsInvitesSummary.getInviteTransactionSummary()).thenReturn(null);
        when(unAcknowledged.getTransactionsInvitesSummary()).thenReturn(transactionsInvitesSummary);
        when(unAcknowledged.getTxid()).thenReturn("some transaction id");
        List<TransactionSummary> unAcknowledgedList = new ArrayList<>();
        unAcknowledgedList.add(unAcknowledged);

        cleaner.markAsFailedToBroadcast(unAcknowledgedList);

        verify(analytics).trackEvent(Analytics.EVENT_PENDING_TRANSACTION_FAILED);
    }

    @Test
    public void tracks_failed_to_broadcast_for_invite() {
        TransactionSummary unAcknowledged = mock(TransactionSummary.class);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(transactionsInvitesSummary.getInviteTransactionSummary()).thenReturn(mock(InviteTransactionSummary.class));
        when(unAcknowledged.getTransactionsInvitesSummary()).thenReturn(transactionsInvitesSummary);
        when(unAcknowledged.getTxid()).thenReturn("some transaction id");
        List<TransactionSummary> unAcknowledgedList = new ArrayList<>();
        unAcknowledgedList.add(unAcknowledged);

        cleaner.markAsFailedToBroadcast(unAcknowledgedList);

        verify(analytics).trackEvent(Analytics.EVENT_PENDING_DROPBIT_SEND_FAILED);
    }

    @Test
    public void removes_transaction_found_on_coinninja_from_pending_list_test() {
        List<TransactionDetail> expectedResponse = new ArrayList<>();
        expectedResponse.add((TransactionDetail) buildTransactionResponse(TestData.INSTANCE.getTRANSACTIONS_ONE()).body());
        Response response = Response.success(expectedResponse);

        when(apiClient.getTransactions(any())).thenReturn(response);

        List<TransactionSummary> pendingList = new ArrayList<>();
        TransactionSummary mock1 = mock(TransactionSummary.class);
        when(mock1.getTxid()).thenReturn("1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24");
        TransactionSummary mock2 = mock(TransactionSummary.class);
        when(mock2.getTxid()).thenReturn("some tx id 2");

        pendingList.add(mock1);
        pendingList.add(mock2);


        List<TransactionSummary> newList = cleaner.checkCoinNinjaMarkAsAcknowledged(pendingList);

        assertThat(newList.size(), equalTo(1));
        assertThat(newList.get(0).getTxid(), equalTo("some tx id 2"));
    }

    @Test
    public void removes_NO_transaction_if_coinninja_returns_success_but_null_test() {

        Response response = Response.success(null);

        when(apiClient.getTransactions(any())).thenReturn(response);

        List<TransactionSummary> pendingList = new ArrayList<>();
        TransactionSummary mock1 = mock(TransactionSummary.class);
        when(mock1.getTxid()).thenReturn("1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24");
        TransactionSummary mock2 = mock(TransactionSummary.class);
        when(mock2.getTxid()).thenReturn("some tx id 2");

        pendingList.add(mock1);
        pendingList.add(mock2);


        List<TransactionSummary> newList = cleaner.checkCoinNinjaMarkAsAcknowledged(pendingList);

        assertThat(newList.size(), equalTo(2));
        assertThat(newList.get(0).getTxid(), equalTo("some tx id 2"));
        assertThat(newList.get(1).getTxid(), equalTo("1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24"));
    }

    @Test
    public void returns_null_if_coinninja_returns_bad_test() {

        when(apiClient.getTransactions(any())).thenReturn(getBadResponse());

        List<TransactionSummary> pendingList = new ArrayList<>();
        TransactionSummary mock1 = mock(TransactionSummary.class);
        when(mock1.getTxid()).thenReturn("1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24");
        TransactionSummary mock2 = mock(TransactionSummary.class);
        when(mock2.getTxid()).thenReturn("some tx id 2");

        pendingList.add(mock1);
        pendingList.add(mock2);


        List<TransactionSummary> newList = cleaner.checkCoinNinjaMarkAsAcknowledged(pendingList);

        assertThat(newList, nullValue());
    }

    @Test
    public void returns_empty_if_coinninja_has_all_of_the_requested_transactions_test() {

        List<TransactionDetail> expectedResposne = new ArrayList<>();
        expectedResposne.add((TransactionDetail) buildTransactionResponse(TestData.INSTANCE.getTRANSACTIONS_ONE()).body());
        Response response = Response.success(expectedResposne);

        when(apiClient.getTransactions(any())).thenReturn(response);

        List<TransactionSummary> pendingList = new ArrayList<>();
        TransactionSummary mock1 = mock(TransactionSummary.class);
        when(mock1.getTxid()).thenReturn("1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24");

        pendingList.add(mock1);

        List<TransactionSummary> newList = cleaner.checkCoinNinjaMarkAsAcknowledged(pendingList);

        assertThat(newList.isEmpty(), equalTo(true));
    }

    @Test
    public void save_notification_test() {
        TransactionSummary unAcknowledged = mock(TransactionSummary.class);
        String transaction_id = "some transaction id";
        when(unAcknowledged.getTxid()).thenReturn(transaction_id);
        when(application.getString(R.string.notification_transaction_failed_to_broadcast, transaction_id))
                .thenReturn("Bitcoin network failed to broadcast transaction: some transaction id. Please try sending again.");


        cleaner.notifyUserOfBroadcastFail(new String[]{unAcknowledged.getTxid()});

        verify(externalNotificationHelper).saveNotification(eq("Bitcoin network failed to broadcast transaction: some transaction id. Please try sending again."), eq("some transaction id"));
    }

    @Test
    public void check_coinninja_mark_as_acknowledged_test() {
        List<TransactionDetail> expectedResposne = new ArrayList<>();
        expectedResposne.add((TransactionDetail) buildTransactionResponse(TestData.INSTANCE.getTRANSACTIONS_ONE()).body());
        Response response = Response.success(expectedResposne);

        when(apiClient.getTransactions(any())).thenReturn(response);

        List<TransactionSummary> pendingList = new ArrayList<>();
        TransactionSummary mock1 = mock(TransactionSummary.class);
        when(mock1.getTxid()).thenReturn("1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24");
        pendingList.add(mock1);


        cleaner.checkCoinNinjaMarkAsAcknowledged(pendingList);


        verify(transactionHelper).markTransactionSummaryAsAcknowledged("1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24");

    }

    @Test
    public void removes_transaction_found_on_blockchaininfo_from_pending_list_test() {

        when(blockchainClient.getTransactionFor(any())).thenReturn(buildBlockchainInfoResponse(BLOCK_CHAIN_INFO));

        List<TransactionSummary> pendingList = new ArrayList<>();
        TransactionSummary mock1 = mock(TransactionSummary.class);
        when(mock1.getTxid()).thenReturn("7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03");
        TransactionSummary mock2 = mock(TransactionSummary.class);
        when(mock2.getTxid()).thenReturn("some tx id 2");

        pendingList.add(mock1);
        pendingList.add(mock2);


        List<TransactionSummary> newList = cleaner.checkBlockchainInfoMarkAsAcknowledged(pendingList);

        assertThat(newList.size(), equalTo(1));
        assertThat(newList.get(0).getTxid(), equalTo("some tx id 2"));
    }

    @Test
    public void check_blockchaininfo_mark_as_acknowledged_test() {
        when(blockchainClient.getTransactionFor(any())).thenReturn(buildBlockchainInfoResponse(BLOCK_CHAIN_INFO));

        List<TransactionSummary> pendingList = new ArrayList<>();
        TransactionSummary mock1 = mock(TransactionSummary.class);
        when(mock1.getTxid()).thenReturn("7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03");
        pendingList.add(mock1);


        cleaner.checkBlockchainInfoMarkAsAcknowledged(pendingList);


        verify(transactionHelper).markTransactionSummaryAsAcknowledged("7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03");

    }

    private Response buildTransactionResponse(String data) {
        TransactionDetail block = new Gson().fromJson(data, new TypeToken<TransactionDetail>() {
        }.getType());
        return Response.success(block);
    }

    private Response buildBlockchainInfoResponse(String json) {
        return Response.success(new Gson().fromJson(json, BlockchainTX.class));

    }

    private Response getBadResponse() {
        return Response.error(400, ResponseBody.create(MediaType.parse("application/json"),
                "[]"));
    }

    public static final String BLOCK_CHAIN_INFO =
            "\n" +
                    "\n" +
                    "{\n" +
                    "   \"ver\":1,\n" +
                    "   \"inputs\":[\n" +
                    "      {\n" +
                    "         \"sequence\":4294967295,\n" +
                    "         \"witness\":\"0247304402204dcaba494328bd472f4bf61761e43c9ca204ea81ce9c5c57d669e4ed4721499f022007a6024b0f5e202a7f38bb90edbecaa788e276239a12aa42d958818d52db3f9f0121036ebf6ab96773a9fa7997688e1712ddc9722ef9274220ba406cb050ac5f1a1306\",\n" +
                    "         \"prev_out\":{\n" +
                    "            \"spent\":true,\n" +
                    "            \"tx_index\":314326392,\n" +
                    "            \"type\":0,\n" +
                    "            \"addr\":\"38wC41V2tNZrr2uiwUthn41b2M8SLGMVRt\",\n" +
                    "            \"value\":999934902,\n" +
                    "            \"n\":1,\n" +
                    "            \"script\":\"a9144f7728b2a54dc9a2b44e47341e7e029bb99c7d7287\"\n" +
                    "         },\n" +
                    "         \"script\":\"1600142f0908d7a15b75bfacb22426b5c1d78f545a683f\"\n" +
                    "      }\n" +
                    "   ],\n" +
                    "   \"weight\":669,\n" +
                    "   \"block_height\":502228,\n" +
                    "   \"relayed_by\":\"0.0.0.0\",\n" +
                    "   \"out\":[\n" +
                    "      {\n" +
                    "         \"spent\":true,\n" +
                    "         \"tx_index\":319280869,\n" +
                    "         \"type\":0,\n" +
                    "         \"addr\":\"18igMXPZwZEZjNQm8JAtPfkUHY5UyQRRiD\",\n" +
                    "         \"value\":100000000,\n" +
                    "         \"n\":0,\n" +
                    "         \"script\":\"76a91454aac92eb2398146daa547d921ed29a63891a76988ac\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "         \"spent\":false,\n" +
                    "         \"tx_index\":319280869,\n" +
                    "         \"type\":0,\n" +
                    "         \"addr\":\"3LGC2ejYwgnV5SKz6vX7TjdCkPVifDTSX8\",\n" +
                    "         \"value\":899764244,\n" +
                    "         \"n\":1,\n" +
                    "         \"script\":\"a914cbb86d23f9555a9a2dd084a8feb928b85b92712887\"\n" +
                    "      }\n" +
                    "   ],\n" +
                    "   \"lock_time\":0,\n" +
                    "   \"size\":249,\n" +
                    "   \"double_spend\":false,\n" +
                    "   \"time\":1514906173,\n" +
                    "   \"tx_index\":319280869,\n" +
                    "   \"vin_sz\":1,\n" +
                    "   \"hash\":\"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
                    "   \"vout_sz\":2\n" +
                    "}\n ";

}