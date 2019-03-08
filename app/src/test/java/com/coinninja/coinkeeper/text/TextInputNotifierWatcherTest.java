package com.coinninja.coinkeeper.text;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.coinninja.coinkeeper.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class TextInputNotifierWatcherTest {

    private EditText text;
    private TextInputNotifierWatcher.OnInputEventListener callback;

    @Before
    public void setUp() {
        callback = mock(TextInputNotifierWatcher.OnInputEventListener.class);
        Context context = RuntimeEnvironment.application.getApplicationContext();
        View view = LayoutInflater.from(context).inflate(R.layout.activity_verify_phone_code, null, false);
        text = view.findViewById(R.id.v_one);
        text.addTextChangedListener(new TextInputNotifierWatcher(callback));
    }

    @Test
    public void notifies_after_text_changed() {
        text.setText("SA");


        verify(callback).onAfterChanged("SA");
    }

    @Test
    public void notifies_of_input() {
        text.setText("0");

        verify(callback).onInput(1);
    }


    @Test
    public void notifies_of_multiple_inputs() {
        text.setText("0123");

        verify(callback).onInput(4);
    }

    @Test
    public void notifies_of_delete_character() {
        text.setText("01");
        text.setText("0");

        verify(callback).onRemove(1);
    }
}