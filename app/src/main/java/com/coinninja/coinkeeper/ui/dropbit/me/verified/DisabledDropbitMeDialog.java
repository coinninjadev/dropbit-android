package com.coinninja.coinkeeper.ui.dropbit.me.verified;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.widget.Button;

import androidx.appcompat.content.res.AppCompatResources;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import javax.inject.Inject;

public class DisabledDropbitMeDialog extends DropBitMeDialog {
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    ServiceWorkUtil serviceWorkUtil;
    @Inject
    ActivityNavigationUtil activityNavigationUtil;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_ENABLED.equals(intent.getAction())) {
                renderAccount();
            }

        }
    };
    IntentFilter intentFilter = new IntentFilter(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_ENABLED);

    public static DropBitMeDialog newInstance() {
        return new DisabledDropbitMeDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        localBroadCastUtil.registerReceiver(receiver, intentFilter);
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.dialog_disabled_dropbit_me;
    }

    @Override
    protected void configurePrimaryCallToAction(Button button) {
        Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.cta_button);
        button.setBackground(drawable);
        button.setText(R.string.dropbit_me_enable_account_button_label);
        button.setOnClickListener(v -> serviceWorkUtil.enableDropBitMe());
    }

    @Override
    protected void configureSecondaryButton(Button button) {
        button.setText(getString(R.string.dropbit_me_learn_more));
        button.setOnClickListener(v -> activityNavigationUtil.learnMoreAboutDropbitMe(getActivity()));
    }

    @Override
    public void onPause() {
        super.onPause();
        localBroadCastUtil.unregisterReceiver(receiver);
    }
}
