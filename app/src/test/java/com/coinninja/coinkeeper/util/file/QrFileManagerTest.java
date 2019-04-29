package com.coinninja.coinkeeper.util.file;

import android.content.Context;
import android.net.Uri;

import com.coinninja.coinkeeper.util.image.QRGeneratorUtil;
import com.google.zxing.WriterException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QrFileManagerTest {

    @Mock
    private QRGeneratorUtil qrGeneratorUtil;

    @Mock
    FileUtil fileUtil;

    @Mock
    Context context;

    @Mock
    FileProviderUtil fileProviderUtil;

    @Mock
    Uri uri;

    private QRFileManager qrFileManager;
    private String exampleScan;
    private byte[] qrImageBytes;

    @Before
    public void setUp() {
        exampleScan = "bitcoin:bitcoin:3ABe5Esyys8sjjircCKvyFxBRm1rcGAAMr";
        qrImageBytes = "imageData".getBytes();
        qrFileManager = new QRFileManager(context, qrGeneratorUtil, fileUtil, fileProviderUtil);
    }


    @Test
    public void provides_sharable_qr_uri() {
        String file = "content://com.coinninja.coinkeeper.debug.provider/qr_code/qr_code.png";
        when(uri.toString()).thenReturn(file);
        when(fileProviderUtil.getUriForFile(any(Context.class), any(File.class))).thenReturn(uri);

        Uri uri = qrFileManager.getSharableURI();

        assertThat(uri.toString(), equalTo(file));
    }

    @Test
    public void deletes_creates_writes_in_order() throws WriterException, IOException {
        InOrder inOrder = inOrder(fileUtil);
        when(qrGeneratorUtil.generateFrom(exampleScan)).thenReturn(qrImageBytes);

        qrFileManager.createQrCode(exampleScan);

        inOrder.verify(fileUtil).delete(any(File.class));
        inOrder.verify(fileUtil).createFile(any(File.class));
        inOrder.verify(fileUtil).writeBytes(any(byte[].class), any(File.class));
    }

    @Test
    public void creates_file() {
        ArgumentCaptor<File> argumentCaptor = ArgumentCaptor.forClass(File.class);
        File qrCacheDirectory = new File("/external/share/tmp/tmp-qr");
        when(context.getExternalFilesDir("tmp-qr")).thenReturn(qrCacheDirectory);

        qrFileManager.createQrCode(exampleScan);

        verify(fileUtil).createFile(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().toString(), equalTo("/external/share/tmp/tmp-qr/qr_code.png"));
    }

    @Test
    public void deletes_file() {
        ArgumentCaptor<File> argumentCaptor = ArgumentCaptor.forClass(File.class);
        File qrCacheDirectory = new File("/external/share/tmp/tmp-qr");
        when(context.getExternalFilesDir("tmp-qr")).thenReturn(qrCacheDirectory);

        qrFileManager.createQrCode(exampleScan);

        verify(fileUtil).delete(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().toString(), equalTo("/external/share/tmp/tmp-qr/qr_code.png"));
    }

    @Test
    public void records_bytes_to_qr_cache() throws WriterException, IOException {
        ArgumentCaptor<File> argumentCaptor = ArgumentCaptor.forClass(File.class);
        File qrCacheDirectory = new File("/external/share/tmp/tmp-qr");
        when(context.getExternalFilesDir("tmp-qr")).thenReturn(qrCacheDirectory);
        when(qrGeneratorUtil.generateFrom(exampleScan)).thenReturn(qrImageBytes);


        qrFileManager.createQrCode(exampleScan);

        verify(fileUtil).writeBytes(eq(qrImageBytes), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().toString(), equalTo("/external/share/tmp/tmp-qr/qr_code.png"));
    }

    @Test
    public void generates_qr_code_from_string() {
        boolean created = qrFileManager.createQrCode(exampleScan);

        assertTrue(created);
    }


}