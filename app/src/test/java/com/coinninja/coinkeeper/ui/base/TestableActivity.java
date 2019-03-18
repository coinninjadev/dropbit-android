package com.coinninja.coinkeeper.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.coinninja.coinkeeper.R;

import androidx.annotation.Nullable;

public class TestableActivity extends Activity {
    public static int LAYOUT = R.layout.test__root_layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
    }

    public void appendLayout(int layoutId) {
        ViewGroup parent = findViewById(R.id.test_root);
        LayoutInflater.from(this).inflate(layoutId, parent, true);
    }
}
