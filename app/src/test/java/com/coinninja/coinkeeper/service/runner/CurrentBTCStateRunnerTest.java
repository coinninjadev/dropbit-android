package com.coinninja.coinkeeper.service.runner;

import android.content.Intent;

import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.CurrentState;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CurrentBTCStateRunnerTest {

    private static final int NUMBER_OF_LOCAL_BROADCASTS = 2;

    private TransactionFee transactionFee;

    @Mock
    private SignedCoinKeeperApiClient client;

    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @Mock
    private CurrentState currentState;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private CNLogger logger;

    @InjectMocks
    private CurrentBTCStateRunner runner;
    private USDCurrency latestPrice;
    private USDCurrency latestCachedPrice;

    @Before
    public void setUp() {
        transactionFee = new TransactionFee(10l, 20l, 30l);
        MockitoAnnotations.initMocks(this);
        latestPrice = new USDCurrency(25.00d);
        latestCachedPrice = new USDCurrency(latestPrice.toLong());
        when(currentState.getLatestPrice()).thenReturn(latestPrice);
        when(currentState.getFees()).thenReturn(transactionFee);
        when(currentState.getBlockheight()).thenReturn(525804);
        when(client.getCurrentState()).thenReturn(Response.success(currentState));
        when(walletHelper.getLatestPrice()).thenReturn(latestCachedPrice);
    }

    @Test
    public void notifies_of_block_tip() {
        runner.run();

        verify(walletHelper).updateBlockHeight(currentState.getBlockheight());
    }

    @Test
    public void saves_current_fee_to_db() {
        runner.run();

        verify(walletHelper).setLatestFee(transactionFee);
    }

    @Test
    public void broadcasts_current_fees() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);

        runner.run();

        verify(localBroadCastUtil, times(NUMBER_OF_LOCAL_BROADCASTS)).sendBroadcast(argumentCaptor.capture());

        Intent intent = argumentCaptor.getAllValues().get(1);

        assertThat(intent.getAction(), equalTo(DropbitIntents.ACTION_TRANSACTION_FEE_UPDATE));
        assertThat(intent.getExtras().getParcelable(DropbitIntents.EXTRA_TRANSACTION_FEE), equalTo(transactionFee));
    }

    @Test
    public void saves_current_price_to_db() {
        runner.run();

        verify(walletHelper).setLatestPrice(currentState.getLatestPrice());
    }

    @Test
    public void broadcasts_current_price() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);

        runner.run();

        verify(localBroadCastUtil, times(NUMBER_OF_LOCAL_BROADCASTS)).sendBroadcast(argumentCaptor.capture());

        Intent intent = argumentCaptor.getAllValues().get(0);
        verify(localBroadCastUtil, times(2)).sendBroadcast(argumentCaptor.capture());

        assertThat(intent.getAction(), equalTo(DropbitIntents.ACTION_BTC_PRICE_UPDATE));
        assertThat(intent.getExtras().getLong(DropbitIntents.EXTRA_BITCOIN_PRICE), equalTo(2500L));
    }

    @Test
    public void does_not_notify_state_change_on_error() {
        ResponseBody body = ResponseBody.create(MediaType.parse("text/plain"), "");
        Response error = Response.error(500, body);
        when(client.getCurrentState()).thenReturn(error);

        runner.run();

        verify(localBroadCastUtil, times(0)).sendBroadcast(anyString());
        verify(localBroadCastUtil, times(0)).sendBroadcast(any(Intent.class));
        verify(logger).logError(CurrentBTCStateRunner.TAG, CurrentBTCStateRunner.ERROR_MESSAGE, error);
    }

    @Test
    public void requests_current_state_from_CN() {
        runner.run();

        verify(client).getCurrentState();
    }

}