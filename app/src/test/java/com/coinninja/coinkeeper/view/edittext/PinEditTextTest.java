package com.coinninja.coinkeeper.view.edittext;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Vibrator;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.View;

import com.coinninja.coinkeeper.view.fragment.abstracts.PinFragment;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import junitx.util.PrivateAccessor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PinEditTextTest {

    @Mock
    PinEditText mockPinEditText;

    @Mock
    DigitHandlerEditText mockDigitHandlerEditText;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        doCallRealMethod().when(mockPinEditText).setOnClickListener(any());
        doCallRealMethod().when(mockPinEditText).onClick(any());
        doCallRealMethod().when(mockPinEditText).onDraw(any());
        doCallRealMethod().when(mockPinEditText).setOnSixDigitsEnteredListener(any());
        doCallRealMethod().when(mockPinEditText).onSixDigits();
        doCallRealMethod().when(mockPinEditText).setOnDismissRequestListener(any());
        doCallRealMethod().when(mockPinEditText).getPin();
        doCallRealMethod().when(mockPinEditText).setText(anyString());
        doCallRealMethod().when(mockPinEditText).getText();
        doCallRealMethod().when(mockPinEditText).clearPin();
        doCallRealMethod().when(mockPinEditText).onKeyPreIme(anyInt(), any());
        doCallRealMethod().when(mockPinEditText).onError();
        doCallRealMethod().when(mockPinEditText).getCustomSelectionActionModeCallback();
        doCallRealMethod().when(mockPinEditText).setCustomSelectionActionModeCallback(any());
        doCallRealMethod().when(mockPinEditText).initView(any(), any());
    }

    @Test
    public void onDraw() throws Exception {
        DrawPinEditText mockDrawPinEditText = mock(DrawPinEditText.class);
        PrivateAccessor.setField(mockPinEditText, "drawPinEditText", mockDrawPinEditText);
        Canvas mockCanvas = mock(Canvas.class);

        mockPinEditText.onDraw(mockCanvas);

        verify(mockPinEditText).onDraw(mockCanvas);
    }

    @Test
    public void setOnClickListener() throws Exception {
        View.OnClickListener onClickListener = mock(View.OnClickListener.class);

        mockPinEditText.setOnClickListener(onClickListener);

        View.OnClickListener currentOnClick = (View.OnClickListener) PrivateAccessor.getField(mockPinEditText, "externalOnClickListener");
        assertEquals(onClickListener, currentOnClick);
    }

    @Test
    public void onClick() throws Exception {

        View view = mock(View.class);

        String fakePin = "45689";
        Editable editable = mock(Editable.class);
        when(editable.length()).thenReturn(fakePin.length());

        when(mockPinEditText.getText()).thenReturn(editable);

        View.OnClickListener onClickListener = v -> assertEquals(v, view);
        mockPinEditText.setOnClickListener(onClickListener);

        mockPinEditText.onClick(view);

        verify(editable).length();
    }

    @Test
    public void onSixDigits() throws Exception {
        PinFragment pinFragment = mock(PinFragment.class);
        mockPinEditText.setOnSixDigitsEnteredListener(pinFragment);

        mockPinEditText.onSixDigits();

        verify(pinFragment).onSixDigits();
    }

    @Test
    public void setOnSixDigitsEnteredListener() throws Exception {
        PinEditText.OnSixDigitsEnteredListener mockDigitsEnteredListener = mock(PinEditText.OnSixDigitsEnteredListener.class);

        mockPinEditText.setOnSixDigitsEnteredListener(mockDigitsEnteredListener);

        PinEditText.OnSixDigitsEnteredListener currentDigitListener = (PinEditText.OnSixDigitsEnteredListener) PrivateAccessor.getField(mockPinEditText, "onSixDigitsEntered");
        assertEquals(mockDigitsEnteredListener, currentDigitListener);
    }

    @Test
    public void getPin() throws Exception {
        PrivateAccessor.setField(mockPinEditText, "digitHandler", mockDigitHandlerEditText);
        doCallRealMethod().when(mockDigitHandlerEditText).getPinWithIntegrityCheck(anyString());


        int[] fakePin = {4, 5, 6, 8, 9, 5};
        String intToString = intToString(fakePin);
        Editable editable = mock(Editable.class);
        when(editable.toString()).thenReturn(intToString);
        when(mockPinEditText.getText()).thenReturn(editable);


        assertArrayEquals(fakePin, mockPinEditText.getPin());
    }

    @Test
    public void clearPin() throws Exception {
        mockPinEditText.clearPin();

        verify(mockPinEditText).setText("");
    }


    private String intToString(int[] fakePin) {
        StringBuilder outValue = new StringBuilder();
        for (int value : fakePin) {
            outValue.append(value);
        }
        return outValue.toString();
    }

    @Test
    public void initView() throws Exception {
        Context context = mock(Context.class);
        Resources res = mock(Resources.class);

        DisplayMetrics metrics = mock(DisplayMetrics.class);
        DrawPinEditText drawPinEditText = mock(DrawPinEditText.class);
        PrivateAccessor.setField(mockPinEditText, "drawPinEditText", drawPinEditText);

        when(context.getResources()).thenReturn(res);
        when(res.getDisplayMetrics()).thenReturn(metrics);
        when(mockPinEditText.isInEditMode()).thenReturn(true);

        mockPinEditText.initView(context, mock(AttributeSet.class));

        verify(drawPinEditText).initLines(metrics.density, null);
        verify(drawPinEditText).initSpacing(metrics.density);
    }

    @Test
    public void onError() throws Exception {
        Context context = mock(Context.class);
        Vibrator vibrator = mock(Vibrator.class);

        when(mockPinEditText.getContext()).thenReturn(context);
        when(context.getSystemService(Context.VIBRATOR_SERVICE)).thenReturn(vibrator);
        mockPinEditText.onError();

        long[] pattern = {0, 100, 0, 100, 0, 100, 0, 100};
        verify(vibrator).vibrate(pattern, -1);
        verify(mockPinEditText).clearPin();
        verify(mockPinEditText).startAnimation(any());
    }

    @Test
    public void onKeyPreIme() throws Exception {
        PinEditText.OnDismissRequestListener onDismissRequestListener = mock(PinEditText.OnDismissRequestListener.class);
        KeyEvent keyEvent = mock(KeyEvent.class);
        when(keyEvent.getAction()).thenReturn(KeyEvent.ACTION_UP);


        mockPinEditText.setOnDismissRequestListener(onDismissRequestListener);
        mockPinEditText.onKeyPreIme(KeyEvent.KEYCODE_BACK, keyEvent);

        verify(onDismissRequestListener).onDismissRequest();
    }

    @Test
    public void onKeyPreIme_BadKey() throws Exception {
        PinEditText.OnDismissRequestListener onDismissRequestListener = mock(PinEditText.OnDismissRequestListener.class);
        KeyEvent keyEvent = mock(KeyEvent.class);
        when(keyEvent.getAction()).thenReturn(KeyEvent.ACTION_UP);


        mockPinEditText.setOnDismissRequestListener(onDismissRequestListener);
        mockPinEditText.onKeyPreIme(KeyEvent.KEYCODE_0, keyEvent);

        verify(onDismissRequestListener, never()).onDismissRequest();
    }

    @Test
    public void onKeyPreIme_NullDismissListener() throws Exception {
        PinEditText.OnDismissRequestListener onDismissRequestListener = mock(PinEditText.OnDismissRequestListener.class);
        KeyEvent keyEvent = mock(KeyEvent.class);
        when(keyEvent.getAction()).thenReturn(KeyEvent.ACTION_UP);


        mockPinEditText.setOnDismissRequestListener(null);
        mockPinEditText.onKeyPreIme(KeyEvent.KEYCODE_BACK, keyEvent);

        verify(onDismissRequestListener, never()).onDismissRequest();
    }


    @Test
    public void disableCopyPaste() throws Exception {
        MockPinEditText pinEditText = mock(MockPinEditText.class);
        doCallRealMethod().when(pinEditText).initView(any(), any());
        doCallRealMethod().when(pinEditText).setCustomSelectionActionModeCallback(any());
        doCallRealMethod().when(pinEditText).getCustomSelectionActionModeCallback();

        Context context = mock(Context.class);
        Resources res = mock(Resources.class);

        DisplayMetrics metrics = mock(DisplayMetrics.class);
        DrawPinEditText drawPinEditText = mock(DrawPinEditText.class);
        PrivateAccessor.setField(pinEditText, "drawPinEditText", drawPinEditText);

        when(context.getResources()).thenReturn(res);
        when(res.getDisplayMetrics()).thenReturn(metrics);
        when(pinEditText.isInEditMode()).thenReturn(true);


        pinEditText.initView(context, mock(AttributeSet.class));
        ActionMode.Callback act = pinEditText.getCustomSelectionActionModeCallback();
        act.onDestroyActionMode(null);

        verify(drawPinEditText).initLines(metrics.density, null);
        verify(drawPinEditText).initSpacing(metrics.density);
        assertNotNull(act);
        assertFalse(act.onPrepareActionMode(null, null));
        assertFalse(act.onCreateActionMode(null, null));
        assertFalse(act.onActionItemClicked(null, null));
    }


    class MockPinEditText extends PinEditText {

        private ActionMode.Callback actionMode;

        public MockPinEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void setCustomSelectionActionModeCallback(ActionMode.Callback actionMode) {
            this.actionMode = actionMode;
        }

        @Override
        public ActionMode.Callback getCustomSelectionActionModeCallback() {
            return actionMode;
        }
    }

}