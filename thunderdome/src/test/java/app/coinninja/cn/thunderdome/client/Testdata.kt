package app.coinninja.cn.thunderdome.client

object Testdata {
    const val account: String = "{\n" +
            "  \"id\": \"--id--\",\n" +
            "  \"created_at\": \"2019-08-16T15:26:10.036Z\",\n" +
            "  \"updated_at\": \"2019-08-16T17:26:10.036Z\",\n" +
            "  \"address\": \"--address--\",\n" +
            "  \"balance\": 123456789,\n" +
            "  \"pending_in\": 1000,\n" +
            "  \"pending_out\": 500\n" +
            "}"

    const val invoices: String = "{\n" +
            "  \"ledger\": [\n" +
            "    {\n" +
            "      \"created_at\": \"2019-08-22T22:34:46.481530Z\",\n" +
            "      \"updated_at\": \"2019-08-22T22:41:42.903254Z\",\n" +
            "      \"id\": \"3228fe64710af215840ec2fa96a0cbde4ca9dd15e6931743fe68d0259cba432d:0\",\n" +
            "      \"status\": \"completed\",\n" +
            "      \"type\": \"btc\",\n" +
            "      \"direction\": \"in\",\n" +
            "      \"value\": 476190,\n" +
            "      \"network_fee\": 10,\n" +
            "      \"processing_fee\": 200,\n" +
            "      \"memo\": \"\",\n" +
            "      \"request\": \"\",\n" +
            "      \"error\": \"\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"created_at\": \"2019-08-22T22:39:38.107282Z\",\n" +
            "      \"updated_at\": \"2019-08-22T22:41:42.915568Z\",\n" +
            "      \"id\": \"a33d41727be679d425b8f3a84dd3620822b3f4c0e44c1c9dcdcb420521ac1987:0\",\n" +
            "      \"status\": \"completed\",\n" +
            "      \"type\": \"btc\",\n" +
            "      \"direction\": \"in\",\n" +
            "      \"value\": 47619,\n" +
            "      \"network_fee\": 0,\n" +
            "      \"processing_fee\": 0,\n" +
            "      \"memo\": \"\",\n" +
            "      \"request\": \"\",\n" +
            "      \"error\": \"\"\n" +
            "    }\n" +
            "  ]\n" +
            "}"

    const val withdrawalRequest: String = "{\n" +
            "  \"result\": {\n" +
            "    \"id\": \"--txid--\",\n" +
            "    \"account_id\": \"--account-id--\",\n" +
            "    \"created_at\": \"2019-09-03T14:41:30.018Z\",\n" +
            "    \"updated_at\": \"2019-09-03T14:41:30.018Z\",\n" +
            "    \"expires_at\": \"2019-09-03T14:41:30.018Z\",\n" +
            "    \"status\": \"PENDING\",\n" +
            "    \"type\": \"BTC\",\n" +
            "    \"direction\": \"OUT\",\n" +
            "    \"generated\": true,\n" +
            "    \"value\": 10000,\n" +
            "    \"network_fee\": 50,\n" +
            "    \"processing_fee\": 500,\n" +
            "    \"add_index\": \"0\",\n" +
            "    \"memo\": \"\",\n" +
            "    \"request\": \"\",\n" +
            "    \"error\": \"\",\n" +
            "    \"hidden\": false\n" +
            "  }\n" +
            "}"

}
