package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.BroadcastBtcInvite;
import com.coinninja.coinkeeper.model.db.BroadcastBtcInviteDao;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.enums.BTCState;

import java.util.List;

import javax.inject.Inject;

public class BroadcastBtcInviteHelper {
    private final DaoSessionManager daoSessionManager;

    @Inject
    BroadcastBtcInviteHelper(DaoSessionManager daoSessionManager) {
        this.daoSessionManager = daoSessionManager;
    }

    public void saveBroadcastInviteAsCanceled(InviteTransactionSummary invite) {
        saveBroadcastBtcInvite(invite, invite.getServerId(), "", invite.getAddress(), BTCState.CANCELED);
    }

    public void saveBroadcastBtcInvite(InviteTransactionSummary invite, String inviteServerID, String txID, String sendToBtcAddress, BTCState btcState) {
        BroadcastBtcInviteDao broadcastInviteDao = daoSessionManager.getBroadcastBtcInviteDao();


        BroadcastBtcInvite broadcastInvite = broadcastInviteDao.queryBuilder().
                where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(inviteServerID)).
                limit(1).unique();

        if (broadcastInvite == null) {
            broadcastInvite = new BroadcastBtcInvite();
            broadcastInviteDao.insert(broadcastInvite);
        }

        broadcastInvite.setBroadcastToAddress(sendToBtcAddress);
        broadcastInvite.setBroadcastTxID(txID);
        broadcastInvite.setBtcState(btcState);
        broadcastInvite.setInviteTransactionSummary(invite);
        broadcastInvite.setInviteTransactionSummaryID(invite.getId());
        broadcastInvite.setInviteServerID(inviteServerID);


        broadcastInvite.update();
        broadcastInvite.refresh();
        broadcastInviteDao.refresh(broadcastInvite);
    }

    public List<BroadcastBtcInvite> getBroadcastInvites() {
        BroadcastBtcInviteDao broadcastInviteDao = daoSessionManager.getBroadcastBtcInviteDao();
        return broadcastInviteDao.queryBuilder().list();
    }

    public void removeBtcInvite(BroadcastBtcInvite btcInvite) {
        BroadcastBtcInviteDao broadcastInviteDao = daoSessionManager.getBroadcastBtcInviteDao();

        BroadcastBtcInvite broadcastInvite = broadcastInviteDao.queryBuilder().
                where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(btcInvite.getInviteServerID())).
                limit(1).unique();

        if (broadcastInvite == null) {
            return;
        }

        broadcastInviteDao.delete(broadcastInvite);
    }
}
