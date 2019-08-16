package com.coinninja.coinkeeper.cn.wallet

import app.dropbit.annotations.Mockable
import com.coinninja.bindings.DecryptionKeys
import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.EncryptionKeys
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import javax.inject.Inject

@CoinkeeperApplicationScope
@Deprecated("")
@Mockable
class HDWallet @Inject internal constructor(
        internal val libBitcoinProvider: LibBitcoinProvider,
        internal val walletHelper: WalletHelper
) {

    @Suppress("UNCHECKED_CAST")
    fun fillBlock(chainIndex: Int, startingIndex: Int, bufferSize: Int): Array<String> {
        val wallet = walletHelper.wallet
        var index = startingIndex
        val libbitcoin = libBitcoinProvider.provide()
        val block = mutableListOf<String>()
        if (chainIndex == EXTERNAL || chainIndex == INTERNAL) {
            for (i in 0 until bufferSize) {
                block.add(libbitcoin.getAddressForPath(
                        DerivationPath(wallet.purpose, wallet.coinType, wallet.accountIndex, chainIndex, index)
                ))
                index++
            }
        }

        return block.toTypedArray()
    }

    fun getUncompressedPublicKey(path: DerivationPath): String {
        return libBitcoinProvider.provide().getUncompressedPublicKeyHex(path)
    }

    fun isBase58CheckEncoded(address: String): Boolean {
        return libBitcoinProvider.provide().isBase58CheckEncoded(address)
    }

    fun generateEncryptionKeys(publicKey: String): EncryptionKeys {
        return libBitcoinProvider.provide().getEncryptionKeys(publicKey)
    }

    fun generateDecryptionKeys(derivationPath: DerivationPath, ephemeralPublicKey: ByteArray): DecryptionKeys {
        return libBitcoinProvider.provide().getDecryptionKeys(derivationPath, ephemeralPublicKey)
    }

    fun getAddressForPath(path: DerivationPath): String {
        return libBitcoinProvider.provide().getAddressForPath(path)
    }

    companion object {
        const val EXTERNAL = 0
        const val INTERNAL = 1
    }
}
