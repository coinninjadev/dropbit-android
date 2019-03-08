package com.coinninja.coinkeeper;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class DumbActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cn_base_layout);
    }
}
