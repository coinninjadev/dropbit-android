package com.coinninja.coinkeeper.view.activity;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.BaseActivity;

public class LicensesActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);
        ((WebView) findViewById(R.id.license_webview)).loadUrl("file:///android_asset/licensing.html");
    }
}
