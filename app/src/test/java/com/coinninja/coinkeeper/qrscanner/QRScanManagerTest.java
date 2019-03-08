package com.coinninja.coinkeeper.qrscanner;

import android.app.Activity;
import android.media.AudioManager;
import android.view.View;
import android.widget.TextView;

import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QRScanManagerTest {

    private QRScanManager qrScanManager;
    private Activity mockActivity;
    private DecoratedBarcodeView mockDecoratedBarcodeView;
    private QRScanManager.OnScanListener mockOnScanListener;
    private DecoratedBarcodeView.TorchListener torchListener;
    private BarcodeView mockBarcodeView;
    private TextView mockBarcodeStatusView;

    @Before
    public void setUp() throws Exception {
        mockActivity = mock(Activity.class);
        mockDecoratedBarcodeView = mock(DecoratedBarcodeView.class);
        mockOnScanListener = mock(QRScanManager.OnScanListener.class);
        mockBarcodeView = mock(BarcodeView.class);
        mockBarcodeStatusView = mock(TextView.class);

        when(mockDecoratedBarcodeView.getBarcodeView()).thenReturn(mockBarcodeView);
        when(mockDecoratedBarcodeView.getStatusView()).thenReturn(mockBarcodeStatusView);
        when(mockActivity.getSystemService(anyString())).thenReturn(Mockito.mock(AudioManager.class));
        qrScanManager = new QRScanManager(mockActivity, mockDecoratedBarcodeView, mockOnScanListener);

        ArgumentCaptor<DecoratedBarcodeView.TorchListener> torchCaptor = ArgumentCaptor.forClass(DecoratedBarcodeView.TorchListener.class);
        verify(mockDecoratedBarcodeView).setTorchListener(torchCaptor.capture());
        torchListener = torchCaptor.getValue();
    }

    @Test
    public void start_capture_forces_decoding_and_resuming_test() {
        qrScanManager.startCapture();

        verify(mockDecoratedBarcodeView).decodeSingle(any());
        verify(mockDecoratedBarcodeView).resume();
    }

    @Test
    public void stop_capture_test() {
        qrScanManager.stopCapture();

        verify(mockDecoratedBarcodeView).pause();
    }

    @Test
    public void when_CaptureManager_returns_scanned_data_call_OnScanListener() {
        String sampleResultData = "Some scanned data";
        BarcodeResult barcodeResult = mock(BarcodeResult.class);
        when(barcodeResult.getText()).thenReturn(sampleResultData);

        qrScanManager.returnResult(barcodeResult);

        verify(mockOnScanListener).onScanComplete(sampleResultData);
    }

    @Test
    public void toggle_flash_from_on_to_off_test() {
        torchListener.onTorchOn();

        qrScanManager.toggleFlash();

        verify(mockDecoratedBarcodeView).setTorchOff();
    }

    @Test
    public void toggle_flash_from_off_to_on_test() {
        torchListener.onTorchOff();

        qrScanManager.toggleFlash();

        verify(mockDecoratedBarcodeView).setTorchOn();
    }

    @Test
    public void set_status_view_INVISIBLE_on_boot_test() {
        qrScanManager = new QRScanManager(mockActivity, mockDecoratedBarcodeView, mockOnScanListener);

        verify(mockBarcodeStatusView, times(2)).setVisibility(View.INVISIBLE);
    }
}