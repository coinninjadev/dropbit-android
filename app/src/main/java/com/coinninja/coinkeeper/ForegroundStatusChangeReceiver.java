package com.coinninja.coinkeeper;

public interface ForegroundStatusChangeReceiver {
    void onBroughtToForeground();

    void onSentToBackground();
}
