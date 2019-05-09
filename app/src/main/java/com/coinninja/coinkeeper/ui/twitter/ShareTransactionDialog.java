package com.coinninja.coinkeeper.ui.twitter;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment;
import com.coinninja.coinkeeper.util.TwitterUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import javax.inject.Inject;

public class ShareTransactionDialog extends BaseBottomDialogFragment {
    private Analytics analytics;
    private TwitterUtil twitterUtil;
    private UserPreferences userPreferences;
    private String memo;

    @Inject
    public ShareTransactionDialog(Analytics analytics, TwitterUtil twitterUtil, UserPreferences userPreferences) {
        this.analytics = analytics;
        this.twitterUtil = twitterUtil;
        this.userPreferences = userPreferences;
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.dialog_share_transaction;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_Transparent);
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().findViewById(R.id.share_next_time).setOnClickListener(v -> shareNextTimeClicked());
        getView().findViewById(R.id.twitter_share_button).setOnClickListener(v -> twitterShareClicked());
        getView().findViewById(R.id.dont_ask_me_button).setOnClickListener(v -> dontAskMeAgainClicked());
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    private void twitterShareClicked() {
        analytics.trackEvent(Analytics.EVENT_SHARE_VIA_TWITTER);

        Intent intent = twitterUtil.createTwitterIntent(getContext(), twitterUtil.getShareMessage(memo));
        if (intent != null) {
            startActivity(intent);
        }

        dismiss();
    }

    private void shareNextTimeClicked() {
        analytics.trackEvent(Analytics.EVENT_SHARE_NEXT_TIME);
        dismiss();
    }

    private void dontAskMeAgainClicked() {
        analytics.trackEvent(Analytics.EVENT_NEVER_SHARE);
        userPreferences.setShouldShareOnTwitter(false);
        dismiss();
    }

}
