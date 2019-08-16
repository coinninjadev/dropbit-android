package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.service.client.model.GsonAddress
import org.greenrobot.greendao.query.QueryBuilder
import javax.inject.Singleton

@Singleton
@Mockable
class DaoSessionManager(
        internal val daoMaster: DaoMaster,
        internal val purpose: Int = 49,
        internal val coinType: Int = 0,
        internal val accountIndex: Int = 0
) {
    internal lateinit var daoSession: DaoSession

    // USER
    val userDao: UserDao get() = daoSession.userDao

    // WALLET
    val walletDao: WalletDao get() = daoSession.walletDao

    fun createWallet(): Wallet {
        resetAll()
        val user = User()
        val id = userDao.insert(user)
        val wallet = Wallet().also {
            it.userId = id
            it.purpose = purpose
            it.coinType = coinType
            it.accountIndex = accountIndex
        }
        val walletId = walletDao.insert(wallet)
        wallet.id = walletId
        wallet.__setDaoSession(daoSession)
        return wallet
    }

    // WORD
    val wordDao: WordDao get() = daoSession.wordDao

    // ADDRESS
    val addressDao: AddressDao get() = daoSession.addressDao

    fun newAddressFrom(gsonAddress: GsonAddress, wallet: Wallet, changeIndex: Int): Address {
        val address = Address()
        address.wallet = wallet
        address.address = gsonAddress.address
        address.changeIndex = gsonAddress.derivationIndex
        address.changeIndex = changeIndex
        address.index = gsonAddress.derivationIndex
        daoSession.addressDao.insert(address)
        return address
    }

    // TARGET STAT
    val targetStatDao: TargetStatDao get() = daoSession.targetStatDao

    // FUNDING STAT
    val fundingStatDao: FundingStatDao get() = daoSession.fundingStatDao

    // TRANSACTION INVITE SUMMARY
    val transactionsInvitesSummaryDao: TransactionsInvitesSummaryDao get() = daoSession.transactionsInvitesSummaryDao

    // TRANSACTION
    val transactionSummaryDao: TransactionSummaryDao get() = daoSession.transactionSummaryDao


    val transactionNotificationDao: TransactionNotificationDao get() = daoSession.transactionNotificationDao

    // DROPBIT
    val inviteTransactionSummaryDao: InviteTransactionSummaryDao get() = daoSession.inviteTransactionSummaryDao

    // ACCOUNT
    val accountDao: AccountDao get() = daoSession.accountDao

    // INTERNAL NOTIFICATION
    val internalNotificationDao: InternalNotificationDao
        get() = daoSession.internalNotificationDao

    val externalNotificationDao: ExternalNotificationDao
        get() = daoSession.externalNotificationDao

    val broadcastBtcInviteDao: BroadcastBtcInviteDao
        get() = daoSession.broadcastBtcInviteDao

    // Dropbit Me Identity
    val dropbitMeIdentityDao: DropbitMeIdentityDao get() = daoSession.dropbitMeIdentityDao

    val userIdentityDao: UserIdentityDao get() = daoSession.userIdentityDao

    fun clear() {
        daoSession.clear()
    }

    fun connect(): DaoSessionManager {
        daoSession = daoMaster.newSession()
        return this
    }

    fun resetAll() {
        clear()
        dropAllTables()
        createAllTables()
        connect()
    }

    fun runRaw(query: String) {
        daoSession.database.execSQL(query)
    }

    fun qeuryForWallet(): QueryBuilder<Wallet> {
        return walletDao.queryBuilder()
    }

    // Account
    fun insert(account: Account): Long {
        val id = accountDao.insert(account)
        account.__setDaoSession(daoSession)
        account.id = id
        return id
    }

    fun insert(targetStat: TargetStat): Long {
        val id = targetStatDao.insert(targetStat)
        targetStat.id = id
        targetStat.__setDaoSession(daoSession)
        return id
    }

    fun newTargetStat(): TargetStat {
        return TargetStat()
    }

    fun newFundingStat(): FundingStat {
        return FundingStat()
    }

    fun insert(fundingStat: FundingStat): Long {
        val id = daoSession.insert(fundingStat)
        fundingStat.id = id
        fundingStat.__setDaoSession(daoSession)
        return id
    }

    fun newTransactionInviteSummary(): TransactionsInvitesSummary {
        return TransactionsInvitesSummary()
    }

    fun insert(summary: TransactionsInvitesSummary): Long {
        val id = transactionsInvitesSummaryDao.insert(summary)
        summary.id = id
        summary.__setDaoSession(daoSession)
        return id
    }

    fun insert(transaction: TransactionSummary): Long {
        val id = transactionSummaryDao.insert(transaction)
        transaction.id = id
        transaction.__setDaoSession(daoSession)
        return id
    }

    fun attach(transactionSummary: TransactionSummary) {
        transactionSummary.__setDaoSession(daoSession)
    }

    fun newTransactionSummary(): TransactionSummary {
        return TransactionSummary()
    }

    // TRANSACTION NOTIFICATION
    fun insert(notification: TransactionNotification): Long {
        return transactionNotificationDao.insert(notification)
    }

    fun newTransactionNotification(): TransactionNotification {
        return TransactionNotification()
    }

    fun insert(inviteTransactionSummary: InviteTransactionSummary): Long {
        val id = daoSession.inviteTransactionSummaryDao.insert(inviteTransactionSummary)
        inviteTransactionSummary.__setDaoSession(daoSession)
        inviteTransactionSummary.id = id
        return id
    }

    fun newInviteTransactionSummary(): InviteTransactionSummary {
        return InviteTransactionSummary()
    }

    fun newDropbitMeIdentity(): DropbitMeIdentity {
        return DropbitMeIdentity()
    }

    fun insert(dropbitMeIdentity: DropbitMeIdentity): Long {
        val id = dropbitMeIdentityDao.insert(dropbitMeIdentity)
        dropbitMeIdentity.__setDaoSession(daoSession)
        dropbitMeIdentity.id = id
        return id
    }

    // UserIdentity
    fun newUserIdentity(): UserIdentity {
        return UserIdentity()
    }

    fun insert(userIdentity: UserIdentity): Long {
        val id = daoSession.insert(userIdentity)
        userIdentity.id = id
        userIdentity.__setDaoSession(daoSession)
        return id
    }

    fun clearCacheFor(transaction: TransactionSummary) {
        //Clear cache of to-many relations
        transaction.resetFunder()
        transaction.resetReceiver()
    }

    private fun dropAllTables() {
        val db = daoSession.database
        DaoMaster.dropAllTables(db, true)
    }

    private fun createAllTables() {
        val db = daoSession.database
        DaoMaster.createAllTables(db, true)
    }

}
