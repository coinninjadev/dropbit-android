package com.coinninja.coinkeeper.util.android;

import android.content.ClipData;
import android.content.ClipboardManager;

import javax.inject.Inject;

public class ClipboardUtil {

    private ClipboardManager clipboardManager;

    @Inject
    public ClipboardUtil(ClipboardManager clipboardManager) {
        this.clipboardManager = clipboardManager;
    }

    public String getRaw() {
        ClipData clipData = clipboardManager.getPrimaryClip();
        return getRawClipboardString(clipData);
    }

    private String getRawClipboardString(ClipData clipData) {
        String rawClipString = "";

        if (clipData != null && clipData.getItemCount() > 0) {
            ClipData.Item item = clipData.getItemAt(0);
            if (item != null && item.getText() != null) {
                rawClipString = item.getText().toString();
            }
        }
        return rawClipString;
    }

    public void setClipFromText(String label, String clipText) {
        ClipData clipData = ClipData.newPlainText(label, clipText);
        clipboardManager.setPrimaryClip(clipData);
    }
}
