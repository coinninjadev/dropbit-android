package com.coinninja.coinkeeper.ui.dropbit.me.verified;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.coinkeeper.util.DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_DISABLED;

public class VerifiedDropbitMeDialog extends DropBitMeDialog {
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_DROPBIT_ME_ACCOUNT_DISABLED.equals(intent.getAction())) {
                renderAccount();
            }
        }
    };
    IntentFilter intentFilter;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    ActivityNavigationUtil activityNavigationUtil;
    @Inject
    ServiceWorkUtil serviceWorkUtil;
    @Inject
    DropbitMeConfiguration dropbitMeConfiguration;
    @Inject
    Picasso picasso;

    public static DropBitMeDialog newInstance() {
        return new VerifiedDropbitMeDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentFilter = new IntentFilter(ACTION_DROPBIT_ME_ACCOUNT_DISABLED);
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view == null) return;

        Button button = withId(view, R.id.dropbit_me_url);
        button.setText(dropbitMeConfiguration.getShareUrl());
        localBroadCastUtil.registerReceiver(receiver, intentFilter);
        setupAvatarUI();
    }

    private void setupAvatarUI() {
        String avatar = dropbitMeConfiguration.getAvatar();

        if (avatar == null) {
            withId(getView(), R.id.twitter_profile_picture).setVisibility(View.GONE);
        } else {
            withId(getView(), R.id.twitter_profile_picture).setVisibility(View.VISIBLE);
            picasso.invalidate(avatar);
            picasso.get().load(avatar).transform(CoinKeeperApplication.appComponent.provideCircleTransform()).into(((ImageView) getView().findViewById(R.id.twitter_profile_picture)));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        localBroadCastUtil.unregisterReceiver(receiver);
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.dialog_verified_dropbit_me_account;
    }

    @Override
    protected void configurePrimaryCallToAction(Button button) {
        button.setText(R.string.dropbit_me_verified_account_button);
        Drawable drawable = Resources.INSTANCE.getDrawable(getContext(), R.drawable.twitter_icon);
        button.setCompoundDrawables(drawable, null, null, null);
        button.setCompoundDrawablePadding(Math.round(getResources().getDimension(R.dimen.button_vertical_padding_small)));
        button.setOnClickListener(v -> onPrimaryButtonClick());
    }

    @Override
    protected void configureSecondaryButton(Button button) {
        button.setText(getString(R.string.dropbit_me_disable_account_button_label));
        button.setOnClickListener(v -> onDisableAccount());
    }

    private void onDisableAccount() {
        serviceWorkUtil.disableDropBitMe();

    }

    private String getShareMessage() {
        return getString(R.string.dropbit_me_share_on_twitter, dropbitMeConfiguration.getShareUrl());
    }

    private void onPrimaryButtonClick() {
        activityNavigationUtil.shareWithTwitter(getActivity(), getShareMessage());
        dismiss();
    }

}
