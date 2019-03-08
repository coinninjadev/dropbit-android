package com.coinninja.coinkeeper.view.edittext;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextPaint;

import org.junit.Before;
import org.junit.Test;

import junitx.util.PrivateAccessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DrawPinEditTextTest {
    int[][] states = new int[][]{
            new int[]{android.R.attr.state_selected}, // selected
            new int[]{android.R.attr.state_focused}, // focused
            new int[]{-android.R.attr.state_focused}, // unfocused
    };

    int[] colors = new int[]{
            Color.GREEN,
            Color.BLACK,
            Color.GRAY
    };

    private DrawPinEditText drawPinEditText;

    @Before
    public void setUp() throws Exception {
        drawPinEditText = new DrawPinEditText(states, colors);
    }

    @Test
    public void initLines() throws Exception {
        Paint paint = mock(Paint.class);

        drawPinEditText.initLines(100, paint);
        Paint linesPaint = (Paint) PrivateAccessor.getField(drawPinEditText, "linesPaint");

        assertNotNull(linesPaint);
    }

    @Test
    public void initSpacing() throws Exception {
        float lineSpacing = (float) PrivateAccessor.getField(drawPinEditText, "lineSpacing");

        drawPinEditText.initSpacing(100);

        float lineSpacingAfter = (float) PrivateAccessor.getField(drawPinEditText, "lineSpacing");

        assertEquals(lineSpacingAfter, (lineSpacing * 100), 0);
        assertNotEquals(lineSpacingAfter, (lineSpacing * 50), 0);
    }

    @Test
    public void draw() throws Exception {
        String pin = "423897";
        Canvas canvas = mock(Canvas.class);
        PinEditText pinEditText = mock(PinEditText.class);
        Editable inputText = mock(Editable.class);
        TextPaint textPaint = mock(TextPaint.class);

        when(inputText.toString()).thenReturn(pin);
        when(inputText.length()).thenReturn(pin.length());
        when(pinEditText.getPaint()).thenReturn(textPaint);
        Paint paint = mock(Paint.class);

        drawPinEditText.initLines(100, paint);
        drawPinEditText.draw(canvas, pinEditText, inputText);

        verify(pinEditText).getWidth();
        verify(pinEditText).getHeight();
        verify(canvas, times(pin.length())).drawLine(anyFloat(), anyFloat(), anyFloat(), anyFloat(), any());
        verify(canvas, times(pin.length())).drawText(anyString(), anyInt(), anyInt(), anyFloat(), anyFloat(), any());
    }

    @Test
    public void draw_isFocused() throws Exception {
        String pin = "423897";
        Canvas canvas = mock(Canvas.class);
        PinEditText pinEditText = mock(PinEditText.class);
        Editable inputText = mock(Editable.class);
        TextPaint textPaint = mock(TextPaint.class);

        when(inputText.toString()).thenReturn(pin);
        when(inputText.length()).thenReturn(pin.length());
        when(pinEditText.getPaint()).thenReturn(textPaint);
        when(pinEditText.isFocused()).thenReturn(true);

        Paint paint = mock(Paint.class);

        drawPinEditText.initLines(100, paint);
        drawPinEditText.draw(canvas, pinEditText, inputText);

        verify(pinEditText).getWidth();
        verify(pinEditText).getHeight();
        verify(canvas, times(pin.length())).drawLine(anyFloat(), anyFloat(), anyFloat(), anyFloat(), any());
        verify(canvas, times(pin.length())).drawText(anyString(), anyInt(), anyInt(), anyFloat(), anyFloat(), any());
    }

}