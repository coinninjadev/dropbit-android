package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.adapter.TrainingPagerAdapter;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.TrainingModel;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING;


public class TrainingActivity extends BaseActivity implements ViewPager.OnPageChangeListener, TrainingPagerAdapter.OnTrainingClickListener {


    public TrainingPagerAdapter trainingAdapter;
    @Inject
    ActivityNavigationUtil activityNavigationUtil;
    @Inject
    CNWalletManager cnWalletManager;
    ViewPager viewPager;

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        bindFooterDots(position);
    }

    @Override
    public void onPageSelected(int position) {
        TrainingModel trainingModel = TrainingModel.values()[position];

        bindFooterDots(position);
        bindLearnLink(trainingModel);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        TabLayout tabLayout = findViewById(R.id.training_footer_dots);
        Button button = findViewById(R.id.training_footer_action_button);

        if (state == SCROLL_STATE_DRAGGING) {
            tabLayout.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLearnLinkClicked(TrainingModel trainingModel) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        switch (trainingModel) {
            case WHATS_BITCOIN:
                intent.setData(DropbitIntents.URI_LEARN_ABOUT_BITCOIN);
                break;
            case SYSTEM_BROKEN:
                intent.setData(DropbitIntents.URI_WHY_BITCOIN);
                break;
            case RECOVERY_WORDS:
                intent.setData(DropbitIntents.URI_RECOVERY_WORDS);
                break;
            case DROPBIT:
                intent.setData(DropbitIntents.URI_WHAT_IS_DROPBIT);
                break;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
    }

    @Override
    public void onEndActionButtonClicked(TrainingModel trainingModel) {
        if (cnWalletManager.getHasWallet()) {
            activityNavigationUtil.navigateToHome(this);
        }
    }

    @Override
    public void onSkipClicked(TrainingModel trainingModel) {
        viewPager.setCurrentItem(3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        trainingAdapter = new TrainingPagerAdapter(this, this);
        initPager(trainingAdapter);
        viewPager.addOnPageChangeListener(this);

        initFirstPage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        trainingAdapter.restartAllVideos();
        setupOnClickListeners();
    }

    protected void initPager(TrainingPagerAdapter trainingAdapter) {
        viewPager = findViewById(R.id.training_pager);
        viewPager.setAdapter(trainingAdapter);
        TabLayout tabLayout = findViewById(R.id.training_footer_dots);
        tabLayout.setupWithViewPager(viewPager, true);
    }

    protected void initFirstPage() {
        int startPosition = 0;
        TrainingModel trainingModel = TrainingModel.values()[startPosition];
        bindFooterDots(startPosition);
        bindLearnLink(trainingModel);
    }

    protected void bindLearnLink(TrainingModel trainingModel) {
        TrainingPagerAdapter.OnTrainingClickListener onTrainingClickListener = this;
        TextView learnLink = findViewById(R.id.training_footer_learn_link);
        learnLink.setText(getText(trainingModel.getrLearnLink()));
        learnLink.setOnClickListener(view -> onTrainingClickListener.onLearnLinkClicked(trainingModel));
    }

    protected void bindFooterDots(int position) {
        TrainingModel trainingModel = TrainingModel.values()[position];
        TrainingPagerAdapter.OnTrainingClickListener onTrainingClickListener = this;
        boolean isLastItem = ((TrainingModel.values().length - 1) == position);
        TabLayout tabLayout = findViewById(R.id.training_footer_dots);
        Button button = findViewById(R.id.training_footer_action_button);

        if (isLastItem) {
            tabLayout.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(view -> onTrainingClickListener.onEndActionButtonClicked(trainingModel));
        } else {
            tabLayout.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
            button.setOnClickListener(null);
        }
    }

    private void setupOnClickListeners() {
        findViewById(R.id.ic_close).setOnClickListener(v -> dismiss());
    }

    private void dismiss() {
        finish();
    }
}
