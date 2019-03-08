package com.coinninja.coinkeeper.service;

import android.Manifest;
import android.content.Intent;

import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.db.TransactionNotificationDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummaryDao;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.LocalContactQueryUtil;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;

import org.greenrobot.greendao.query.Join;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import dagger.android.AndroidInjection;

public class ContactLookupService extends JobIntentService {

    @Inject
    TransactionSummaryCountDataSource transactionSummaryDataSource;

    @Inject
    LocalContactQueryUtil localContactQueryUtil;

    @Inject
    DaoSessionManager sessionManager;

    @Inject
    PermissionsUtil permissionsUtil;

    private TransactionsInvitesSummaryDao transactionInviteSummaryDao;
    private List<List<Contact>> localContacts;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        this.transactionInviteSummaryDao = sessionManager.getTransactionsInvitesSummaryDao();
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (!permissionsUtil.hasPermission(Manifest.permission.READ_CONTACTS) ||
                !transactionSummaryDataSource.shouldAttemptToMapContacts()) { return; }
        queryForAllLocalContacts();
        List<TransactionsInvitesSummary> transactionSummaries = queryAllTransactionSummariesWithoutToName();
        for (TransactionsInvitesSummary summary : transactionSummaries) {
            for (List<Contact> contactChunk : localContacts) {
                for (Contact contact : contactChunk) {
                    PhoneNumber phoneNumberToCompareAgainst = findPhoneNumberToCompareAgainst(summary);
                    if (compare(contact.getPhoneNumber(), phoneNumberToCompareAgainst)) {
                        summary.setToName(contact.getDisplayName());
                        transactionInviteSummaryDao.update(summary);
                    }
                }
            }
        }
    }

    private PhoneNumber findPhoneNumberToCompareAgainst(TransactionsInvitesSummary summary) {
        return summary.getToPhoneNumber() == null ? summary.getTransactionSummary().getTransactionNotification().getPhoneNumber() : summary.getToPhoneNumber();
    }

    private void queryForAllLocalContacts() {
        localContacts = localContactQueryUtil.getContactsInChunks(100);
    }

    private List<TransactionsInvitesSummary> queryAllTransactionSummariesWithoutToName() {
        ArrayList<TransactionsInvitesSummary> list = new ArrayList(sessionManager.getTransactionsInvitesSummaryDao().queryBuilder()
                .where(TransactionsInvitesSummaryDao.Properties.ToPhoneNumber.isNotNull()).list());

        QueryBuilder<TransactionsInvitesSummary> summaryWithMemos = sessionManager.getTransactionsInvitesSummaryDao().queryBuilder();
        Join transactionSummaryJoin = summaryWithMemos.join(TransactionSummary.class, TransactionSummaryDao.Properties.TransactionsInvitesSummaryID);
        summaryWithMemos.join(transactionSummaryJoin, TransactionSummaryDao.Properties.TransactionNotificationId, TransactionNotification.class, TransactionNotificationDao.Properties.Id);
        summaryWithMemos.where(TransactionsInvitesSummaryDao.Properties.ToName.isNull(), TransactionsInvitesSummaryDao.Properties.ToPhoneNumber.isNull());

        list.addAll(summaryWithMemos.list());

        return list;
    }

    boolean compare(PhoneNumber localPhoneNumber, PhoneNumber transactionSummaryPhoneNumber) {
        if(localPhoneNumber == null || !localPhoneNumber.isValid() ||
                transactionSummaryPhoneNumber == null || !transactionSummaryPhoneNumber.isValid()) { return false; }

        String localNational = String.valueOf(localPhoneNumber.getNationalNumber());
        String transactionSummaryNational = String.valueOf(transactionSummaryPhoneNumber.getNationalNumber());
        return transactionSummaryNational.contains(localNational);
    }

    @CoinkeeperApplicationScope
    public static class TransactionSummaryCountDataSource {
        private long numberOfTransactions = 0l;
        private DaoSessionManager manager;

        @Inject
        public TransactionSummaryCountDataSource(DaoSessionManager manager) {
            this.manager = manager;
        }

        public boolean shouldAttemptToMapContacts() {
            long numberOfTransactions = manager.getTransactionSummaryDao().queryBuilder().count();

            if (this.numberOfTransactions == 0l || this.numberOfTransactions < numberOfTransactions) {
                this.numberOfTransactions = numberOfTransactions;
                return true;
            }

            return false;
        }
    }
}