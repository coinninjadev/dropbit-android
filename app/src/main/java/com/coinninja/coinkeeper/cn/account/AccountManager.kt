package com.coinninja.coinkeeper.cn.account

import app.dropbit.annotations.Mockable
import com.coinninja.bindings.DerivationPath
import com.coinninja.coinkeeper.cn.wallet.HDWallet
import com.coinninja.coinkeeper.model.dto.AddressDTO
import com.coinninja.coinkeeper.model.helpers.AddressHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import javax.inject.Inject

@Mockable
class AccountManager @Inject internal constructor(
        internal val hdWallet: HDWallet,
        internal val walletHelper: WalletHelper,
        internal val addressCache: AddressCache,
        internal val addressHelper: AddressHelper
) {

    val nextReceiveAddress: String
        get() {
            val addresses = addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)
            return if (addresses.isNotEmpty()) addresses[0].address else hdWallet.getAddressForPath(derivationPathForExternalIndex(0))
        }

    val nextReceiveIndex: Int
        get() {
            val addresses = addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)
            return if (addresses.isNotEmpty()) addresses[0].index else 0
        }

    val nextChangeIndex: Int
        get() {
            val addresses = addressHelper.getUnusedAddressesFor(HDWallet.INTERNAL)
            return if (addresses.isNotEmpty()) addresses[0].index else 0
        }

    val largestReportedChangeAddress: Int
        get() = addressHelper.getLargestDerivationIndexReportedFor(HDWallet.INTERNAL)

    val largestReportedReceiveAddress: Int
        get() = addressHelper.getLargestDerivationIndexReportedFor(HDWallet.EXTERNAL)

    private fun derivationPathForExternalIndex(index: Int): DerivationPath {
        val wallet = walletHelper.wallet
        return DerivationPath(wallet.purpose, wallet.coinType, wallet.accountIndex, HDWallet.EXTERNAL, index)
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
        addressCache.cacheAddressesFor(HDWallet.EXTERNAL)
        addressCache.cacheAddressesFor(HDWallet.INTERNAL)
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
