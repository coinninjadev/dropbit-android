package com.coinninja.coinkeeper.ui.base;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import javax.inject.Inject;

public class DoneImeEditorActionListener implements TextView.OnEditorActionListener {
    OnDoneActionSelectedListener onDoneActionSelectedListener;

    @Inject
    public DoneImeEditorActionListener() {
    }

    public void setOnDoneActionSelectedListener(OnDoneActionSelectedListener onDoneActionSelectedListener) {
        this.onDoneActionSelectedListener = onDoneActionSelectedListener;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE && onDoneActionSelectedListener != null) {
            onDoneActionSelectedListener.onDoneSelected(v.getText().toString());
            return true;
        }
        return false;
    }

    public interface OnDoneActionSelectedListener {
        void onDoneSelected(String text);
    }
}
