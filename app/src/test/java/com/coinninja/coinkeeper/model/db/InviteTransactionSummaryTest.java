package com.coinninja.coinkeeper.model.db;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class InviteTransactionSummaryTest {
    private InviteTransactionSummary transactionSummary;

    @Before
    public void setUp() throws Exception {
        transactionSummary = new CustomMockInviteTransactionSummary();
    }

    @Test
    public void prove_the_custom_mock_is_touching_the_real_InviteTransactionSummary_class_test() {
        String sampleTxId = "some tx id";

        transactionSummary.setBtcTransactionId(sampleTxId);//this setter dose NOT exist in the mock
        String currentTxId = transactionSummary.getBtcTransactionId();

        assertThat(currentTxId, equalTo(sampleTxId));
    }

    @Test
    public void set_tx_id_to_real_value_then_try_to_set_it_empty_test() {
        //setting txID to empty should never work, InviteTransactionSummary should protect it's self
        String sampleTxId = "some tx id";

        transactionSummary.setBtcTransactionId(sampleTxId);
        String currentTxId = transactionSummary.getBtcTransactionId();

        assertThat(currentTxId, equalTo(sampleTxId));//prove its set to a non empty value
        transactionSummary.setBtcTransactionId("");//then try to set it empty
        String newTxId = transactionSummary.getBtcTransactionId();


        //txid should still be sampleId and not empty
        //new txid should be the same as the old
        assertFalse(newTxId.isEmpty());
        assertThat(newTxId, equalTo(sampleTxId));
        assertThat(newTxId, equalTo(currentTxId));
    }

    @Test
    public void set_tx_id_to_real_value_then_try_to_set_it_null_test() {
        //setting txID to null should never work, InviteTransactionSummary should protect it's self
        String sampleTxId = "some tx id";

        transactionSummary.setBtcTransactionId(sampleTxId);
        String currentTxId = transactionSummary.getBtcTransactionId();

        assertThat(currentTxId, equalTo(sampleTxId));//prove its set to a not null value
        transactionSummary.setBtcTransactionId(null);//then try to set it null
        String newTxId = transactionSummary.getBtcTransactionId();


        //txid should still be sampleId and not null
        //new txid should be the same as the old
        assertFalse(newTxId == null);
        assertThat(newTxId, equalTo(sampleTxId));
        assertThat(newTxId, equalTo(currentTxId));
    }


    //this custom mock will use @Override's to touch the real underline classes code
    class CustomMockInviteTransactionSummary extends InviteTransactionSummary {

        private String txId;


        //the real InviteTransactionSummary will call this method and this mock will catch it and set its local var
        @Override
        protected void setTxId(String btcTransactionId) {
            //the unit test should Never call setTxId directly.
            //if this method is ever called that means the real InviteTransactionSummary called it
            txId = btcTransactionId;
        }

        @Override
        public String getBtcTransactionId() {
            return txId;
        }
    }
}