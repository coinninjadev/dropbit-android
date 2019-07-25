package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.Address
import com.coinninja.coinkeeper.model.db.Address.addressesIn
import com.coinninja.coinkeeper.model.db.AddressDao
import com.coinninja.coinkeeper.model.db.FundingStatDao
import com.coinninja.coinkeeper.model.db.TargetStatDao
import com.coinninja.coinkeeper.service.client.model.GsonAddress
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@Mockable
class AddressHelper @Inject constructor(
        internal val daoSessionManager: DaoSessionManager,
        internal val walletHelper: WalletHelper
) {
    private fun containsAddress(address: String): Boolean {
        return daoSessionManager.addressDao.queryBuilder()
                .where(AddressDao.Properties.Address.eq(address)).count() > 0
    }

    fun addressForPubKey(address: String): Address? =
            daoSessionManager.addressDao.queryBuilder()
                    .where(AddressDao.Properties.Address.eq(address)).unique()

    fun addAddresses(addresses: List<GsonAddress>, changeIndex: Int): List<Address> {
        val savedAddresses: ArrayList<String> = ArrayList()

        addresses.forEach loop@{
            val addressValue = it.address

            if (savedAddresses.indexOf(addressValue) == 0 || !containsAddress(addressValue)) {
                savedAddresses.add(addressValue)
                daoSessionManager.newAddressFrom(it, walletHelper.wallet, changeIndex)
            }
        }

        return addressesIn(daoSessionManager.addressDao, savedAddresses)
    }


    fun updateSpentTransactions() {
        val targets = daoSessionManager.targetStatDao.queryBuilder().where(
                TargetStatDao.Properties.AddressId.isNotNull,
                TargetStatDao.Properties.FundingId.isNull).list()

        for (target in targets) {
            val fundingStat = daoSessionManager.fundingStatDao.queryBuilder().where(
                    FundingStatDao.Properties.Position.eq(target.position),
                    FundingStatDao.Properties.Value.eq(target.value),
                    FundingStatDao.Properties.FundedTransaction.eq(target.transaction.txid)
            ).unique()

            if (fundingStat != null) {
                target.refresh()
                target.fundingStat = fundingStat
                target.update()

                fundingStat.refresh()
                fundingStat.targetStat = target
                fundingStat.update()
            }
        }
    }

    fun hasReceivedTransaction(): Boolean {
        var hasReceived = false
        val list = daoSessionManager.addressDao.queryBuilder().where(
                AddressDao.Properties.ChangeIndex.eq(0)
        ).list()

        for (address in list) {
            if (address.targets.size > 0) {
                for (target in address.targets) {
                    val transaction = target.transaction
                    if (transaction == null || transaction.transactionsInvitesSummary == null)
                        continue

                    if (transaction.transactionsInvitesSummary.inviteTransactionSummary == null) {
                        hasReceived = true
                        break
                    }
                }
            }
        }

        return hasReceived
    }

    fun getAddressCountFor(chainIndex: Int): Int {
        return daoSessionManager.addressDao.queryBuilder()
                .where(AddressDao.Properties.ChangeIndex.eq(chainIndex)).count().toInt()
    }

    fun saveAddress(chainIndex: Int, derivationIndex: Int, addr: String) {
        var address: Address? = daoSessionManager.addressDao.queryBuilder()
                .where(
                        AddressDao.Properties.ChangeIndex.eq(chainIndex),
                        AddressDao.Properties.Index.eq(derivationIndex),
                        AddressDao.Properties.Address.eq(addr)
                )
                .unique()

        if (null != address) return

        address = Address()
        address.address = addr
        address.wallet = walletHelper.wallet
        address.changeIndex = chainIndex
        address.index = derivationIndex
        daoSessionManager.addressDao.insert(address)
    }

    fun getUnusedAddressesFor(chainIndex: Int): List<Address> {
        return daoSessionManager.addressDao.queryRaw("" +
                " LEFT JOIN TARGET_STAT on T._id = TARGET_STAT.ADDRESS_ID" +
                " LEFT JOIN FUNDING_STAT on T._id = FUNDING_STAT.ADDRESS_ID" +
                " WHERE T.CHANGE_INDEX = " + chainIndex.toString() +
                " AND  FUNDING_STAT.ADDRESS_ID IS NULL " +
                " AND TARGET_STAT.ADDRESS_ID IS NULL "
        )
    }

    fun getLargestDerivationIndexReportedFor(chainIndex: Int): Int {
        val chainAddresses = daoSessionManager.addressDao.queryRaw("" +
                " LEFT JOIN TARGET_STAT on T._id = TARGET_STAT.ADDRESS_ID" +
                " LEFT JOIN FUNDING_STAT on T._id = FUNDING_STAT.ADDRESS_ID" +
                " WHERE T.CHANGE_INDEX = " + chainIndex.toString() +
                " AND FUNDING_STAT.ADDRESS_ID IS NOT NULL " +
                " OR TARGET_STAT.ADDRESS_ID IS NOT NULL "
        )

        val comparator = { e1: Address, e2: Address -> e1.index.compareTo(e2.index) }
        Collections.sort(chainAddresses, comparator)
        chainAddresses.reverse()

        return if (chainAddresses.size > 0) chainAddresses[0].index else 0
    }

}
