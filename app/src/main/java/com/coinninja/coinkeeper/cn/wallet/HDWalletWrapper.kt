package com.coinninja.coinkeeper.cn.wallet

import app.coinninja.cn.libbitcoin.enum.Network
import app.coinninja.cn.libbitcoin.model.*
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import app.coinninja.cn.libbitcoin.HDWallet
import com.coinninja.coinkeeper.model.db.Wallet

@Mockable
class HDWalletWrapper constructor(
        val walletHelper: WalletHelper,
        val walletConfiguration: WalletConfiguration
) {

    val network:Network get() = if (walletConfiguration.isTestNet)
            Network.TESTNET
        else
            Network.MAINNET

    private val wallet: HDWallet get() = HDWallet(walletHelper.seedWords, network)

    val signingKey: String
        get() =
            wallet.signingKey
    val verificationKey: String
        get() =
            wallet.verificationKey

    fun getAddressForPath(path: DerivationPath): MetaAddress =
            wallet.getAddressForPath(path)

    fun getAddressForSegwitUpgrade(wallet: Wallet, path:DerivationPath):MetaAddress =
        HDWallet(walletHelper.getSeedWordsForWallet(wallet), network).getAddressForPath(path)

    fun verificationKeyFor(wallet: Wallet):String =
            HDWallet(walletHelper.getSeedWordsForWallet(wallet), network).verificationKey

    fun sign(wallet: Wallet, data:String):String =
            HDWallet(walletHelper.getSeedWordsForWallet(wallet), network).sign(data)

    fun sign(data: String): String = wallet.sign(data)

    fun fillBlock(wallet:Wallet, purpose: Int, coin: Int, account: Int, chainIndex: Int,
                  startingIndex: Int, bufferSize: Int
    ): Array<MetaAddress> = HDWallet(walletHelper.getSeedWordsForWallet(wallet), network)
            .fillBlock(purpose, coin, account, chainIndex, startingIndex, bufferSize)

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