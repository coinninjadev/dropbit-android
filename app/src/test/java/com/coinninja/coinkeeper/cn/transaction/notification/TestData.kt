package com.coinninja.coinkeeper.cn.transaction.notification

class TestData {
    companion object {
        val V1_Payload_Decrypted = "{\n" +
                "     \"meta\": {\n" +
                "       \"version\": 1\n" +
                "     },  \n" +
                "     \"txid\": \"--txid--\",\n" +
                "     \"info\": {\n" +
                "       \"memo\": \"Here's your 5 dollars \uD83D\uDCB8\",\n" +
                "       \"amount\": 500,\n" +
                "       \"currency\": \"USD\"\n" +
                "     },  \n" +
                "     \"profile\": {\n" +
                "       \"display_name\": \"Bob\", \n" +
                "       \"country_code\": 1,\n" +
                "       \"phone_number\": \"3305551122\",\n" +
                "       \"dropbit_me\": \"bob-handle\", \n" +
                "       \"avatar\": \"aW5zZXJ0IGF2YXRhciBoZXJlCg==\"\n" +
                "     }   \n" +
                "   }   "
        val V2_Payload_Decrypted_Twitter = "{\n" +
                "  \"meta\": {\n" +
                "    \"version\": 2\n" +
                "  },\n" +
                "  \"txid\": \"--txid--\",\n" +
                "  \"info\": {\n" +
                "    \"memo\": \"Here's your 5 dollars \uD83D\uDCB8\",\n" +
                "    \"amount\": 500,\n" +
                "    \"currency\": \"USD\"\n" +
                "  },\n" +
                "  \"profile\": {\n" +
                "    \"type\": \"twitter\",\n" +
                "    \"identity\": \"123456789:aliceandbob\",\n" +
                "    \"display_name\": \"Alice Bob\",\n" +
                "    \"dropbit_me\": \"dropbit.me/@aliceandbob\",\n" +
                "    \"avatar\": \"aW5zZXJ0IGF2YXRhciBoZXJlCg==\"\n" +
                "  }\n" +
                "}\n"

        val V2_Payload_Decrypted_Phone = "{\n" +
                "  \"meta\": {\n" +
                "    \"version\": 2\n" +
                "  },\n" +
                "  \"txid\": \"--txid--\",\n" +
                "  \"info\": {\n" +
                "    \"memo\": \"Here's your 5 dollars \uD83D\uDCB8\",\n" +
                "    \"amount\": 500,\n" +
                "    \"currency\": \"USD\"\n" +
                "  },\n" +
                "  \"profile\": {\n" +
                "    \"type\": \"phone\",\n" +
                "    \"identity\": \"+13305551122\",\n" +
                "    \"display_name\": \"Alice Bob\",\n" +
                "    \"dropbit_me\": \"dropbit.me/@aliceandbob\",\n" +
                "    \"avatar\": \"aW5zZXJ0IGF2YXRhciBoZXJlCg==\"\n" +
                "  }\n" +
                "}\n"
    }
}