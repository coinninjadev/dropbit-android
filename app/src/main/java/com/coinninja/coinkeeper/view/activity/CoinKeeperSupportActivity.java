package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.view.adapter.CoinKeeperSupportAdapter;


public class CoinKeeperSupportActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cn_support);
        RecyclerView recyclerView = findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new CoinKeeperSupportAdapter(this::onItemSelected));
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    protected void onItemSelected(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(intent);
    }
}
