package com.coinninja.coinkeeper.view.button;

import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;

public class CopyToBufferButton extends AppCompatButton {

    private ClipboardUtil util;

    public CopyToBufferButton(Context context) {
        super(context);
        init();
    }

    public CopyToBufferButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CopyToBufferButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        util = new ClipboardUtil((ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE));
        super.setOnClickListener(v -> copyClip());
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        return;
    }

    private void copyClip() {
        String clipLabel = getClipLabel();
        util.setClipFromText(clipLabel, getClip());
        Toast.makeText(getContext(), clipLabel, Toast.LENGTH_LONG).show();
    }

    private String getClipLabel() {
        return getContext().getString(R.string.copied, getClip());
    }

    private String getClip() {
        return asString();
    }

    public String asString() {
        return getText().toString();
    }
}
