package com.coinninja.coinkeeper.view.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.util.uri.BitcoinUriBuilder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class RequestDialogFragmentTest {

    @Mock
    AccountManager accountManager;

    BitcoinUriBuilder bitcoinUriBuilder = new BitcoinUriBuilder();

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    @Mock
    ClipboardUtil clipboardUtil;

    PaymentHolder paymentHolder;

    private String testAddress = "jd9dj9sdjd9jdf0swhje";

    private FragmentController<RequestDialogFragment> fragmentController;
    private RequestDialogFragment fragment;
    private ShadowActivity shadowActivity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(accountManager.getNextReceiveAddress()).thenReturn(testAddress);
        paymentHolder = new PaymentHolder(new USDCurrency(60000d), new TransactionFee(5, 10, 15));
        fragmentController = Robolectric.buildFragment(RequestDialogFragment.class);
        fragment = fragmentController.get();
        fragmentController.create();
        paymentHolder.loadPaymentFrom(new USDCurrency(0d));
    }

    private void start() {
        fragment.setPaymentHolder(paymentHolder);
        fragment.accountManager = accountManager;
        fragment.bitcoinUriBuilder = bitcoinUriBuilder;
        fragment.localBroadCastUtil = localBroadCastUtil;
        fragment.clipboardUtil = clipboardUtil;
        fragmentController.start().resume().visible();
        shadowActivity = shadowOf(fragment.getActivity());
    }

    private void startWithAmount() {
        paymentHolder.loadPaymentFrom(new BTCCurrency(1d));
        start();
    }

    @Test
    public void sets_copy_button_text_with_next_receive_address() {
        start();
        Button button = fragment.getView().findViewById(R.id.request_copy_button);

        assertThat(button.getText().toString(), equalTo(testAddress));
    }

    @Test
    public void price_visible_when_set() {
        startWithAmount();

        TextView primary = fragment.getView().findViewById(R.id.primary_currency);
        TextView secondary = fragment.getView().findViewById(R.id.secondary_currency);

        assertThat(primary.getText().toString(), equalTo(paymentHolder.getPrimaryCurrency().toFormattedCurrency()));
        assertThat(secondary.getText().toString(), equalTo(paymentHolder.getSecondaryCurrency().toFormattedCurrency()));
        assertThat(secondary.getVisibility(), equalTo(View.VISIBLE));
        assertThat(primary.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void price_not_visible_when_void() {
        start();
        TextView primary = fragment.getView().findViewById(R.id.primary_currency);
        TextView secondary = fragment.getView().findViewById(R.id.secondary_currency);

        assertThat(secondary.getVisibility(), equalTo(View.GONE));
        assertThat(primary.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void selecting_copy_button_puts_address_in_clipboard() {
        startWithAmount();
        fragment.getView().findViewById(R.id.request_copy_button).performClick();

        verify(clipboardUtil).setClipFromText("Bitcoin Address", "bitcoin:jd9dj9sdjd9jdf0swhje?amount=1.00000000");
    }

    @Test
    public void shows_message_informing_user_that_address_copied() {
        start();
        fragment.getView().findViewById(R.id.request_copy_button).performClick();

        String textOfLatestToast = ShadowToast.getTextOfLatestToast();
        assertThat(textOfLatestToast, equalTo(fragment.getContext().getString(R.string.request_copied_message)));
    }

    @Test
    public void send_opens_chooser_to_share_address() {
        start();
        fragment.getView().findViewById(R.id.request_funds).performClick();

        Intent chooserIntent = shadowActivity.getNextStartedActivity();
        Intent intent = (Intent) chooserIntent.getExtras().get("android.intent.extra.INTENT");

        assertThat(intent.getAction(), equalTo(Intent.ACTION_SEND));
        assertThat(intent.getExtras().getString(Intent.EXTRA_TEXT), equalTo("bitcoin:" + testAddress));
        assertThat(intent.getType(), equalTo("image/*"));
    }

    @Ignore
    @Test
    public void close_button_closes_fragment() {
        start();
        Dialog latestDialog = ShadowDialog.getLatestDialog();

        assertFalse(latestDialog.isShowing());
    }

    @Test
    public void does_not_include_price_for_request_when_price_is_zero() {
        start();

        fragment.getView().findViewById(R.id.request_funds).performClick();

        Intent chooserIntent = shadowActivity.getNextStartedActivity();
        Intent intent = (Intent) chooserIntent.getExtras().get("android.intent.extra.INTENT");
        assertThat(intent.getExtras().getString(Intent.EXTRA_TEXT), equalTo("bitcoin:" + testAddress));
    }


    @Test
    public void starts_qr_service_to_generate_qr_code() {
        startWithAmount();

        Intent intent = shadowActivity.getNextStartedService();
        assertThat(intent.getStringExtra(Intents.EXTRA_TEMP_QR_SCAN),
                equalTo("bitcoin:jd9dj9sdjd9jdf0swhje?amount=1.00000000"));
    }

    @Test
    public void includes_price_with_request() {
        startWithAmount();

        fragment.getView().findViewById(R.id.request_funds).performClick();

        Intent chooserIntent = shadowActivity.getNextStartedActivity();
        Intent intent = (Intent) chooserIntent.getExtras().get("android.intent.extra.INTENT");
        assertThat(intent.getExtras().getString(Intent.EXTRA_TEXT),
                equalTo("bitcoin:" + testAddress + "?amount=" + "1.00000000"));
    }

    @Test
    public void registers_for_qr_broadcast_when_resumed() {
        start();

        ArgumentCaptor<IntentFilter> intentFilterCaptor = ArgumentCaptor.forClass(IntentFilter.class);

        verify(localBroadCastUtil).registerReceiver(eq(fragment.qrBroadcastReceiver), intentFilterCaptor.capture());
        IntentFilter filter = intentFilterCaptor.getValue();
        assertThat(filter.getAction(0), equalTo(Intents.ACTION_VIEW_QR_CODE));
    }

    @Test
    public void unregisters_for_qr_broadcast_when_paused() {
        start();

        fragmentController.pause();

        verify(localBroadCastUtil).unregisterReceiver(fragment.qrBroadcastReceiver);
    }


}