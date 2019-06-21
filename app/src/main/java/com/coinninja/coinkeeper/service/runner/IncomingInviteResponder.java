package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.dto.AddressDTO;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import javax.inject.Inject;

import retrofit2.Response;

public class IncomingInviteResponder implements Runnable {
    static String TAG = IncomingInviteResponder.class.getSimpleName();

    private final SignedCoinKeeperApiClient client;
    private final TransactionHelper transactionHelper;
    private final InternalNotificationHelper notificationHelper;
    private final WalletHelper walletHelper;
    private final AccountManager accountManager;
    private final Analytics analytics;
    private final CNLogger logger;

    @Inject
    public IncomingInviteResponder(SignedCoinKeeperApiClient client, TransactionHelper transactionHelper,
                                   InternalNotificationHelper internalNotificationHelper,
                                   WalletHelper walletHelper, AccountManager accountManager,
                                   Analytics analytics, CNLogger logger) {
        this.client = client;
        this.walletHelper = walletHelper;
        this.transactionHelper = transactionHelper;
        notificationHelper = internalNotificationHelper;
        this.accountManager = accountManager;
        this.analytics = analytics;
        this.logger = logger;
    }

    @Override
    public void run() {
        List<InviteTransactionSummary> invites = walletHelper.getIncompleteReceivedInvites();
        HashMap<String, AddressDTO> unusedAddressesToPubKey = accountManager.unusedAddressesToPubKey(HDWallet.EXTERNAL, invites.size());
        ArrayList<String> addresses = new ArrayList(unusedAddressesToPubKey.keySet());

        InviteTransactionSummary invite;
        String address;
        String pubkey;
        AddressDTO addressDTO;

        for (int i = 0; i < invites.size(); i++) {
            invite = invites.get(i);
            address = addresses.get(i % addresses.size());
            addressDTO = unusedAddressesToPubKey.get(address);
            pubkey = addressDTO.getUncompressedPublicKey();

            CNWalletAddress cnAddress = postAddress(address, invite, pubkey);
            if (cnAddress != null) {
                analytics.trackEvent(Analytics.EVENT_DROPBIT_ADDRESS_PROVIDED);
                transactionHelper.updateInviteAddressTransaction(invite.getServerId(), address);
                saveNotificationFor(invite);
            }
        }
    }

    private CNWalletAddress postAddress(String sendAddress, InviteTransactionSummary invite, String addressPubKey) {
        Response response = client.sendAddressForInvite(invite.getServerId(), sendAddress, addressPubKey);

        if (response.isSuccessful()) {
            return (CNWalletAddress) response.body();
        } else {
            logger.logError(TAG, "|---- Send address failed", response);
            return null;
        }
    }

    private void saveNotificationFor(InviteTransactionSummary invite) {
        String notificationMessage =
                "We have sent a Bitcoin address to "
                        + invite.getLocaleFriendlyDisplayIdentityForSender()
                        + " for "
                        + new BTCCurrency(invite.getValueSatoshis()).toFormattedCurrency()
                        + " to be sent.";
        notificationHelper.addNotifications(notificationMessage);
    }
}
