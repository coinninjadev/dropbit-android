package com.coinninja.coinkeeper.bitcoin;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionBuilder;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.model.Transaction;
import com.coinninja.coinkeeper.service.client.BlockchainClient;
import com.coinninja.coinkeeper.service.client.model.BlockchainTX;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BroadcastTransactionHelperTest {
    String BLOCK_CHAIN_INFO =
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
    private BlockchainClient blockchainClient;
    @Mock
    private TransactionData transactionData;

    @InjectMocks
    private BroadcastTransactionHelper broadcastHelper;

    @After
    public void tearDown() {
        BLOCK_CHAIN_INFO = null;
        analytics = null;
        transactionBuilder = null;
        blockchainClient = null;
        transactionData = null;
        broadcastHelper = null;
    }

    @Test
    @Ignore
    public void blockchainInfo_failed_but_libbitcoin_worked_so_use_libbitcoin_data_test() {
        String txID = "SOME TXID";
        TransactionBroadcastResult mockLibbitcoinResult = new TransactionBroadcastResult(200, true, "", new Transaction("", txID));
        TransactionData transactionData = mock(TransactionData.class);
        String mockRawTX = "RAW TX DATA 000000000djs9jds9js9jds";
        when(transactionBuilder.buildAndBroadcast(transactionData)).thenReturn(mockLibbitcoinResult);
        when(transactionBuilder.build(transactionData)).thenReturn(new Transaction(mockRawTX, txID));
        when(transactionBuilder.buildAndBroadcast(transactionData)).thenReturn(mockLibbitcoinResult);

        Response blockchainInfoResposneBad = getBadResponse(400, "");
        when(blockchainClient.broadcastTransaction(mockRawTX)).thenReturn(blockchainInfoResposneBad);

        TransactionBroadcastResult result = broadcastHelper.broadcast(transactionData);

        assertThat(mockLibbitcoinResult, equalTo(result));
    }

    @Test
    public void blockchainInfo_worked_and_libbitcoin_worked_so_use_blockchainInfo_data_test() {
        String txID = "SOME TXID";
        TransactionBroadcastResult mockLibbitcoinResult = new TransactionBroadcastResult(200, true, "", new Transaction("", txID));
        TransactionData transactionData = mock(TransactionData.class);
        String mockRawTX = "RAW TX DATA 000000000djs9jds9js9jds";
        when(transactionBuilder.buildAndBroadcast(transactionData)).thenReturn(mockLibbitcoinResult);
        when(transactionBuilder.build(transactionData)).thenReturn(new Transaction(mockRawTX, txID));
        when(transactionBuilder.buildAndBroadcast(transactionData)).thenReturn(mockLibbitcoinResult);
        Response blockchainInfoResposneGood = buildBlockchainInfoResposne(BLOCK_CHAIN_INFO);
        when(blockchainClient.broadcastTransaction(mockRawTX)).thenReturn(blockchainInfoResposneGood);


        TransactionBroadcastResult result = broadcastHelper.broadcast(transactionData);


        assertThat(result.getTxId(), equalTo("SOME TXID"));
    }

    @Test
    public void blockchainInfo_worked_and_libbitcoin_failed_so_use_blockchainInfo_data_test() {
        String txID = "SOME TXID";
        TransactionBroadcastResult mockLibbitcoinResult = new TransactionBroadcastResult(200, true, "", new Transaction("", txID));
        TransactionData transactionData = mock(TransactionData.class);
        String mockRawTX = "RAW TX DATA 000000000djs9jds9js9jds";
        when(transactionBuilder.buildAndBroadcast(transactionData)).thenReturn(mockLibbitcoinResult);
        when(transactionBuilder.build(transactionData)).thenReturn(new Transaction(mockRawTX, txID));
        when(transactionBuilder.buildAndBroadcast(transactionData)).thenReturn(mockLibbitcoinResult);
        Response blockchainInfoResposneGood = buildBlockchainInfoResposne(BLOCK_CHAIN_INFO);
        when(blockchainClient.broadcastTransaction(mockRawTX)).thenReturn(blockchainInfoResposneGood);


        TransactionBroadcastResult result = broadcastHelper.broadcast(transactionData);


        assertThat(result.getTxId(), equalTo("SOME TXID"));
    }


    @Test
    @Ignore
    public void report_libbitcoin_failed_test() throws JSONException {
        ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<JSONObject> proprietiesCaptor = ArgumentCaptor.forClass(JSONObject.class);

        Transaction transaction = new Transaction("SOME TXID", "RAW TX DATA 000000000djs9jds9js9jds");
        TransactionBroadcastResult mockLibbitcoinResult =
                new TransactionBroadcastResult(500, false, "some error Libbitcoin", transaction);
        when(transactionBuilder.buildAndBroadcast(transactionData)).thenReturn(mockLibbitcoinResult);
        when(transactionBuilder.build(transactionData)).thenReturn(transaction);
        Response blockchainInfoResposneBad = getBadResponse(500, "some error blockchain");
        when(blockchainClient.broadcastTransaction(transaction.getRawTx())).thenReturn(blockchainInfoResposneBad);

        TransactionBroadcastResult result = broadcastHelper.broadcast(transactionData);

        assertThat(result.isSuccess(), equalTo(false));
        verify(analytics).trackEvent(eventCaptor.capture(), proprietiesCaptor.capture());
        assertThat(eventCaptor.getValue(), equalTo("BroadcastFailure"));
        JSONObject jsonObject = proprietiesCaptor.getValue();
        assertThat(jsonObject.getInt(Analytics.EVENT_BROADCAST_JSON_KEY_LIB_CODE), equalTo(500));
        assertThat(jsonObject.getString(Analytics.EVENT_BROADCAST_JSON_KEY_LIB_MSG), equalTo("some error Libbitcoin"));
    }

    @Test
    @Ignore
    public void report_blockchain_failed_test() throws JSONException {
        ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<JSONObject> proprietiesCaptor = ArgumentCaptor.forClass(JSONObject.class);

        String txID = "SOME TXID";
        String mockRawTX = "RAW TX DATA 000000000djs9jds9js9jds";
        TransactionBroadcastResult mockLibbitcoinResult = new TransactionBroadcastResult(500, false, "some error Libbitcoin", new Transaction(mockRawTX, txID));
        TransactionData transactionData = mock(TransactionData.class);
        when(transactionBuilder.buildAndBroadcast(transactionData)).thenReturn(mockLibbitcoinResult);
        when(transactionBuilder.build(transactionData)).thenReturn(new Transaction(mockRawTX, txID));
        Response blockchainInfoResposneBad = getBadResponse(500, "some error blockchain");
        when(blockchainClient.broadcastTransaction(mockRawTX)).thenReturn(blockchainInfoResposneBad);


        TransactionBroadcastResult result = broadcastHelper.broadcast(transactionData);

        assertThat(result.isSuccess(), equalTo(false));
        verify(analytics).trackEvent(eventCaptor.capture(), proprietiesCaptor.capture());
        assertThat(eventCaptor.getValue(), equalTo("BroadcastFailure"));
        JSONObject jsonObject = proprietiesCaptor.getValue();
        assertThat(jsonObject.getInt(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_CODE), equalTo(500));
        assertThat(jsonObject.getString(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_MSG), equalTo("some error blockchain"));
    }

    @Test
    @Ignore
    public void report_libbitcoin_successful_to_mixpanel_test() throws JSONException {
        ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<JSONObject> proprietiesCaptor = ArgumentCaptor.forClass(JSONObject.class);
        TransactionData transactionData = new TransactionData();
        //libbitcoin setup
        String txID = "SOME TXID";
        String mockRawTX = "RAW TX DATA 000000000djs9jds9js9jds";
        int libbitcoinResponseCode = 200;
        boolean libbitcoinIsSuccess = true;
        String libbitcoinMessage = "some Libbitcoin success";
        setupLibbitcoinResponse(libbitcoinResponseCode, libbitcoinIsSuccess, libbitcoinMessage, mockRawTX, txID, transactionData);
        //blockchain.info
        Response blockChainInfoResponseGood = buildBlockchainInfoResposne(BLOCK_CHAIN_INFO);
        when(blockchainClient.broadcastTransaction(mockRawTX)).thenReturn(blockChainInfoResponseGood);


        TransactionBroadcastResult result = broadcastHelper.broadcast(transactionData);


        assertThat(result.isSuccess(), equalTo(true));
        verify(analytics).trackEvent(eventCaptor.capture(), proprietiesCaptor.capture());
        assertThat(eventCaptor.getValue(), equalTo("BroadcastSuccess"));
        JSONObject jsonObject = proprietiesCaptor.getValue();
        assertThat(jsonObject.getInt(Analytics.EVENT_BROADCAST_JSON_KEY_LIB_CODE), equalTo(200));
        assertThat(jsonObject.getString(Analytics.EVENT_BROADCAST_JSON_KEY_LIB_MSG), equalTo("some Libbitcoin success"));
    }

    @Test
    @Ignore
    public void report_blockchain_successful_to_mixpanel_test() throws JSONException {
        ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<JSONObject> proprietiesCaptor = ArgumentCaptor.forClass(JSONObject.class);
        TransactionData transactionData = new TransactionData();
        //libbitcoin setup
        String txID = "SOME TXID";
        String mockRawTX = "RAW TX DATA 000000000djs9jds9js9jds";
        int libbitcoinResponseCode = 200;
        boolean libbitcoinIsSuccess = true;
        String libbitcoinMessage = "some Libbitcoin success";
        setupLibbitcoinResponse(libbitcoinResponseCode, libbitcoinIsSuccess, libbitcoinMessage, mockRawTX, txID, transactionData);
        //blockchain.info
        Response blockChainInfoResponseGood = buildBlockchainInfoResposne(BLOCK_CHAIN_INFO);
        when(blockchainClient.broadcastTransaction(mockRawTX)).thenReturn(blockChainInfoResponseGood);

        TransactionBroadcastResult result = broadcastHelper.broadcast(transactionData);


        assertThat(result.isSuccess(), equalTo(true));
        verify(analytics).trackEvent(eventCaptor.capture(), proprietiesCaptor.capture());
        assertThat(eventCaptor.getValue(), equalTo("BroadcastSuccess"));
        JSONObject jsonObject = proprietiesCaptor.getValue();
        assertThat(jsonObject.getInt(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_CODE), equalTo(200));
        assertThat(jsonObject.getString(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_MSG), equalTo("OK"));
    }

    private void setupLibbitcoinResponse(int libbitcoinResponseCode,
                                         boolean libbitcoinIsSuccess, String libbitcoinMessage, String mockRawTX, String
                                                 txID, TransactionData transactionData) {
        TransactionBroadcastResult mockLibbitcoinResult = new TransactionBroadcastResult(libbitcoinResponseCode, libbitcoinIsSuccess, libbitcoinMessage, new Transaction(mockRawTX, txID));
        when(transactionBuilder.buildAndBroadcast(transactionData)).thenReturn(mockLibbitcoinResult);
        when(transactionBuilder.build(transactionData)).thenReturn(new Transaction(mockRawTX, txID));
    }

    @Test
    public void generate_failed_broadcast_test() {
        String sampleFailedReasonMessage = "SOME ERROR MESSAGE";

        TransactionBroadcastResult result = broadcastHelper.generateFailedBroadcast(sampleFailedReasonMessage);

        assertThat(result.isSuccess(), equalTo(false));
        assertThat(result.getMessage(), equalTo(sampleFailedReasonMessage));
    }

    @Test
    public void generate_successful_broadcast_test() {
        String sampleTxid = "SOME TX ID -- dkjs09jds0ind9inmsokkjhw9u";

        TransactionBroadcastResult result = broadcastHelper.generateSuccessfulBroadcast(new Transaction("", sampleTxid), "Successful", 200);

        assertThat(result.isSuccess(), equalTo(true));
        assertThat(result.getTxId(), equalTo(sampleTxid));
    }

    private Response buildBlockchainInfoResposne(String json) {
        return Response.success(new Gson().fromJson(json, BlockchainTX.class));

    }

    private Response getBadResponse(int code, String message) {
        return Response.error(code, ResponseBody.create(MediaType.parse("application/json"),
                message));
    }
}