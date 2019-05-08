package com.coinninja.coinkeeper.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;


import javax.inject.Inject;

import static com.coinninja.android.helpers.Resources.getString;


public class TwitterUtil {

    private Context context;
    private final Shuffler shuffler;

    @Inject
    TwitterUtil(@ApplicationContext Context context, Shuffler shuffler) {
        this.context = context;
        this.shuffler = shuffler;
    }

    public String getShareMessage(String memo) {
        Integer random = shuffler.pick(10);

        if (memo == null || memo.equals("")) {
            if (random % 2 == 0) {
                return getString(context, R.string.twitter_share_non_memo_first);
            } else {
                return getString(context, R.string.twitter_share_non_memo_second);
            }
        } else {
            if (random % 2 == 0) {
                return getString(context, R.string.twitter_share_memo_first);
            } else {
                return getString(context, R.string.twitter_share_memo_second);
            }
        }
    }

    public Intent createTwitterIntent(Context context, String memo) {
        if (context == null) {
            return null;
        }

        String memoString = getShareMessage(memo);
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TEXT, memoString);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("https://twitter.com/intent/tweet")
                .buildUpon()
                .appendQueryParameter("text", memoString)
                .build();
        intent.setData(uri);
        return intent;
    }
}
