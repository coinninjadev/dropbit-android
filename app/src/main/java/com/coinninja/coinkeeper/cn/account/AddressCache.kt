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
        @NumAddressesToCache internal val numAddressesToCache: Int
) {
    fun getUncompressedPublicKey(path: DerivationPath): String {
        return hdWallet.getAddressForPath(path).pubKey
    }

    /**
     * @param chainIndex EXTERNAL=0, INTERNAL=1
     */
    internal fun cacheAddressesFor(wallet: Wallet, chainIndex: Int) {
        if (!shouldCacheAddressesForChain(wallet, chainIndex)) return
        val addresses: Array<MetaAddress> = hdWallet.fillBlock(wallet,
                wallet.purpose, wallet.coinType, wallet.accountIndex, chainIndex, 0,
                largestAddressIndexReportedFor(wallet, chainIndex) + numAddressesToCache
        )

        addresses.forEach {
            addressHelper.saveAddress(wallet, it)
        }
    }

    /**
     * @param chainIndex EXTERNAL=0, INTERNAL=1
     */
    private fun largestAddressIndexReportedFor(wallet: Wallet, chainIndex: Int): Int {
        return when (chainIndex) {
            HDWalletWrapper.EXTERNAL -> wallet.externalIndex
            HDWalletWrapper.INTERNAL -> wallet.internalIndex
            else -> throw IllegalArgumentException("Expected 0 or 1")
        }
    }

    private fun shouldCacheAddressesForChain(wallet: Wallet, chainIndex: Int): Boolean {
        val numAddressesToHaveCached = calcNumAddressesToHaveCached(wallet, chainIndex)
        return addressHelper.getAddressCountFor(wallet, chainIndex) < numAddressesToHaveCached
    }


    private fun calcNumAddressesToHaveCached(wallet: Wallet, chainIndex: Int): Int {
        return largestAddressIndexReportedFor(wallet, chainIndex) + numAddressesToCache
    }
}