package com.coinninja.coinkeeper.cn.account

import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.model.db.Wallet
import com.coinninja.coinkeeper.model.dto.AddressDTO
import com.coinninja.coinkeeper.model.helpers.AddressHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import javax.inject.Inject

@Mockable
class AccountManager @Inject internal constructor(
        internal val hdWallet: HDWalletWrapper,
        internal val walletHelper: WalletHelper,
        internal val addressCache: AddressCache,
        internal val addressHelper: AddressHelper
) {

    val nextReceiveAddress: String
        get() {
            val addresses = addressHelper.getUnusedAddressesFor(walletHelper.primaryWallet, HDWalletWrapper.EXTERNAL)
            return if (addresses.isNotEmpty()) addresses[0].address else hdWallet.getAddressForPath(derivationPathForExternalIndex(0)).address
        }

    val nextReceiveIndex: Int
        get() {
            val addresses = addressHelper.getUnusedAddressesFor(walletHelper.primaryWallet, HDWalletWrapper.EXTERNAL)
            return if (addresses.isNotEmpty()) addresses[0].index else 0
        }

    val nextChangeIndex: Int
        get() {
            val addresses = addressHelper.getUnusedAddressesFor(walletHelper.primaryWallet, HDWalletWrapper.INTERNAL)
            return if (addresses.isNotEmpty()) addresses[0].index else 0
        }

    fun largestReportedChangeAddress(wallet: Wallet): Int = addressHelper.getLargestDerivationIndexReportedFor(wallet, HDWalletWrapper.INTERNAL)

    fun largestReportedReceiveAddress(wallet: Wallet): Int = addressHelper.getLargestDerivationIndexReportedFor(wallet, HDWalletWrapper.EXTERNAL)

    private fun derivationPathForExternalIndex(index: Int): DerivationPath {
        val wallet = walletHelper.primaryWallet
        return DerivationPath(wallet.purpose, wallet.coinType, wallet.accountIndex, HDWalletWrapper.EXTERNAL, index)
    }

    fun reportLargestReceiveIndexConsumed(wallet: Wallet, index: Int) {
        if (wallet.externalIndex <= index) {
            wallet.externalIndex = index
            wallet.update()
        }
    }

    fun reportLargestChangeIndexConsumed(wallet: Wallet, index: Int) {
        if (wallet.internalIndex <= index) {
            wallet.internalIndex = index
            wallet.update()
        }
    }

    fun cacheAddresses(wallet: Wallet) {
        addressCache.cacheAddressesFor(wallet, HDWalletWrapper.EXTERNAL)
        addressCache.cacheAddressesFor(wallet, HDWalletWrapper.INTERNAL)
    }

    fun unusedAddressesToPubKey(chainIndex: Int, blockSize: Int): HashMap<String, AddressDTO> {
        val unusedAddresses = addressHelper.getUnusedAddressesFor(walletHelper.primaryWallet, chainIndex)
        val addressToDTO = HashMap<String, AddressDTO>()
        val size = if (blockSize <= unusedAddresses.size) blockSize else unusedAddresses.size

        for (i in 0 until size) {
            val address = unusedAddresses[i]
            addressToDTO[address.address] = AddressDTO(address, addressCache.getUncompressedPublicKey(address.derivationPath))
        }

        return addressToDTO
    }
}
