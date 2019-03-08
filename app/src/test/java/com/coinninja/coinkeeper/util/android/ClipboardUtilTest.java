package com.coinninja.coinkeeper.util.android;

import android.content.ClipData;
import android.content.ClipboardManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ClipboardUtilTest {
    private ClipboardUtil clipboardUtil;

    @Mock
    ClipboardManager clipboardManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        clipboardUtil = new ClipboardUtil(clipboardManager);
    }

    @Test
    public void sets_primary_clip() {
        ArgumentCaptor<ClipData> clipCaptor = ArgumentCaptor.forClass(ClipData.class);
        String clipText = "clipText";
        String clipLabel = "clipLabel";

        clipboardUtil.setClipFromText(clipLabel, clipText);

        verify(clipboardManager).setPrimaryClip(clipCaptor.capture());
        assertThat(clipCaptor.getValue().getItemAt(0).getText(), equalTo(clipText));
    }

    @Test
    public void get_valid_data_from_clipboard_test() {
        String sampleDataInClipboard = "some random data in clipboard";
        ClipData clipData = mock(ClipData.class);
        ClipData.Item item = mock(ClipData.Item.class);
        when(clipboardManager.getPrimaryClip()).thenReturn(clipData);
        when(clipData.getItemCount()).thenReturn(1);
        when(clipData.getItemAt(0)).thenReturn(item);
        when(item.getText()).thenReturn(sampleDataInClipboard);

        String stringInsideClipboard = clipboardUtil.getRaw();
        assertThat(stringInsideClipboard, equalTo(sampleDataInClipboard));
    }

    @Test
    public void get_null_data_from_clipboard_test() {
        String sampleDataInClipboard = null;
        ClipboardManager clipboard = mock(ClipboardManager.class);
        ClipData clipData = mock(ClipData.class);
        ClipData.Item item = mock(ClipData.Item.class);

        when(clipboard.getPrimaryClip()).thenReturn(clipData);
        when(clipData.getItemCount()).thenReturn(1);
        when(clipData.getItemAt(0)).thenReturn(item);
        when(item.getText()).thenReturn(sampleDataInClipboard);

        String stringInsideClipboard = clipboardUtil.getRaw();
        assertThat(stringInsideClipboard, equalTo(""));
    }

    @Test
    public void pass_in_null_data_from_clipboard_test() {
        ClipboardManager clipboard = mock(ClipboardManager.class);
        ClipData clipData = null;

        when(clipboard.getPrimaryClip()).thenReturn(clipData);

        String stringInsideClipboard = clipboardUtil.getRaw();
        assertThat(stringInsideClipboard, equalTo(""));
    }

    @Test
    public void get_empty_data_from_clipboard_test() {
        String sampleDataInClipboard = "";
        ClipboardManager clipboard = mock(ClipboardManager.class);
        ClipData clipData = mock(ClipData.class);
        ClipData.Item item = mock(ClipData.Item.class);

        when(clipboard.getPrimaryClip()).thenReturn(clipData);
        when(clipData.getItemCount()).thenReturn(1);
        when(clipData.getItemAt(0)).thenReturn(item);
        when(item.getText()).thenReturn(sampleDataInClipboard);

        String stringInsideClipboard = clipboardUtil.getRaw();
        assertThat(stringInsideClipboard, equalTo(sampleDataInClipboard));
    }
}