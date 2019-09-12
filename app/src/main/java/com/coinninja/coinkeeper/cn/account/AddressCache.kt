package com.coinninja.coinkeeper.cn.account

import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.MetaAddress
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.di.interfaces.NumAddressesToCache
import com.coinninja.coinkeeper.model.db.Wallet
import com.coinninja.coinkeeper.model.helpers.AddressHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import javax.inject.Inject

@Mockable
class AddressCache @Inject internal constructor(
        internal val hdWallet: HDWalletWrapper,
        internal val addressHelper: AddressHelper,
        internal val walletHelper: WalletHelper,
        @param:NumAddressesToCache internal val numAddressesToCache: Int
) {
    fun getUncompressedPublicKey(path: DerivationPath): String {
        return hdWallet.getAddressForPath(path).pubKey
    }

    /**
     * @param chainIndex EXTERNAL=0, INTERNAL=1
     */
    internal fun cacheAddressesFor(chainIndex: Int) {
        if (!shouldCacheAddressesForChain(chainIndex)) return
        val wallet: Wallet = walletHelper.wallet
        val addresses: Array<MetaAddress> = hdWallet.fillBlock(
                wallet.purpose, wallet.coinType, wallet.accountIndex, chainIndex, 0,
                largestAddressIndexReportedFor(chainIndex) + numAddressesToCache
        )

        addresses.forEach {
            addressHelper.saveAddress(it)
        }
    }

    /**
     * @param chainIndex EXTERNAL=0, INTERNAL=1
     */
    private fun largestAddressIndexReportedFor(chainIndex: Int): Int {
        return when (chainIndex) {
            HDWalletWrapper.EXTERNAL -> walletHelper.currentExternalIndex
            HDWalletWrapper.INTERNAL -> walletHelper.currentInternalIndex
            else -> throw IllegalArgumentException("Expected 0 or 1")
        }
    }

    private fun shouldCacheAddressesForChain(chainIndex: Int): Boolean {
        val numAddressesToHaveCached = calcNumAddressesToHaveCached(chainIndex)
        return addressHelper.getAddressCountFor(chainIndex) < numAddressesToHaveCached
    }


    private fun calcNumAddressesToHaveCached(chainIndex: Int): Int {
        return largestAddressIndexReportedFor(chainIndex) + numAddressesToCache
    }
}