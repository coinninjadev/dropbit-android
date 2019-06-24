package com.coinninja.coinkeeper.view.activity;

import android.os.Bundle;
import android.webkit.WebView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;

import androidx.annotation.Nullable;

public class LicensesActivity extends SecuredActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);
        ((WebView) findViewById(R.id.license_webview)).loadUrl("file:///android_asset/licensing.html");
    }
}
