package com.coinninja.coinkeeper.bitcoin;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionBuilder;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.model.Transaction;
import com.coinninja.coinkeeper.service.client.BlockchainClient;
import com.coinninja.coinkeeper.service.client.BlockstreamClient;
import com.coinninja.coinkeeper.service.client.model.BlockchainTX;
import com.coinninja.coinkeeper.util.ErrorLoggingUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BroadcastTransactionHelperTest {
    private static final String BLOCK_CHAIN_INFO =
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

    @Mock
    private TransactionBuilder transactionBuilder;

    @Mock
    private Analytics analytics;

    @Mock
    BlockstreamClient blockstreamClient;

    @Mock
    private BlockchainClient blockchainClient;

    @Mock
    private TransactionData transactionData;

    @Mock
    private ErrorLoggingUtil errorLoggingUtil;

    @InjectMocks
    private BroadcastTransactionHelper broadcastHelper;

    private String txid = "--txid--";
    private String rawTxid = "RAW TX DATA 000000000djs9jds9js9jds";
    private TransactionBroadcastResult transactionBroadcastResult = new TransactionBroadcastResult(200, true, "", new Transaction("", txid));

    @After
    public void tearDown() {
        txid = null;
        rawTxid = null;
        transactionBroadcastResult = null;
        analytics = null;
        transactionBuilder = null;
        blockchainClient = null;
        transactionData = null;
        broadcastHelper = null;
    }

    @Before
    public void setUp() {
        when(transactionBuilder.build(transactionData)).thenReturn(new Transaction(rawTxid, txid));
        Response broadcastResult = buildBlockchainInfoResposne(BLOCK_CHAIN_INFO);
        when(blockchainClient.broadcastTransaction(rawTxid)).thenReturn(broadcastResult);

    }

    @Test
    public void sending_successful_broadcast_to_blockchain_info() {
        Response broadcastResult = buildBlockchainInfoResposne(BLOCK_CHAIN_INFO);
        when(blockchainClient.broadcastTransaction(rawTxid)).thenReturn(broadcastResult);
        when(blockstreamClient.broadcastTransaction(rawTxid)).thenReturn(broadcastResult);

        TransactionBroadcastResult result = broadcastHelper.broadcast(transactionData);

        assertThat(result.isSuccess(), equalTo(true));
        assertThat(result.getTxId(), equalTo(txid));
    }

    @Test
    public void report_successful_broadcast() throws JSONException {
        Response broadcastResult = buildBlockchainInfoResposne(BLOCK_CHAIN_INFO);
        when(blockchainClient.broadcastTransaction(rawTxid)).thenReturn(broadcastResult);
        when(blockstreamClient.broadcastTransaction(rawTxid)).thenReturn(broadcastResult);
        ArgumentCaptor<JSONObject> proprietiesCaptor = ArgumentCaptor.forClass(JSONObject.class);

        broadcastHelper.broadcast(transactionData);

        verify(analytics).trackEvent(eq(Analytics.Companion.EVENT_BROADCAST_COMPLETE), proprietiesCaptor.capture());
        JSONObject jsonObject = proprietiesCaptor.getValue();
        assertThat(jsonObject.getInt(Analytics.Companion.EVENT_BROADCAST_JSON_KEY_BLOCK_CODE), equalTo(200));
        assertThat(jsonObject.getString(Analytics.Companion.EVENT_BROADCAST_JSON_KEY_BLOCK_MSG), equalTo("OK"));
    }

    @Test
    public void blockchain_request_failed() {
        String message = "Transaction already exists.";
        Response broadcastResult = buildFailureResponse(500, message);
        Response blockstreamResult = buildFailureResponse(400, message);
        when(blockchainClient.broadcastTransaction(rawTxid)).thenReturn(broadcastResult);
        when(blockstreamClient.broadcastTransaction(rawTxid)).thenReturn(blockstreamResult);

        TransactionBroadcastResult result = broadcastHelper.broadcast(transactionData);

        assertThat(result.isSuccess(), equalTo(false));
        assertThat(result.getTxId(), equalTo(txid));
        assertThat(result.getMessage(), equalTo(message));
    }

    @Test
    public void report_failure_broadcast() throws JSONException {
        String message = "Transaction already exists.";
        Response broadcastResult = buildFailureResponse(500, message);
        when(blockchainClient.broadcastTransaction(rawTxid)).thenReturn(broadcastResult);
        when(blockstreamClient.broadcastTransaction(rawTxid)).thenReturn(broadcastResult);
        ArgumentCaptor<JSONObject> proprietiesCaptor = ArgumentCaptor.forClass(JSONObject.class);

        broadcastHelper.broadcast(transactionData);

        verify(analytics).trackEvent(eq(Analytics.Companion.EVENT_BROADCAST_FAILED), proprietiesCaptor.capture());
        JSONObject jsonObject = proprietiesCaptor.getValue();
        assertThat(jsonObject.getInt(Analytics.Companion.EVENT_BROADCAST_JSON_KEY_BLOCK_CODE), equalTo(500));
        assertThat(jsonObject.getString(Analytics.Companion.EVENT_BROADCAST_JSON_KEY_BLOCK_MSG), equalTo(message));
    }

    private Response buildBlockchainInfoResposne(String json) {
        return Response.success(new Gson().fromJson(json, BlockchainTX.class));

    }

    private Response buildFailureResponse(int code, String message) {
        return Response.error(code, ResponseBody.create(MediaType.parse("application/json"),
                message));
    }
}