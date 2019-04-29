package com.coinninja.coinkeeper.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.coinninja.coinkeeper.service.DeleteWalletService;
import com.coinninja.coinkeeper.ui.settings.DeleteWalletPresenter.DeleteWalletCompleteReceiver;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DeleteWalletPresenterTest {


    private DeleteWalletPresenter deleteWalletPresenter;

    Context context;
    LocalBroadCastUtil localBroadCastUtil;
    OnDeleteWalletCallback onDeleteWalletCallback;

    @Before
    public void setUp() {
        context = mock(Context.class);
        localBroadCastUtil = mock(LocalBroadCastUtil.class);
        onDeleteWalletCallback = mock(OnDeleteWalletCallback.class);

        when(context.getApplicationContext()).thenReturn(context);

        deleteWalletPresenter = new DeleteWalletPresenter(context, localBroadCastUtil);
    }

    @Test
    public void unregisters_receiver_when_deleted() {
        deleteWalletPresenter.setCallback(onDeleteWalletCallback);

        deleteWalletPresenter.receiver.onReceive(context, null);

        verify(localBroadCastUtil).unregisterReceiver(deleteWalletPresenter.receiver);
    }

    @Test
    public void does_not_call_callback_when_one_is_not_provided() {
        deleteWalletPresenter.receiver.onReceive(context, null);

        verify(onDeleteWalletCallback, times(0)).onDeleted();
    }

    @Test
    public void tells_callback_that_delete_is_completed() {
        deleteWalletPresenter.setCallback(onDeleteWalletCallback);

        deleteWalletPresenter.receiver.onReceive(context, null);

        verify(onDeleteWalletCallback).onDeleted();
    }

    @Test
    public void observes_local_notification_of_deletion_completed() {
        ArgumentCaptor<IntentFilter> filterCaptor = ArgumentCaptor.forClass(IntentFilter.class);

        deleteWalletPresenter.onDelete();
        verify(localBroadCastUtil).registerReceiver(any(DeleteWalletCompleteReceiver.class), filterCaptor.capture());

        IntentFilter filter = filterCaptor.getValue();
        assertThat(filter.getAction(0), equalTo(DropbitIntents.ACTION_ON_WALLET_DELETED));
    }

    @Test
    public void starts_delete_service_on_delete() {
        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);

        deleteWalletPresenter.onDelete();
        verify(context).startService(intentCaptor.capture());

        Intent serviceIntent = intentCaptor.getValue();
        assertThat(serviceIntent.getComponent().getClassName(), equalTo(DeleteWalletService.class.getName()));
    }

}