package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity;
import com.coinninja.coinkeeper.model.db.enums.IdentityType;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.model.CNUserPatch;

import javax.inject.Inject;

public class DropbitAccountHelper {
    private final DaoSessionManager daoSessionManager;
    private final WalletHelper walletHelper;

    @Inject
    DropbitAccountHelper(DaoSessionManager daoSessionManager, WalletHelper walletHelper) {
        this.daoSessionManager = daoSessionManager;
        this.walletHelper = walletHelper;
    }

    public void updateUserAccount(CNUserPatch cnUserPatch) {
        Account account = walletHelper.getUserAccount();
        if (account == null) return;
        account.setIsPrivate(cnUserPatch.isPrivate());
        account.update();
    }

    public void updateVerifiedAccount(CNUserAccount cnUserAccount) {
        Account account = walletHelper.getUserAccount();
        if (account == null) return;
        account.setIsPrivate(cnUserAccount.isPrivate());
        account.setStatus(Account.Status.VERIFIED);
        account.update();

        DropbitMeIdentity dropbitMeIdentity = daoSessionManager.newDropbitMeIdentity();
        dropbitMeIdentity.setHash(account.getPhoneNumberHash());
        dropbitMeIdentity.setIdentity(account.getPhoneNumber().toString());
        dropbitMeIdentity.setType(IdentityType.PHONE);
        dropbitMeIdentity.setAccount(account);
        dropbitMeIdentity.setHandle(account.getPhoneNumberHash().substring(0, 12));
        daoSessionManager.insert(dropbitMeIdentity);

    }
}
