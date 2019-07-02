package com.coinninja.coinkeeper.service.client;

public class MockAPIData {
    public static final String PRICING = "{\n" +
            "\"blockheight\": 518631,\n" +
            "\"fees\": {\n" +
            "\"slow\": 40.1,\n" +
            "\"med\": 20.1,\n" +
            "\"fast\": 10\n" +
            "},\n" +
            "\"pricing\": {\n" +
            "\"ask\": 418.79,\n" +
            "\"bid\": 418.35,\n" +
            "\"last\": 418.66,\n" +
            "\"high\": 418.83,\n" +
            "\"low\": 417.1,\n" +
            "\"open\": {},\n" +
            "\"averages\": {},\n" +
            "\"volume\": 56542.49,\n" +
            "\"changes\": {},\n" +
            "\"volume_percent\": 66.42,\n" +
            "\"timestamp\": 1458754392,\n" +
            "\"display_timestamp\": \"Wed, 23 Mar 2016 17:33:12 +0000\"\n" +
            "}\n" +
            "}";

    public static final String ADDRESS_STATS = "{\n" +
            "\"address\": \"1Gy2Ast7uT13wQByPKs9Vi9Qj1BVcARgVQ\",\n" +
            "\"balance\": 0,\n" +
            "\"received\": 2058617,\n" +
            "\"spent\": 2058617,\n" +
            "\"tx_count\": 2\n" +
            "}";

    public static final String ADDRESS_TRANSACTIONS = "[\n" +
            "   {\n" +
            "       \"address\": \"1Gy2Ast7uT13wQByPKs9Vi9Qj1BVcARgVQ\",\n" +
            "       \"txid\": \"f231aaf68aff1e0957d3c9eb668772d6bb249f07a3176cc3c9c99dbe5e960f83\",\n" +
            "       \"time\": 1520972149,\n" +
            "       \"vin\": 2058617,\n" +
            "       \"vout\": 0\n" +
            "   }\n" +
            "]";

    public static final String ADDRESS_TWO_TRANSACTIONS = "[\n" +
            "   {\n" +
            "       \"address\": \"3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX\",\n" +
            "       \"txid\": \"1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24\",\n" +
            "       \"time\": 1521560498,\n" +
            "       \"vin\": 507070,\n" +
            "       \"vout\": 0\n" +
            "   },\n" +
            "   {\n" +
            "       \"address\": \"3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX\",\n" +
            "       \"txid\": \"9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57\",\n" +
            "       \"time\": 1520526378,\n" +
            "       \"vin\": 0,\n" +
            "       \"vout\": 507070\n" +
            "   }\n" +
            "]";

    public static final String ADDRESS_QUERY_RESPONSE__PAGE_1 = "[\n" +
            "   {\n" +
            "       \"address\": \"1Gy2Ast7uT13wQByPKs9Vi9Qj1BVcARgVQ\",\n" +
            "       \"txid\": \"f231aaf68aff1e0957d3c9eb668772d6bb249f07a3176cc3c9c99dbe5e960f83\",\n" +
            "       \"time\": 1520972149,\n" +
            "       \"vin\": 2058617,\n" +
            "       \"vout\": 0\n" +
            "   },\n" +
            "   {\n" +
            "       \"address\": \"3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX\",\n" +
            "       \"txid\": \"1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24\",\n" +
            "       \"time\": 1521560498,\n" +
            "       \"vin\": 507070,\n" +
            "       \"vout\": 0\n" +
            "   },\n" +
            "   {\n" +
            "       \"address\": \"3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX\",\n" +
            "       \"txid\": \"9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57\",\n" +
            "       \"time\": 1520526378,\n" +
            "       \"vin\": 0,\n" +
            "       \"vout\": 507070\n" +
            "   }\n" +
            "]";

    public static final String TRANSACTION_STATS = "{\n" +
            "       \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
            "       \"coinbase\": false,\n" +
            "       \"confirmations\": 470,\n" +
            "       \"fees_rate\": 1015821,\n" +
            "       \"fees\": 170658,\n" +
            "       \"miner\": \"GBMiners\",\n" +
            "       \"vin_value\": 180208833,\n" +
            "       \"vout_value\": 179858833\n" +
            "   }";

