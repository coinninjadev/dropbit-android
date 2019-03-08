package com.coinninja.coinkeeper.model.encryptedpayload.v1;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TransactionNotificationV1Test {

    @Test
    public void serializes_to_json_string() {
        TransactionNotificationV1 transactionNotificationV1 = new TransactionNotificationV1();
        InfoV1 infoV1 = new InfoV1();
        infoV1.setAmount(10000L);
        infoV1.setCurrency("USD");
        infoV1.setMemo("---memo---");
        MetaV1 metaV1 = new MetaV1();
        metaV1.setVersion(1);
        ProfileV1 profileV1 = new ProfileV1();
        profileV1.setAvatar("--avatar--");
        profileV1.setCountryCode(1);
        profileV1.setHandle("--handle--");
        profileV1.setPhoneNumber("3305551111");
        transactionNotificationV1.setMeta(metaV1);
        transactionNotificationV1.setTxid("--txid--");
        transactionNotificationV1.setInfo(infoV1);

        assertThat(transactionNotificationV1.toString(),
                equalTo("{\"meta\":{\"version\":1},\"txid\":\"--txid--\",\"info\":{\"memo\":\"---memo---\",\"amount\":10000,\"currency\":\"USD\"}}"));
    }


}