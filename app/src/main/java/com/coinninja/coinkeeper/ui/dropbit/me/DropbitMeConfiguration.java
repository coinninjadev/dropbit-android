package com.coinninja.coinkeeper.ui.dropbit.me;

import android.net.Uri;

import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.di.interfaces.DropbitMeUri;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;

import java.util.List;

import javax.inject.Inject;

@CoinkeeperApplicationScope
public class DropbitMeConfiguration {

    private final Uri dropbitMeUri;
    private final WalletHelper walletHelper;
    private boolean isNewlyVerified = false;
    private boolean shouldPromptWhenNextAvailable = false;
    private OnViewDropBitMeViewRequestedObserver onViewDropBitMeViewRequestedObserver;

    @Inject
    DropbitMeConfiguration(@DropbitMeUri Uri dropbitMeUri, WalletHelper walletHelper) {
        this.dropbitMeUri = dropbitMeUri;
        this.walletHelper = walletHelper;
    }

    public void setNewlyVerified() {
        isNewlyVerified = true;
        shouldPromptWhenNextAvailable = true;
    }

    public boolean isNewlyVerified() {
        return isNewlyVerified;
    }

    public boolean shouldShowWhenPossible() {
        return shouldPromptWhenNextAvailable;
    }

    public void showWhenPossible() {
        shouldPromptWhenNextAvailable = true;

        if (onViewDropBitMeViewRequestedObserver != null) {
            onViewDropBitMeViewRequestedObserver.onShowDropBitMeRequested();
        }
    }

    public void acknowledge() {
        isNewlyVerified = false;
        shouldPromptWhenNextAvailable = false;
    }

    public boolean hasVerifiedAccount() {
        return walletHelper.hasVerifiedAccount();
    }


    public void setOnViewDropBitMeViewRequestedObserver(OnViewDropBitMeViewRequestedObserver onViewDropBitMeViewRequestedObserver) {
        this.onViewDropBitMeViewRequestedObserver = onViewDropBitMeViewRequestedObserver;
    }

    public String getShareUrl() {
        Account userAccount = walletHelper.getUserAccount();
        if (userAccount == null || userAccount.getIdentities().size() == 0) return "";

        List<DropbitMeIdentity> identities = userAccount.getIdentities();
        return dropbitMeUri.buildUpon().appendPath(identities.get(0).getHandle()).toString();
    }

    public boolean isDisabled() {
        Account userAccount = walletHelper.getUserAccount();
        return userAccount == null || userAccount.getIsPrivate();
    }
}
