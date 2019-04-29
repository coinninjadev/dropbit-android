package com.coinninja.coinkeeper.view.fragment;

import android.content.Intent;
import android.widget.Button;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.uri.BitcoinUriBuilder;
import com.coinninja.matchers.IntentFilterMatchers;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowToast;

import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(AndroidJUnit4.class)
public class RequestDialogFragmentTest {

    @Mock
    AccountManager accountManager;

    BitcoinUriBuilder bitcoinUriBuilder = new BitcoinUriBuilder();

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    @Mock
    WalletHelper walletHelper;

    @Mock
    LazyList transactions;

    @Mock
    ClipboardUtil clipboardUtil;

    private String testAddress = "jd9dj9sdjd9jdf0swhje";

    private RequestDialogFragment fragment;

    private ActivityScenario<TransactionHistoryActivity> scenario;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        application.walletHelper = walletHelper;
        application.accountManager = accountManager;
        application.clipboardUtil = clipboardUtil;
        application.localBroadCastUtil = localBroadCastUtil;

        when(accountManager.getNextReceiveAddress()).thenReturn(testAddress);
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);

        scenario = ActivityScenario.launch(TransactionHistoryActivity.class);
    }

    @After
    public void tearDown() {
        fragment = null;
        clipboardUtil = null;
        transactions = null;
        localBroadCastUtil = null;
        bitcoinUriBuilder = null;
        accountManager = null;
        testAddress = null;
        scenario.close();
    }

    private void start() {
        fragment = new RequestDialogFragment();
        scenario.onActivity(activity -> {
            fragment.show(activity.getSupportFragmentManager(), RequestDialogFragment.class.getName());
        });
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    @Test
    public void sets_copy_button_text_with_next_receive_address() {
        start();
        Button button = fragment.getView().findViewById(R.id.request_copy_button);

        assertThat(button.getText().toString(), equalTo(testAddress));
    }

    @Test
    public void selecting_copy_button_puts_address_in_clipboard() {
        start();

        assertNotNull(fragment.getView());
        clickOn(withId(fragment.getView(), R.id.request_copy_button));

        verify(clipboardUtil).setClipFromText("Bitcoin Address", "bitcoin:jd9dj9sdjd9jdf0swhje");
    }

    @Test
    public void shows_message_informing_user_that_address_copied() {
        start();
        fragment.getView().findViewById(R.id.request_copy_button).performClick();

        String textOfLatestToast = ShadowToast.getTextOfLatestToast();
        assertThat(textOfLatestToast, equalTo(fragment.getContext().getString(R.string.request_copied_message)));
    }

    @Test
    public void clicking_on_request_shows_chooser() {
        start();

        fragment.getView().findViewById(R.id.request_funds).performClick();

        Intents.intending(hasAction(Intent.ACTION_SEND));
        Intents.intending(hasExtraWithKey(Intent.EXTRA_TEXT));
        Intents.intending(hasExtra(Intent.EXTRA_TEXT, "bitcoin:jd9dj9sdjd9jdf0swhje"));
        Intents.intending(hasExtraWithKey(Intent.EXTRA_SUBJECT));
        Intents.intending(hasExtra(Intent.EXTRA_SUBJECT, "Request Bitcoin"));
        Intents.intending(hasExtraWithKey("subject"));
        Intents.intending(hasExtra("subject", "Request Bitcoin"));
        Intents.intending(hasExtraWithKey("sms_body"));
        Intents.intending(hasExtra("sms_body", "Request Bitcoin"));
    }


    @Test
    public void starts_qr_service_to_generate_qr_code() {
        start();

        Intents.intending(hasComponent(DropbitIntents.EXTRA_TEMP_QR_SCAN));
        Intents.intending(hasExtraWithKey(DropbitIntents.EXTRA_TEMP_QR_SCAN));
        Intents.intending(hasExtra(DropbitIntents.EXTRA_TEMP_QR_SCAN, "bitcoin:jd9dj9sdjd9jdf0swhje"));
    }

    @Test
    public void registers_for_qr_broadcast_when_resumed() {
        start();
        assertThat(fragment.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_VIEW_QR_CODE));
        verify(localBroadCastUtil).registerReceiver(fragment.receiver, fragment.intentFilter);
    }

    @Test
    public void unregisters_for_qr_broadcast_when_paused() {
        start();

        fragment.onPause();
        fragment.onStop();

        verify(localBroadCastUtil).unregisterReceiver(fragment.receiver);
    }


}