package com.coinninja.coinkeeper.service.runner;

import android.content.Intent;

import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

public class SyncIncomingInvitesRunner implements Runnable {
    private final GetIncomingInviteRunner getIncomingInviteRunner;
    private final LocalBroadCastUtil broadcastUtil;
    private final IncomingInviteResponder incomingInviteResponder;

    @Inject
    public SyncIncomingInvitesRunner(GetIncomingInviteRunner getIncomingInviteRunner,
                                     IncomingInviteResponder incomingInviteResponder, LocalBroadCastUtil localBroadCastUtil) {
        this.getIncomingInviteRunner = getIncomingInviteRunner;
        broadcastUtil = localBroadCastUtil;
        this.incomingInviteResponder = incomingInviteResponder;
    }

    @Override
    public void run() {
        getIncomingInviteRunner.run();
        incomingInviteResponder.run();

        broadcastUtil.sendBroadcast(new Intent(Intents.ACTION_INTERNAL_NOTIFICATION_UPDATE));
    }
}
