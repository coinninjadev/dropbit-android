package com.coinninja.coinkeeper.service;

import android.content.Intent;
import android.net.Uri;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.file.QRFileManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class QRCodeServiceTest {

    @Mock
    private QRFileManager qrFileManager;

    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    private String address_link = "bitcoin:3ABe5Esyys8sjjircCKvyFxBRm1rcGAAMr";
    private Intent intent;
    private QRCodeService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_TEMP_QR_SCAN, address_link);
        service = new QRCodeService();
        service.qrFileManager = qrFileManager;
        service.localBroadCastUtil = localBroadCastUtil;
        when(qrFileManager.getSharableURI()).thenReturn(Uri.parse("content://com.coinninja.coinkeeper.debug.provider/qr_code/qr_code.png"));
        when(qrFileManager.createQrCode(address_link)).thenReturn(true);
    }

    @Test
    public void only_notifies_of_location_on_successfule_writes() {
        when(qrFileManager.createQrCode(address_link)).thenReturn(false);

        service.onHandleIntent(intent);

        verify(localBroadCastUtil, times(0)).sendBroadcast(any(Intent.class));
    }


    @Test
    public void includes_link_for_sharable_uri_on_complete() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);

        service.onHandleIntent(intent);

        verify(localBroadCastUtil).sendBroadcast(argumentCaptor.capture());
        Intent broadcastIntent = argumentCaptor.getValue();
        assertThat(broadcastIntent.getStringExtra(DropbitIntents.EXTRA_QR_CODE_LOCATION),
                equalTo("content://com.coinninja.coinkeeper.debug.provider/qr_code/qr_code.png"));

    }

    @Test
    public void notifies_of_saved_file() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);

        service.onHandleIntent(intent);

        verify(localBroadCastUtil).sendBroadcast(argumentCaptor.capture());
        Intent broadcastIntent = argumentCaptor.getValue();
        assertThat(broadcastIntent.getAction(), equalTo(DropbitIntents.ACTION_VIEW_QR_CODE));
    }

    @Test
    public void encodes_data() {
        service.onHandleIntent(intent);

        verify(qrFileManager).createQrCode(address_link);
    }
}
