package com.coinninja.coinkeeper.ui.dropbit.me.verified;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;

public class NewlyVerifiedDropbitMeDialog extends VerifiedDropbitMeDialog {


    public static DropBitMeDialog newInstance() {
        return new NewlyVerifiedDropbitMeDialog();
    }

    @Override
    protected boolean shouldShowClose() {
        return false;
    }

    @Override
    protected String getTitleText() {
        return getString(R.string.dropbit_me_you_have_been_verified_title);
    }
}
