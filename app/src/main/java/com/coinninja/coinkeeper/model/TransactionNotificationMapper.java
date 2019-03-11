package com.coinninja.coinkeeper.model;

import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.InfoV1;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.MetaV1;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.ProfileV1;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.TransactionNotificationV1;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;

import javax.inject.Inject;

public class TransactionNotificationMapper {

    PhoneNumberUtil phoneNumberUtil;

    @Inject
    public TransactionNotificationMapper(PhoneNumberUtil phoneNumberUtil) {
        this.phoneNumberUtil = phoneNumberUtil;
    }

    public TransactionNotification fromV1(TransactionNotificationV1 v1) {
        TransactionNotification transactionNotification = new TransactionNotification();

        InfoV1 info = v1.getInfo();
        transactionNotification.setMemo(info.getMemo());
        transactionNotification.setAmount(info.getAmount());
        transactionNotification.setAmountCurrency(info.getCurrency());

        ProfileV1 profile = v1.getProfile();
        transactionNotification.setAvatar(profile.getAvatar());
        transactionNotification.setDropbitMeHandle(profile.getHandle());
        transactionNotification.setDisplayName(profile.getDisplayName());

        transactionNotification.setPhoneNumber(new PhoneNumber(profile.getCountryCode(), profile.getPhoneNumber()));

        transactionNotification.setIsShared(true);

        return transactionNotification;
    }

    public TransactionNotificationV1 toV1(CompletedBroadcastDTO completedBroadcastDTO, Account account) {
        InfoV1 infoV1 = new InfoV1();
        infoV1.setMemo(completedBroadcastDTO.getMemo());
        infoV1.setCurrency("USD");
        infoV1.setAmount(completedBroadcastDTO.getHolder().satoshisRequestingToSpend);

        CNPhoneNumber CNPhoneNumber = new CNPhoneNumber(account.getPhoneNumber());

        ProfileV1 profileV1 = new ProfileV1();
        profileV1.setAvatar("");
        profileV1.setHandle("");
        profileV1.setDisplayName("");
        profileV1.setCountryCode(CNPhoneNumber.getCountryCode());
        profileV1.setPhoneNumber(CNPhoneNumber.getPhoneNumber());

        TransactionNotificationV1 v1 = new TransactionNotificationV1();
        v1.setInfo(infoV1);
        v1.setProfile(profileV1);
        v1.setTxid(completedBroadcastDTO.transactionId);
        v1.setMeta(new MetaV1());
        return v1;
    }

    public TransactionNotificationV1 toV1(InviteTransactionSummary invite, Account account) {
        TransactionNotification transactionNotification = invite.getTransactionNotification();
        InfoV1 infoV1 = new InfoV1();
        infoV1.setMemo(transactionNotification.getMemo());
        infoV1.setCurrency("USD");
        infoV1.setAmount(invite.getValueSatoshis());

        CNPhoneNumber CNPhoneNumber = new CNPhoneNumber(account.getPhoneNumber());

        ProfileV1 profileV1 = new ProfileV1();
        profileV1.setAvatar("");
        profileV1.setHandle("");
        profileV1.setDisplayName("");
        profileV1.setCountryCode(CNPhoneNumber.getCountryCode());
        profileV1.setPhoneNumber(CNPhoneNumber.getPhoneNumber());

        TransactionNotificationV1 v1 = new TransactionNotificationV1();
        v1.setInfo(infoV1);
        v1.setTxid(invite.getBtcTransactionId());
        v1.setProfile(profileV1);
        v1.setMeta(new MetaV1());
        return v1;
    }

}
