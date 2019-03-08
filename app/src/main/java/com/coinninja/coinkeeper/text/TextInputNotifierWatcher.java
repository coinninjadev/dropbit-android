package com.coinninja.coinkeeper.text;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;

public class TextInputNotifierWatcher implements TextWatcher, View.OnKeyListener {
    private final OnInputEventListener callback;

    public TextInputNotifierWatcher(OnInputEventListener callback) {

        this.callback = callback;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (!s.toString().isEmpty() && s.length() > before) {
            callback.onInput(s.length());
        } else if (s.toString().isEmpty() || before > s.length()) {
            callback.onRemove(s.length());
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        callback.onAfterChanged(s.toString());
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_DEL) {
            callback.onRemove(1);

        }
        return false;
    }

    public interface OnInputEventListener {
        void onInput(int numValues);

        void onRemove(int numValues);

        void onAfterChanged(String input);
    }
}
