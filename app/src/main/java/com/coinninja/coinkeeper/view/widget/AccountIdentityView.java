package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;

public class AccountIdentityView extends LinearLayout {

    public AccountIdentityView(Context context) {
        this(context, null);
    }

    public AccountIdentityView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AccountIdentityView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setImage(int imageId) {
        ImageView logoImageView = findViewById(R.id.verified_identity_image);

        switch (imageId) {
            case R.drawable.twitter_icon:
                logoImageView.setBackgroundResource(R.color.colorAccent);
                break;
            case R.drawable.ic_phone:
                logoImageView.setBackgroundResource(R.color.colorBTNText);
                break;
            default:
                return;
        }

        logoImageView.setImageDrawable(Resources.INSTANCE.getDrawable(getContext(), imageId));
    }

    public String getVerifiedAccountName() {
        return ((TextView) findViewById(R.id.verified_identity_title)).getText().toString();
    }

    public void setVerifiedAccountName(String string) {
        ((TextView) findViewById(R.id.verified_identity_title)).setText(string);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.merge_account_identity_view, this, true);
        setBackgroundColor(getResources().getColor(R.color.font_white));
    }
}
