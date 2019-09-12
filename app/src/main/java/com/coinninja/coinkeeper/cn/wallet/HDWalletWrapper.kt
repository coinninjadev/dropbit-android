package com.coinninja.coinkeeper.cn.wallet

import app.coinninja.cn.libbitcoin.enum.Network
import app.coinninja.cn.libbitcoin.model.*
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import app.coinninja.cn.libbitcoin.HDWallet as Wallet

@Mockable
class HDWalletWrapper constructor(
        val walletHelper: WalletHelper,
        val walletConfiguration: WalletConfiguration
) {
    private val wallet: Wallet
        get() =
            Wallet(walletHelper.seedWords, if (walletConfiguration.isTestNet)
                Network.TESTNET
            else
                Network.MAINNET
            )

    val signingKey: String
        get() =
            wallet.signingKey
    val verificationKey: String
        get() =
            wallet.verificationKey

    fun getAddressForPath(path: DerivationPath): MetaAddress =
            wallet.getAddressForPath(path)

    fun sign(data: String): String = wallet.sign(data)

    fun fillBlock(purpose: Int, coin: Int, account: Int, chainIndex: Int,
                  startingIndex: Int, bufferSize: Int
    ): Array<MetaAddress> = wallet.fillBlock(purpose, coin, account, chainIndex, startingIndex, bufferSize)

    fun encryptionKeys(uncompressedPublicKey: ByteArray): EncryptionKeys =
            wallet.encryptionKeys(uncompressedPublicKey)

    fun decryptionKeys(derivationPath: DerivationPath, decoded: ByteArray): DecryptionKeys =
            wallet.decryptionKeys(derivationPath, decoded)

    fun base58encodedKey(): String = wallet.base58encodedKey()

    fun transactionFrom(transactionData: TransactionData): Transaction =
            wallet.transactionFrom(transactionData)

    companion object {
        const val EXTERNAL = 0
        const val INTERNAL = 1
    }
}