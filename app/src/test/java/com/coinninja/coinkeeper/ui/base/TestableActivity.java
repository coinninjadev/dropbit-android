package com.coinninja.coinkeeper.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.coinninja.coinkeeper.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TestableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.CoinKeeperTheme_Dark_Toolbar);
        setContentView(R.layout.test__root_layout);
    }

    public void appendLayout(int layoutId) {
        ViewGroup parent = findViewById(R.id.test_root);
        getLayoutInflater().inflate(layoutId, parent, true);
    }
}
