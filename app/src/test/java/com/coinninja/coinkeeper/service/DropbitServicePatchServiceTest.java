package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class DropbitServicePatchServiceTest {


    @Mock
    PreferencesUtil preferencesUtil;

    @Mock
    TransactionHelper transactionHelper;

    @InjectMocks
    DropbitServicePatchService dropbitServicePatchService;

    @Test
    public void records_that_the_patch_was_written() {
        dropbitServicePatchService.onHandleWork(null);

        verify(preferencesUtil).savePreference(DropbitServicePatchService.PATCH_KEY, true);
    }

    @Test
    public void does_not_execute_when_patch_already_applied() {
        when(preferencesUtil.contains(DropbitServicePatchService.PATCH_KEY)).thenReturn(true);

        dropbitServicePatchService.onHandleWork(null);

        verify(preferencesUtil, times(0)).savePreference(DropbitServicePatchService.PATCH_KEY, true);
    }

    @Test
    public void updates_canceled_transactions() {
        List<TransactionsInvitesSummary> canceledDropbits = new ArrayList<>();
        when(transactionHelper.getAllCanceledDropbits()).thenReturn(canceledDropbits);
        when(transactionHelper.getAllExpiredDropbits()).thenReturn(new ArrayList<>());

        TransactionsInvitesSummary tis1 = mock(TransactionsInvitesSummary.class);
        when(tis1.getInviteTime()).thenReturn(0L);
        canceledDropbits.add(tis1);

        TransactionsInvitesSummary tis2 = mock(TransactionsInvitesSummary.class);
        when(tis2.getInviteTime()).thenReturn(1542401798247L);
        canceledDropbits.add(tis2);

        dropbitServicePatchService.onHandleWork(null);

        verify(tis1, times(0)).setInviteTime(anyLong());
        verify(tis1, times(0)).setBtcTxTime(anyLong());
        verify(tis1, times(0)).update();

        verify(tis2).setInviteTime(0);
        verify(tis2).setBtcTxTime(1542401798247L);
        verify(tis2).update();
    }

    @Test
    public void updates_expired_transactions() {
        List<TransactionsInvitesSummary> expiredDropbits = new ArrayList<>();
        when(transactionHelper.getAllExpiredDropbits()).thenReturn(expiredDropbits);
        when(transactionHelper.getAllCanceledDropbits()).thenReturn(new ArrayList<>());

        TransactionsInvitesSummary tis1 = mock(TransactionsInvitesSummary.class);
        when(tis1.getInviteTime()).thenReturn(0L);
        expiredDropbits.add(tis1);

        TransactionsInvitesSummary tis2 = mock(TransactionsInvitesSummary.class);
        when(tis2.getInviteTime()).thenReturn(1542401798247L);
        expiredDropbits.add(tis2);

        dropbitServicePatchService.onHandleWork(null);

        verify(tis1, times(0)).setInviteTime(anyLong());
        verify(tis1, times(0)).setBtcTxTime(anyLong());
        verify(tis1, times(0)).update();

        verify(tis2).setInviteTime(0);
        verify(tis2).setBtcTxTime(1542401798247L);
        verify(tis2).update();
    }
}