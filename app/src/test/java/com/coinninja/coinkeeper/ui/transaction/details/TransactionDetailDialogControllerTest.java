package com.coinninja.coinkeeper.ui.transaction.details;

import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;

import androidx.appcompat.app.AlertDialog;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.hasTag;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class TransactionDetailDialogControllerTest {
    @Mock
    BindableTransaction bindableTransaction;

    @Mock
    WalletHelper walletHelper;

    @Mock
    ActivityNavigationUtil activityNavigationUtil;

    @InjectMocks
    TransactionDetailDialogController controller;

    private TestableActivity activity;
    private String targetAddress = "-- receive address --";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(100000));
        activity = Robolectric.setupActivity(TestableActivity.class);
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.SEND);
        when(bindableTransaction.getConfirmationState()).thenReturn(BindableTransaction.ConfirmationState.UNCONFIRMED);
        when(bindableTransaction.getConfirmationCount()).thenReturn(0);
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(0L));
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(0L));
        when(bindableTransaction.getFeeCurrency()).thenReturn(new BTCCurrency(0L));
        when(bindableTransaction.getTargetAddress()).thenReturn(targetAddress);
    }

    @After
    public void tearDown() {
        activity = null;
        controller = null;
        bindableTransaction = null;
        activityNavigationUtil = null;
    }

    @Test
    public void closing_dialog_using_ic_close_dismisses_self() {
        controller.showTransaction(activity, bindableTransaction);

        withId(getLastShownAlertDialog(), R.id.ic_close).performClick();

        assertFalse(getLastShownAlertDialog().isShowing());
    }

    @Test
    public void renders_fees() {
        when(bindableTransaction.getFeeCurrency()).thenReturn(new BTCCurrency(1000L));
        controller.showTransaction(activity, bindableTransaction);

        TextView fee = withId(getLastShownAlertDialog(), R.id.value_network_fee);

        assertThat(fee, hasText("0.00001 ($0.01)"));
    }

    @Test
    public void share_txid_with_other_apps() {
        when(bindableTransaction.getTxID()).thenReturn("--txid--");
        controller.showTransaction(activity, bindableTransaction);

        withId(getLastShownAlertDialog(), R.id.share_transaction).performClick();

        verify(activityNavigationUtil).shareTransaction(activity, "--txid--");
    }

    @Test
    public void views_transaction_on_cn_block_explorer() {
        when(bindableTransaction.getTxID()).thenReturn("--txid--");
        controller.showTransaction(activity, bindableTransaction);

        withId(getLastShownAlertDialog(), R.id.see_on_block).performClick();

        verify(activityNavigationUtil).showTxidOnBlock(activity, "--txid--");
    }

    @Test
    public void views_address_on_cn_block_explorer() {
        when(bindableTransaction.getTargetAddress()).thenReturn("--address--");
        controller.showTransaction(activity, bindableTransaction);

        withId(getLastShownAlertDialog(), R.id.receive_address).performClick();

        verify(activityNavigationUtil).showAddressOnBlock(activity, "--address--");
    }

    @Test
    public void sets_address_to_be_the_receive_address() {
        controller.showTransaction(activity, bindableTransaction);

        TextView address = withId(getLastShownAlertDialog(), R.id.receive_address);

        assertThat(address, hasText(targetAddress));
    }

    @Test
    public void sets_value_of_transaction___as_transfer() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.TRANSFER);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(100000));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));

        controller.showTransaction(activity, bindableTransaction);

        TextView value = withId(getLastShownAlertDialog(), R.id.value_when_sent);
        assertThat(value, hasText("0 ($0.00)"));
    }

    @Test
    public void sets_value_of_transaction___as_receiver() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.RECEIVE);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(100000));
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(50000000L));

        controller.showTransaction(activity, bindableTransaction);

        TextView value = withId(getLastShownAlertDialog(), R.id.value_when_sent);
        assertThat(value, hasText("0.5 ($500.00)"));
    }

    @Test
    public void sets_value_of_transaction___as_sender() {
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(100000));
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(50000000L));

        controller.showTransaction(activity, bindableTransaction);

        TextView value = withId(getLastShownAlertDialog(), R.id.value_when_sent);
        assertThat(value, hasText("0.5 ($500.00)"));
    }

    @Test
    public void sets_number_of_confirmations() {
        when(bindableTransaction.getConfirmationCount()).thenReturn(100);
        controller.showTransaction(activity, bindableTransaction);
        TextView confirmations = withId(getLastShownAlertDialog(), R.id.value_confirmations);
        assertThat(confirmations, hasText("6+"));

        when(bindableTransaction.getConfirmationCount()).thenReturn(5);
        controller.showTransaction(activity, bindableTransaction);
        confirmations = withId(getLastShownAlertDialog(), R.id.value_confirmations);
        assertThat(confirmations, hasText("5"));
    }

    @Test
    public void sets_confirmation_state() {
        when(bindableTransaction.getConfirmationState()).thenReturn(BindableTransaction.ConfirmationState.CONFIRMED);
        controller.showTransaction(activity, bindableTransaction);
        TextView confirmations = withId(getLastShownAlertDialog(), R.id.confirmations);
        assertThat(confirmations, hasText(Resources.getString(activity, R.string.confirmations_view_stage_5)));

        when(bindableTransaction.getConfirmationState()).thenReturn(BindableTransaction.ConfirmationState.UNCONFIRMED);
        controller.showTransaction(activity, bindableTransaction);
        confirmations = withId(getLastShownAlertDialog(), R.id.confirmations);
        assertThat(confirmations, hasText(Resources.getString(activity, R.string.confirmations_view_stage_4)));
    }

    @Test
    public void renders_send_icon_when_transfer() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.TRANSFER);
        controller.showTransaction(activity, bindableTransaction);

        ImageView imageView = withId(getLastShownAlertDialog(), R.id.ic_send_state);

        assertThat(imageView, hasTag(R.drawable.ic_transaction_send));
    }

    @Test
    public void renders_send_icon_when_receive() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.RECEIVE);
        controller.showTransaction(activity, bindableTransaction);

        ImageView imageView = withId(getLastShownAlertDialog(), R.id.ic_send_state);

        assertThat(imageView, hasTag(R.drawable.ic_transaction_receive));
    }

    @Test
    public void renders_send_icon_when_send() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.SEND);
        controller.showTransaction(activity, bindableTransaction);

        ImageView imageView = withId(getLastShownAlertDialog(), R.id.ic_send_state);

        assertThat(imageView, hasTag(R.drawable.ic_transaction_send));
    }

    @Test
    public void renders_dialog_with_details_of_transaction() {
        controller.showTransaction(activity, bindableTransaction);

        assertNotNull(getLastShownAlertDialog());
    }

    private AlertDialog getLastShownAlertDialog() {
        return (AlertDialog) ShadowDialog.getLatestDialog();
    }
}