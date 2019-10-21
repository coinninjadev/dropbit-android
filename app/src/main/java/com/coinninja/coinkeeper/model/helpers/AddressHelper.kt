package com.coinninja.coinkeeper.model.helpers

import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.MetaAddress
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.Address.addressesIn
import com.coinninja.coinkeeper.service.client.model.GsonAddress
import java.util.*
import javax.inject.Inject

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

    fun addressForPath(derivationPath: DerivationPath): Address? =
            daoSessionManager.addressDao.queryBuilder()
                    .where(
                            AddressDao.Properties.ChangeIndex.eq(derivationPath.chain),
                            AddressDao.Properties.Index.eq(derivationPath.index)
                    )
                    .unique()

    fun addAddresses(wallet:Wallet, addresses: List<GsonAddress>, changeIndex: Int): List<Address> {
        val savedAddresses: MutableList<String> = mutableListOf()

        addresses.forEach {
            if (!savedAddresses.contains(it.address) && !containsAddress(it.address)) {
                savedAddresses.add(it.address)
                daoSessionManager.newAddressFrom(it, wallet, changeIndex)
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

    fun getAddressCountFor(wallet: Wallet, chainIndex: Int): Int {
        return daoSessionManager.addressDao.queryBuilder()
                .where(
                        AddressDao.Properties.WalletId.eq(wallet.id),
                        AddressDao.Properties.ChangeIndex.eq(chainIndex)
                ).count().toInt()
    }

    fun saveAddress(wallet:Wallet, metaAddress: MetaAddress) {
        metaAddress.derivationPath?.let { derivationPath ->
            var address: Address? = daoSessionManager.addressDao.queryBuilder()
                    .where(
                            AddressDao.Properties.ChangeIndex.eq(derivationPath.chain),
                            AddressDao.Properties.Index.eq(derivationPath.index),
                            AddressDao.Properties.Address.eq(metaAddress.address)
                    )
                    .unique()

            if (null != address) return

            address = Address()
            address.address = metaAddress.address
            address.wallet = wallet
            address.changeIndex = derivationPath.chain
            address.index = derivationPath.index
            daoSessionManager.addressDao.insert(address)

        }
    }

    fun getUnusedAddressesFor(wallet: Wallet, chainIndex: Int): List<Address> {
        return daoSessionManager.addressDao.queryRaw("""
                LEFT JOIN TARGET_STAT on T._id = TARGET_STAT.ADDRESS_ID 
                LEFT JOIN FUNDING_STAT on T._id = FUNDING_STAT.ADDRESS_ID 
                WHERE T.CHANGE_INDEX =  $chainIndex 
                AND T.WALLET_ID = ${wallet.id}
                AND  FUNDING_STAT.ADDRESS_ID IS NULL 
                AND TARGET_STAT.ADDRESS_ID IS NULL 
        """)
    }

    fun getLargestDerivationIndexReportedFor(wallet: Wallet, chainIndex: Int): Int {
        val chainAddresses = daoSessionManager.addressDao.queryRaw("""
                LEFT JOIN TARGET_STAT on T._id = TARGET_STAT.ADDRESS_ID
                LEFT JOIN FUNDING_STAT on T._id = FUNDING_STAT.ADDRESS_ID
                WHERE T.CHANGE_INDEX = $chainIndex
                AND T.WALLET_ID = ${wallet.id}
                AND FUNDING_STAT.ADDRESS_ID IS NOT NULL 
                OR TARGET_STAT.ADDRESS_ID IS NOT NULL 
            
        """
        )

        val comparator = { e1: Address, e2: Address -> e1.index.compareTo(e2.index) }
        Collections.sort(chainAddresses, comparator)
        chainAddresses.reverse()

        return if (chainAddresses.size > 0) chainAddresses[0].index else 0
    }


}
