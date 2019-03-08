package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.adapter.SeedWordsPagerAdapter;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.presenter.activity.RecoveryWordsPresenter;
import com.coinninja.coinkeeper.ui.backup.SkipBackupPresenter;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class BackupActivity extends SecuredActivity implements ViewPager.OnPageChangeListener, RecoveryWordsPresenter.View {

    @Inject
    SkipBackupPresenter skipBackupPresenter;

    @Inject
    CNWalletManager cnWalletManager;

    @Inject
    RecoveryWordsPresenter presenter;

    @Inject
    SeedWordsPagerAdapter seedWordsPagerAdapter;

    @Inject
    ActivityNavigationUtil navigationUtil;

    private ViewPager seedWordsPager;
    private TextView wordPositionCount;
    private Button nextBTN;
    private Button backBTN;
    private String[] seedWords;
    private int currentPagePosition;
    private int viewState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewState = getIntent().getIntExtra(Intents.EXTRA_VIEW_STATE, Intents.EXTRA_CREATE);
        if (Intents.EXTRA_VIEW == viewState || Intents.EXTRA_BACKUP == viewState) {
            setTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff_CloseOn);
        } else {
            setTheme(R.style.CoinKeeperTheme_LightActionBar_UpOn_SkipOn);
        }

        setContentView(R.layout.activity_backup);
        seedWords = getIntent().getStringArrayExtra(Intents.EXTRA_RECOVERY_WORDS);
        seedWordsPager = findViewById(R.id.seed_words_pager);
        nextBTN = findViewById(R.id.seed_word_next_btn);
        backBTN = findViewById(R.id.seed_word_back_btn);
        wordPositionCount = findViewById(R.id.seed_word_position_count);
    }

    @Override
    protected void onResume() {
        super.onResume();
        seedWordsPagerAdapter.setSeedWords(seedWords);
        nextBTN.setOnClickListener(v -> presenter.onNextClicked());
        backBTN.setOnClickListener(v -> presenter.onBackClicked());
        presenter.attach(this);
        initSeedWordsPager();
    }

    @Override
    public void scrollToPage(int pagePosition) {
        seedWordsPager.setCurrentItem(pagePosition);
    }

    @Override
    public void setPageCounterText(String pageMsg) {
        wordPositionCount.setText(pageMsg);
    }


    @Override
    public void showNextActivity() {
        if (Intents.EXTRA_VIEW == viewState) {
            navigationUtil.navigateToHome(this);
        } else {
            showVerifyScreen();
        }
    }

    @Override
    public void hideFirst() {
        backBTN.setVisibility(View.GONE);
    }

    @Override
    public void showNext() {
        nextBTN.setBackgroundResource(R.drawable.primary_button);
        nextBTN.setText(R.string.next);
    }

    @Override
    public void showFirst() {
        backBTN.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLast() {
        nextBTN.setBackgroundResource(R.drawable.cta_button);

        if (Intents.EXTRA_VIEW == viewState) {
            nextBTN.setText(R.string.finish);
        } else {
            nextBTN.setText(R.string.verify);
        }
    }

    @Override
    public void onSkipClicked() {
        skipBackupPresenter.presentSkip(this, seedWords);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        presenter.onPageChange(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public int getPagePosition() {
        return currentPagePosition;
    }

    @Override
    public void setPagePosition(int pagePosition) {
        currentPagePosition = pagePosition;
    }

    private void showVerifyScreen() {
        Intent intent = new Intent(this, VerifyRecoverywordsActivity.class);
        intent.putExtra(VerifyRecoverywordsActivity.DATA_RECOVERY_WORDS, seedWords);
        intent.putExtra(Intents.EXTRA_VIEW_STATE, viewState);
        startActivity(intent);
    }

    private void initSeedWordsPager() {
        seedWordsPager.setAdapter(seedWordsPagerAdapter);
        seedWordsPager.addOnPageChangeListener(this);
        seedWordsPager.setCurrentItem(0);
        presenter.onPageChange(0);
    }

}
