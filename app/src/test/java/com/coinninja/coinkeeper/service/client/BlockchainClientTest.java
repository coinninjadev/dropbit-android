package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.service.client.model.BlockchainTX;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BlockchainClientTest {

    private MockWebServer server;
    private BlockchainClient apiClient;

    @Before
    public void setUp() {
        server = new MockWebServer();
        apiClient = BlockchainClient.newInstance(server.url("").toString());
    }

    @After
    public void tearDown() {
        try {
            server.shutdown();
        } catch (Exception e) {

        }
    }

    @Test
    public void api_client_fetches_transaction_data() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(TX));

        Response response = apiClient.getTransactionFor(TXID);

        assertThat(response.code(), equalTo(200));
        BlockchainTX transaction = (BlockchainTX) response.body();
        assertThat(transaction.getHash(), equalTo(TXID));
    }

    public static final String TXID = "7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03";
    public static final String TX =
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