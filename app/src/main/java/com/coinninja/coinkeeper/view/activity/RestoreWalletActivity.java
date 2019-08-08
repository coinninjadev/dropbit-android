package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.view.adapter.RestoreWalletPageAdapter;

public class RestoreWalletActivity extends BaseActivity {

    String[] recovery_words;
    ViewPager pageView;

    public RestoreWalletActivity() {
        recovery_words = new String[12];
    }

    @Override
    public void onBackPressed() {
        if (pageView.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            onPageBack(null);
        }
    }

    public void setupPager() {
        pageView = findViewById(R.id.recovery_words_pager);
        pageView.setAdapter(new RestoreWalletPageAdapter(12, this::onPageBack, this::onPageForward));
        pageView.getCurrentItem();
    }

    void onPageForward(View view) {
        int currentItem = pageView.getCurrentItem();
        recovery_words[currentItem] = ((Button) view).getText().toString().toLowerCase();

        if (currentItem >= 11) {
            createPin();
            reportAnalytics();
        } else {
            ((RestoreWalletPageAdapter) pageView.getAdapter()).resetState();
            pageView.setCurrentItem(currentItem + 1);
        }
    }

    void onPageBack(View view) {
        int currentItem = pageView.getCurrentItem();
        ((RestoreWalletPageAdapter) pageView.getAdapter()).resetState();

        if (currentItem > 0) {
            pageView.setCurrentItem(currentItem - 1);
        }

        recovery_words[currentItem] = null;
    }

    void setViewPager(ViewPager viewPager) {
        pageView = viewPager;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_wallet);
        setupPager();
    }

    private void createPin() {
        Intent intent = new Intent(this, CreatePinActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray(DropbitIntents.EXTRA_RECOVERY_WORDS, recovery_words);
        if (getIntent() != null && getIntent().hasExtra(DropbitIntents.EXTRA_SHOW_TWITTER_VERIFY_BUTTON)) {
            intent.putExtra(DropbitIntents.EXTRA_SHOW_TWITTER_VERIFY_BUTTON, getIntent().getStringExtra(DropbitIntents.EXTRA_SHOW_TWITTER_VERIFY_BUTTON));
        }
        intent.putExtra(DropbitIntents.EXTRA_NEXT_BUNDLE, bundle);
        intent.putExtra(DropbitIntents.EXTRA_NEXT, RecoverWalletActivity.class.getName());
        startActivity(intent);
    }

    private void reportAnalytics() {
        if (analytics != null) {
            analytics.trackEvent(Analytics.Companion.EVENT_WALLET_RESTORE);
        }
    }
}
