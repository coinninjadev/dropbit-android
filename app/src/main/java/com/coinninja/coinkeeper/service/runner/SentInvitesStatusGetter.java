package com.coinninja.coinkeeper.service.runner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class SentInvitesStatusGetter implements Runnable {
    private static final String TAG = SentInvitesStatusGetter.class.getSimpleName();

    private final SignedCoinKeeperApiClient client;
    private final TransactionHelper transactionHelper;
    private PhoneNumberUtil phoneNumberUtil;
    private final Context context;
    private final InternalNotificationHelper internalNotificationHelper;

    @Inject
    SentInvitesStatusGetter(@ApplicationContext Context context, InternalNotificationHelper internalNotificationHelper,
                            SignedCoinKeeperApiClient client, TransactionHelper transactionHelper,
                            PhoneNumberUtil phoneNumberUtil) {
        this.context = context;
        this.internalNotificationHelper = internalNotificationHelper;
        this.client = client;
        this.transactionHelper = transactionHelper;
        this.phoneNumberUtil = phoneNumberUtil;
    }

    @Override
    public void run() {
        Response response = client.getSentInvites();
        if (response.isSuccessful()) {
            updateSentInvitesDatabase((List<DropBitInvitation>) response.body());
        } else {
            Log.d(TAG, "|---- Get Sent Invites failed");
            Log.d(TAG, "|------ statusCode: " + String.valueOf(response.code()));
            Log.d(TAG, "|--------- message: " + response.body());
        }
    }

    private void updateSentInvitesDatabase(List<DropBitInvitation> dropBitInvitations) {
        for (DropBitInvitation dropBitInvitation : dropBitInvitations) {
            if ("new".equals(dropBitInvitation.getStatus())) {
                transactionHelper.updateInviteAddressTransaction(dropBitInvitation);
                continue;
            }
            InviteTransactionSummary oldInvite = transactionHelper.getInviteTransactionSummary(dropBitInvitation);
            if (null != oldInvite) {
                BTCState oldInviteBtcState = oldInvite.getBtcState();
                InviteTransactionSummary newInvite = transactionHelper.updateInviteAddressTransaction(dropBitInvitation);
                if (hasStateChanged(oldInviteBtcState, newInvite)) {
                    notifyUser(newInvite);
                }
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private void notifyUser(InviteTransactionSummary invite) {
        if (invite.getBtcState() == BTCState.EXPIRED) {
            internalNotificationHelper.addNotifications(
                    String.format(context.getString(R.string.invite_send_expired_message), getContact(invite)));
        } else if (invite.getBtcState() == BTCState.CANCELED) {
            BTCCurrency btc = new BTCCurrency(invite.getValueSatoshis());
            btc.setCurrencyFormat(BTCCurrency.ALT_CURRENCY_FORMAT);
            internalNotificationHelper.addNotifications(
                    String.format(context.getString(R.string.invite_send_canceled_message),
                            getContact(invite), btc.toFormattedCurrency()));
        }
    }

    private String getContact(InviteTransactionSummary invite) {
        if (invite.getType() == Type.SENT) {
            if (null == invite.getInviteName() || invite.getInviteName().isEmpty()) {
                return invite.getReceiverPhoneNumber().toNationalDisplayText();
            } else {
                return invite.getInviteName();
            }
        } else {
            return invite.getSenderPhoneNumber().toNationalDisplayText();
        }
    }

    private boolean hasStateChanged(BTCState oldState, InviteTransactionSummary newInvite) {
        return !oldState.equals(newInvite.getBtcState());
    }
}