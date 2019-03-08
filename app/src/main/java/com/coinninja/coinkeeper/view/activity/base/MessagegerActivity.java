package com.coinninja.coinkeeper.view.activity.base;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.service.runner.HealthCheckTimerRunner;
import com.coinninja.coinkeeper.service.tasks.CNHealthCheckTask;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.InternetUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class MessagegerActivity extends BaseActivity implements CNHealthCheckTask.HealthCheckCallback {

    InternetUtil internetUtil;

    @Inject
    public Analytics analytics;

    List<WeakReference<Fragment>> fragList = new ArrayList<>();
    @Inject
    HealthCheckTimerRunner healthCheckRunner;
    ViewGroup queue;
    boolean hasForeGround;

    @Override
    public void onAttachFragment(Fragment fragment) {
        fragList.add(new WeakReference(fragment));
    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getLayoutInflater().inflate(
                R.layout.activity_messenger,
                findViewById(R.id.cn_content_wrapper),
                true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        internetUtil = InternetUtil.newInstance(this);
        healthCheckRunner.setCallback(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        queue = findViewById(R.id.message_queue);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForInternalNotifications();
        checkInternet();
        healthCheckRunner.run();
        hasForeGround = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        queue.removeCallbacks(healthCheckRunner);
        hasForeGround = false;
    }

    @Override
    protected void onStop() {
        ViewGroup queue = findViewById(R.id.message_queue);
        if (null != queue) {
            queue.removeAllViews();
            teardownMute();
        }
        analytics.onActivityStop(this);
        super.onStop();
    }

    @Override
    public void onHealthSuccess() {
        scheduleHealthCheck();
        tearDownNoInternet();
    }

    @Override
    public void onHealthFail() {
        scheduleHealthCheck();
        onNoInternet();
    }

    private void checkForInternalNotifications() {
        new LocalBroadCastUtil(getApplication()).sendBroadcast(new Intent(Intents.ACTION_INTERNAL_NOTIFICATION_UPDATE));
    }

    private void checkInternet() {
        if (internetUtil != null && !internetUtil.hasInternet()) {
            onNoInternet();
        }
    }

    private void tearDownNoInternet() {
        if (findViewById(R.id.id_no_internet_message) == null) return;

        queue.removeView(findViewById(R.id.id_no_internet_message));
        teardownMute();
    }

    private void onNoInternet() {
        if (findViewById(R.id.id_no_internet_message) == null) {
            LayoutInflater.from(this).inflate(R.layout.no_internet_message, findViewById(R.id.message_queue));
            findViewById(R.id.id_no_internet_message).
                    findViewById(R.id.component_message_action).setOnClickListener(v -> onNetworkConfigClick());
            muteViews();
        }
    }

    public void muteViews() {
        dismissAllDialogs();
        View mute = findViewById(R.id.mute);
        mute.setVisibility(View.VISIBLE);
        mute.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public void muteViewsWithMessage(String message) {
        muteViews();
        TextView messageView = findViewById(R.id.muted_message);
        messageView.setVisibility(View.VISIBLE);
        messageView.setText(message);
    }

    public void teardownMute() {
        findViewById(R.id.mute).setVisibility(View.GONE);
        findViewById(R.id.muted_message).setVisibility(View.GONE);
    }

    private void onNetworkConfigClick() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void dismissAllDialogs() {
        List<Fragment> fragments = getActiveFragments();

        if (fragments == null)
            return;

        for (Fragment fragment : fragments) {
            if (fragment instanceof DialogFragment) {
                DialogFragment dialogFragment = (DialogFragment) fragment;
                dialogFragment.dismissAllowingStateLoss();
            }
        }
    }

    public List<Fragment> getActiveFragments() {
        ArrayList<Fragment> activeFagments = new ArrayList<Fragment>();
        for (WeakReference<Fragment> ref : fragList) {
            Fragment f = ref.get();
            if (f != null) {
                if (f.isVisible()) {
                    activeFagments.add(f);
                }
            }
        }
        return activeFagments;
    }

    private void scheduleHealthCheck() {
        queue.postDelayed(healthCheckRunner, Intents.THIRTY_SECONDS);
    }

    public boolean isForeGrounded() {
        return hasForeGround;
    }
}