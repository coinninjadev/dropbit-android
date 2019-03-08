package com.coinninja.coinkeeper.ui.base;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.coinninja.coinkeeper.ui.base.DoneImeEditorActionListener.OnDoneActionSelectedListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DoneImeEditorActionListenerTest {

    private DoneImeEditorActionListener doneImeEditorActionListener;


    @Before
    public void setUp() {
        doneImeEditorActionListener = new DoneImeEditorActionListener();
    }

    @After
    public void tearDown() {
        doneImeEditorActionListener = null;
    }

    @Test
    public void provides_text_to_call_back_when_done_selected() {
        OnDoneActionSelectedListener listener = mock(OnDoneActionSelectedListener.class);
        TextView textView = mock(TextView.class);
        when(textView.getText()).thenReturn("foo my bar");
        doneImeEditorActionListener.setOnDoneActionSelectedListener(listener);

        doneImeEditorActionListener.onEditorAction(textView, EditorInfo.IME_ACTION_DONE, mock(KeyEvent.class));

        verify(listener).onDoneSelected("foo my bar");

    }

    @Test
    public void does_nothing_with_no_event_listener() {
        doneImeEditorActionListener.setOnDoneActionSelectedListener(null);

        doneImeEditorActionListener.onEditorAction(mock(TextView.class), EditorInfo.IME_ACTION_DONE, mock(KeyEvent.class));
    }
}