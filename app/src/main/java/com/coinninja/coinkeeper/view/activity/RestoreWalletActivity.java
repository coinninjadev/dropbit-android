package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.adapter.RestoreWalletPageAdapter;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class RestoreWalletActivity extends SecuredActivity {

    String[] recovery_words;
    ViewPager pageView;

    public RestoreWalletActivity() {
        recovery_words = new String[12];
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_wallet);
        setupPager();
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

    private void createPin() {
        Intent intent = new Intent(this, CreatePinActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray(DropbitIntents.EXTRA_RECOVERY_WORDS, recovery_words);
        intent.putExtra(DropbitIntents.EXTRA_NEXT_BUNDLE, bundle);
        intent.putExtra(DropbitIntents.EXTRA_NEXT, RecoverWalletActivity.class.getName());
        startActivity(intent);
    }

    private void reportAnalytics() {
        if (analytics != null) {
            analytics.trackEvent(Analytics.EVENT_WALLET_RESTORE);
        }
    }
}