    public static final String TRANSACTION_DETAIL =
            "{\n" +
                    "   \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
                    "   \"hash\": \"ee90a9ec4bbcf1ab327a6489a74a393b85515bc5bf8d308a3201b19974445276\",\n" +
                    "   \"size\": 249,\n" +
                    "   \"vsize\": 168,\n" +
                    "   \"weight\": 669,\n" +
                    "   \"version\": 1,\n" +
                    "   \"locktime\": 0,\n" +
                    "   \"coinbase\": false,\n" +
                    "   \"txinwitness\": [ " +
                    "           \"304402204dcaba494328bd472f4bf61761e43c9ca204ea81ce9c5c57d669e4ed4721499f022007a6024b0f5e202a7f38bb90edbecaa788e276239a12aa42d958818d52db3f9f01\",\n" +
                    "           \"036ebf6ab96773a9fa7997688e1712ddc9722ef9274220ba406cb050ac5f1a1306\"\n" +
                    "   ],\n" +
                    "   \"blockhash\": \"0000000000000000007aba266efd9aedfc005b69539bf077d1eaffb4a5fb9272\",\n" +
                    "   \"height\": 1,\n" +
                    "   \"confirmations\": 470,\n" +
                    "   \"time\": 1514906608,\n" +
                    "   \"blocktime\": 1524906608,\n" +
                    "   \"vin\": [\n" +
                    "         {\n" +
                    "               \"txid\": \"69151603ebe4192d50c1aaaca4e0ab0ea335184e261376c2eda64c35ce9fd1b5\",\n" +
                    "               \"vout\": 1,\n" +
                    "               \"scriptSig\": {\n" +
                    "                  \"asm\": \"00142f0908d7a15b75bfacb22426b5c1d78f545a683f\",\n" +
                    "                  \"hex\": \"1600142f0908d7a15b75bfacb22426b5c1d78f545a683f\"\n" +
                    "         },\n" +
                    "         \"txinwitness\": [\n" +
                    "               \"304402204dcaba494328bd472f4bf61761e43c9ca204ea81ce9c5c57d669e4ed4721499f022007a6024b0f5e202a7f38bb90edbecaa788e276239a12aa42d958818d52db3f9f01\",\n" +
                    "               \"036ebf6ab96773a9fa7997688e1712ddc9722ef9274220ba406cb050ac5f1a1306\"\n" +
                    "         ],\n" +
                    "         \"sequence\": 4294967295,\n" +
                    "         \"previousoutput\": {\n" +
                    "               \"value\": 999934902,\n" +
                    "               \"n\": 1,\n" +
                    "               \"scriptPubKey\": {\n" +
                    "                     \"asm\": \"OP_HASH160 4f7728b2a54dc9a2b44e47341e7e029bb99c7d72 OP_EQUAL\",\n" +
                    "                     \"hex\": \"a9144f7728b2a54dc9a2b44e47341e7e029bb99c7d7287\",\n" +
                    "                     \"reqSigs\": 1,\n" +
                    "                     \"type\": \"scripthash\",\n" +
                    "                     \"addresses\": [\n" +
                    "                            \"38wC41V2tNZrr2uiwUthn41b2M8SLGMVRt\"\n" +
                    "                    ]\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"vout\": [\n" +
                    "        {\n" +
                    "            \"value\": 100000000,\n" +
                    "            \"n\": 0,\n" +
                    "            \"scriptPubKey\": {\n" +
                    "                    \"asm\": \"OP_DUP OP_HASH160 54aac92eb2398146daa547d921ed29a63891a769 OP_EQUALVERIFY OP_CHECKSIG\",\n" +
                    "                    \"hex\": \"76a91454aac92eb2398146daa547d921ed29a63891a76988ac\",\n" +
                    "                    \"reqSigs\": 1,\n" +
                    "                    \"type\": \"pubkeyhash\",\n" +
                    "                    \"addresses\": []\n" +
                    "            }\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"value\": 899764244,\n" +
                    "            \"n\": 1,\n" +
                    "            \"scriptPubKey\": {}\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
}

