package com.coinninja.coinkeeper.ui.dropbit.me.verify;

import android.widget.Button;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import javax.inject.Inject;

public class VerifyDropBitMeDialog extends DropBitMeDialog {
    public static String TAG = VerifyDropBitMeDialog.class.getName();

    @Inject
    ActivityNavigationUtil activityNavigationUtil;

    public static VerifyDropBitMeDialog newInstance() {
        VerifyDropBitMeDialog verifyDropBitMeDialog = new VerifyDropBitMeDialog();
        return verifyDropBitMeDialog;
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.dialog_verify_dropbit_me_account;
    }

    @Override
    protected void configurePrimaryCallToAction(Button button) {
        button.setText(getString(R.string.dropbit_me_verify_my_account_button));
        button.setOnClickListener(v ->
                activityNavigationUtil.navigateToUserVerification(getActivity()));
    }

}
