package com.coinninja.coinkeeper.util.file;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileUtilTest {


    private FileUtil fileUtil;

    @Mock
    File file;

    @Mock
    ByteArrayOutputStream byteArrayOutputStream;

    @Mock FileOutputUtil fileOutputUtil;

    @Mock
    FileOutputStream fileOutputStream;

    @Before
    public void setUp() throws FileNotFoundException {
        fileUtil = new FileUtil(fileOutputUtil);
        when(fileOutputUtil.getStreamFor(any(File.class))).thenReturn(fileOutputStream);
    }

    @Test
    public void writes_bytes_to_file() throws IOException {
        byte[] bytes = "foo".getBytes();

        fileUtil.writeBytes(bytes, file);

        verify(fileOutputStream).write(bytes);
    }

    @Test
    public void closes_when_stream_even_when_file_not_written() throws IOException {
        byte[] bytes = "foo".getBytes();
        when(file.exists()).thenReturn(true);
        when(byteArrayOutputStream.toByteArray()).thenReturn(bytes);

        doThrow(new IOException()).when(fileOutputStream).write(any());

        fileUtil.writeByteStream(byteArrayOutputStream, file);

        verify(byteArrayOutputStream).close();
    }

    @Test
    public void closes_byte_stream_when_write_completed() throws IOException {
        byte[] bytes = "foo".getBytes();
        when(file.exists()).thenReturn(true);
        when(byteArrayOutputStream.toByteArray()).thenReturn(bytes);

        fileUtil.writeByteStream(byteArrayOutputStream, file);

        verify(byteArrayOutputStream).close();
    }

    @Test
    public void wrytes_bytes_to_file() throws IOException {
        byte[] bytes = "foo".getBytes();
        when(file.exists()).thenReturn(true);
        when(byteArrayOutputStream.toByteArray()).thenReturn(bytes);

        fileUtil.writeByteStream(byteArrayOutputStream, file);

        verify(fileOutputStream).write(bytes);
    }
    
    @Test()
    public void does_not_create_file_when_file_exits() throws IOException {
        when(file.exists()).thenReturn(true);

        boolean created = fileUtil.createFile(file);

        assertFalse(created);
    }

    @Test
    public void creates_file_when_requests() throws IOException {
        when(file.exists()).thenReturn(false);
        when(file.createNewFile()).thenReturn(true);

        boolean created =fileUtil.createFile(file);

        assertTrue(created);
    }

    @Test
    public void does_not_delete_file_that_does_not_exist() {
        when(file.exists()).thenReturn(false);

        boolean deleted = fileUtil.delete(file);

        assertFalse(deleted);
    }

    @Test
    public void deletes_given_file_when_exits() throws IOException {
        when(file.exists()).thenReturn(true);
        when(file.delete()).thenReturn(true);

        boolean deleted = fileUtil.delete(file);

        assertTrue(deleted);
    }
}
