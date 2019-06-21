package com.coinninja.coinkeeper.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;

import javax.inject.Inject;


public class TwitterUtil {

    private final Shuffler shuffler;
    private Context context;

    @Inject
    public TwitterUtil(@ApplicationContext Context context, Shuffler shuffler) {
        this.context = context;
        this.shuffler = shuffler;
    }

    public String getShareMessage(String memo) {
        Integer random = shuffler.pick(10);

        if (memo == null || memo.equals("")) {
            if (random % 2 == 0) {
                return Resources.INSTANCE.getString(context, R.string.twitter_share_non_memo_first);
            } else {
                return Resources.INSTANCE.getString(context, R.string.twitter_share_non_memo_second);
            }
        } else {
            if (random % 2 == 0) {
                return Resources.INSTANCE.getString(context, R.string.twitter_share_memo_first, memo);
            } else {
                return Resources.INSTANCE.getString(context, R.string.twitter_share_memo_second, memo);
            }
        }
    }

    public Intent createTwitterIntent(Context context, String tweet) {
        if (context == null) {
            return null;
        }

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TEXT, tweet);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("https://twitter.com/intent/tweet")
                .buildUpon()
                .appendQueryParameter("text", tweet)
                .build();
        intent.setData(uri);
        return intent;
    }
}
