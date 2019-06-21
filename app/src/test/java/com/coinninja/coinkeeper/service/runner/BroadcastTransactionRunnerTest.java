package com.coinninja.coinkeeper.service.runner;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.bitcoin.BroadcastTransactionHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.analytics.Analytics;

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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BroadcastTransactionRunnerTest {

    @Mock
    private CoinKeeperApplication application;

    @Mock
    private TransactionBroadcastResult result;

    @Mock
    private BroadcastTransactionRunner.BroadcastListener callback;

    @Mock
    private Analytics analytics;

    @Mock
    private TransactionData transactionData;

    @Mock
    private SignedCoinKeeperApiClient apiClient;

    @Mock
    private BroadcastTransactionHelper broadcastTransactionHelper;

    @InjectMocks
    private BroadcastTransactionRunner task;

    @After
    public void tearDown() {
        task = null;
        broadcastTransactionHelper = null;
        apiClient = null;
        transactionData = null;
        analytics = null;
        callback = null;
        result = null;
        application = null;
    }

    @Before
    public void setUp() throws Exception {
        when(result.isSuccess()).thenReturn(true);
        when(apiClient.getCurrentState()).thenReturn(Response.success(null));

        task.setBroadcastListener(callback);

        when(broadcastTransactionHelper.broadcast(any())).thenReturn(result);
        when(transactionData.getPaymentAddress()).thenReturn("Some bitcoin address");
    }

    @Test
    public void clone_retains_listener() {
        task = task.clone();
        assertNotNull(task.broadcastListener);
    }

    @Test
    public void notifies_of_broadcast_started() {
        task.doInBackground(transactionData);

        verify(analytics).trackEvent(eq(Analytics.EVENT_BROADCAST_STARTED));
    }

    @Test
    public void flushes_before_ending() {
        task.onPostExecute(result);

        analytics.flush();
    }

    @Test
    public void logs_broadcast_failure_when_checking_fails() throws JSONException {
        when(apiClient.getCurrentState()).thenReturn(Response.error(500,
                ResponseBody.create(MediaType.parse("plain/text"), "")));


        task.doInBackground(transactionData);


        ArgumentCaptor<JSONObject> argumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
        verify(analytics).trackEvent(eq(Analytics.EVENT_BROADCAST_FAILED), argumentCaptor.capture());

        JSONObject properties = argumentCaptor.getValue();
        assertThat(properties.getString(Analytics.EVENT_BROADCAST_JSON_KEY_CHECK_IN_FAIL), equalTo("CheckInFail"));
    }

    @Test
    public void progress_stops_when_failure_occurs() {
        when(apiClient.getCurrentState()).thenReturn(Response.error(500,
                ResponseBody.create(MediaType.parse("plain/text"), "")));

        task.doInBackground(transactionData);

        verify(apiClient).getCurrentState();
        assertThat(task.getProgress(), equalTo(.33333334F));
    }

    @Test
    public void checks_in_with_coinninja() {
        task.doInBackground(transactionData);

        verify(apiClient).getCurrentState();
        assertThat(task.getProgress(), equalTo(1F));
    }

    @Test
    public void doInBackground() {
        TransactionBroadcastResult result = task.doInBackground(transactionData);

        verify(broadcastTransactionHelper).broadcast(transactionData);
        assertThat(result, equalTo(result));
    }

    @Test
    public void onPostExecute_broadcast_successful() {
        task.onPostExecute(result);

        verify(callback).onBroadcastSuccessful(result);
    }

    @Test
    public void update_user_profile_to_account_for_has_sent() {
        task.onPostExecute(result);

        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_SENT_ADDRESS, true);
    }

    @Test
    public void onPostExecute_broadcastFail() {
        when(result.isSuccess()).thenReturn(false);

        task.onPostExecute(result);

        verify(callback).onBroadcastError(result);
    }

    @Test
    public void broadcast_checksum_Fail() {
        String reason = "Some error message";
        when(transactionData.getPaymentAddress()).thenReturn(null);
        when(application.getString(R.string.transaction_checksum_error)).thenReturn(reason);

        task.doInBackground(transactionData);

        verify(broadcastTransactionHelper).generateFailedBroadcast(reason);
    }

}