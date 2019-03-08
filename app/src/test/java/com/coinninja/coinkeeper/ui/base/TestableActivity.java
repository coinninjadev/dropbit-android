package com.coinninja.coinkeeper.ui.base;

import android.app.Activity;
import android.os.Bundle;

import com.coinninja.coinkeeper.R;

import androidx.annotation.Nullable;

public class TestableActivity extends Activity {
    public static int LAYOUT = R.layout.activity_splash;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
    }
}
