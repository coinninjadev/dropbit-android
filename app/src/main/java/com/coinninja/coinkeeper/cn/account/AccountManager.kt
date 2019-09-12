package com.coinninja.coinkeeper.cn.account

import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
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
            val addresses = addressHelper.getUnusedAddressesFor(HDWalletWrapper.EXTERNAL)
            return if (addresses.isNotEmpty()) addresses[0].address else hdWallet.getAddressForPath(derivationPathForExternalIndex(0)).address
        }

    val nextReceiveIndex: Int
        get() {
            val addresses = addressHelper.getUnusedAddressesFor(HDWalletWrapper.EXTERNAL)
            return if (addresses.isNotEmpty()) addresses[0].index else 0
        }

    val nextChangeIndex: Int
        get() {
            val addresses = addressHelper.getUnusedAddressesFor(HDWalletWrapper.INTERNAL)
            return if (addresses.isNotEmpty()) addresses[0].index else 0
        }

    val largestReportedChangeAddress: Int
        get() = addressHelper.getLargestDerivationIndexReportedFor(HDWalletWrapper.INTERNAL)

    val largestReportedReceiveAddress: Int
        get() = addressHelper.getLargestDerivationIndexReportedFor(HDWalletWrapper.EXTERNAL)

    private fun derivationPathForExternalIndex(index: Int): DerivationPath {
        val wallet = walletHelper.wallet
        return DerivationPath(wallet.purpose, wallet.coinType, wallet.accountIndex, HDWalletWrapper.EXTERNAL, index)
    }

    //TODO can find the largest consumed from DB and use that as a reference point
    @Deprecated("")
    fun reportLargestReceiveIndexConsumed(index: Int) {
        if (walletHelper.currentExternalIndex <= index) {
            walletHelper.setExternalIndex(index + 1)
        }
    }

    //TODO can find the largest consumed from DB and use that as a reference point
    @Deprecated("")
    fun reportLargestChangeIndexConsumed(index: Int) {
        if (walletHelper.currentInternalIndex <= index) {
            walletHelper.setInternalIndex(index + 1)
        }
    }

    fun cacheAddresses() {
        addressCache.cacheAddressesFor(HDWalletWrapper.EXTERNAL)
        addressCache.cacheAddressesFor(HDWalletWrapper.INTERNAL)
    }

    fun unusedAddressesToPubKey(chainIndex: Int, blockSize: Int): HashMap<String, AddressDTO> {
        val unusedAddresses = addressHelper.getUnusedAddressesFor(chainIndex)
        val addressToDTO = HashMap<String, AddressDTO>()
        val size = if (blockSize <= unusedAddresses.size) blockSize else unusedAddresses.size

        for (i in 0 until size) {
            val address = unusedAddresses[i]
            addressToDTO[address.address] = AddressDTO(address, addressCache.getUncompressedPublicKey(address.derivationPath))
        }

        return addressToDTO
    }
}
