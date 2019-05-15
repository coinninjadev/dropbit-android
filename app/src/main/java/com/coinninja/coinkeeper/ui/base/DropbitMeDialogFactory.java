package com.coinninja.coinkeeper.ui.base;

import com.coinninja.coinkeeper.ui.dropbit.me.DropBitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.dropbit.me.verified.DisabledDropbitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.verified.NewlyVerifiedDropbitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.verified.VerifiedDropbitMeDialog;
import com.coinninja.coinkeeper.ui.dropbit.me.verify.VerifyDropBitMeDialog;

import javax.inject.Inject;

public class DropbitMeDialogFactory {

    private final DropbitMeConfiguration dropbitMeConfiguration;

    @Inject
    DropbitMeDialogFactory(DropbitMeConfiguration dropbitMeConfiguration) {
        this.dropbitMeConfiguration = dropbitMeConfiguration;
    }

    public DropBitMeDialog newInstance() {
        if (!dropbitMeConfiguration.hasVerifiedAccount()) {
            return VerifyDropBitMeDialog.newInstance();
        } else if (dropbitMeConfiguration.isNewlyVerified()) {
            return NewlyVerifiedDropbitMeDialog.newInstance();
        } else if (dropbitMeConfiguration.isDisabled()) {
            return DisabledDropbitMeDialog.newInstance();
        } else {
            return VerifiedDropbitMeDialog.newInstance();
        }
    }
}
