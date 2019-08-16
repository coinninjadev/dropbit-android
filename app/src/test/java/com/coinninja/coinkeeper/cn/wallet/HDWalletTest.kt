package com.coinninja.coinkeeper.cn.wallet

import com.coinninja.bindings.DecryptionKeys
import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.EncryptionKeys
import com.coinninja.bindings.Libbitcoin
import com.coinninja.coinkeeper.cn.wallet.data.HDWalletTestData
import com.coinninja.coinkeeper.model.db.Wallet
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class HDWalletTest {

    private val libbitcoin: Libbitcoin = mock()
    private val wallet: Wallet = mock()

    private fun createHDWwallet(): HDWallet {
        val hdWallet = HDWallet(mock(), mock())
        whenever(hdWallet.libBitcoinProvider.provide()).thenReturn(libbitcoin)
        whenever(hdWallet.walletHelper.wallet).thenReturn(wallet)
        whenever(wallet.purpose).thenReturn(49)
        whenever(wallet.coinType).thenReturn(1)
        whenever(wallet.accountIndex).thenReturn(0)
        return hdWallet
    }

    @Test
    fun fillsExternalBlocksAtStartingIndex_external() {
        val wallet = createHDWwallet()
        val bufferSize = 50
        prepareAddresses(HDWallet.EXTERNAL, bufferSize)
        val addresses = wallet.fillBlock(HDWallet.EXTERNAL, 0, bufferSize)

        assertThat(addresses).isEqualTo(HDWalletTestData.blockOneExternalAddresses)
    }

    @Test
    fun fillsInternalBlocksAtStartingIndex_internal() {
        val wallet = createHDWwallet()
        val bufferSize = 50
        prepareAddresses(HDWallet.INTERNAL, bufferSize)
        val addresses = wallet.fillBlock(HDWallet.INTERNAL, 0, bufferSize)


        assertThat(addresses).isEqualTo(HDWalletTestData.blockOneInternalAddresses)
    }

    @Test
    fun returns_address_for_derivation_path() {
        val wallet = createHDWwallet()
        val address = "--ex-address"
        val derivationPath = DerivationPath(49, 0, 0, 0, 0)
        whenever(libbitcoin.getAddressForPath(derivationPath)).thenReturn(address)

        assertThat(wallet.getAddressForPath(derivationPath)).isEqualTo(address)
    }

    @Test
    fun generateEncryptionKeys() {
        val wallet = createHDWwallet()
        val publicKey = "public"
        val encryptionKeys = EncryptionKeys("".toByteArray(), "".toByteArray(), "".toByteArray())
        whenever(libbitcoin.getEncryptionKeys(publicKey)).thenReturn(encryptionKeys)

        val keys = wallet.generateEncryptionKeys(publicKey)
        assertThat(keys).isEqualTo(encryptionKeys)
    }

    @Test
    fun generateDecryptionKeys() {
        val wallet = createHDWwallet()
        val decryptionKeys = DecryptionKeys("".toByteArray(), "".toByteArray())
        val derivationPath = DerivationPath("m/49/0/0/0/1")
        val ephemeralPublicKey = "ephemeralPublicKey".toByteArray()

        whenever(libbitcoin.getDecryptionKeys(derivationPath, ephemeralPublicKey)).thenReturn(decryptionKeys)

        val keys = wallet.generateDecryptionKeys(derivationPath, ephemeralPublicKey)

        assertThat(keys).isEqualTo(decryptionKeys)
    }

    private fun prepareAddresses(chainIndex: Int, bufferSize: Int) {
        if (chainIndex == HDWallet.EXTERNAL) {
            for (i in 0 until bufferSize) {
                whenever(libbitcoin.getAddressForPath(DerivationPath(wallet.purpose, wallet.coinType, wallet.accountIndex, HDWallet.EXTERNAL, i))).thenReturn(HDWalletTestData.blockOneExternalAddresses[i])
            }
        } else if (chainIndex == HDWallet.INTERNAL) {
            for (i in 0 until bufferSize) {
                whenever(libbitcoin.getAddressForPath(DerivationPath(wallet.purpose, wallet.coinType, wallet.accountIndex, HDWallet.INTERNAL, i))).thenReturn(HDWalletTestData.blockOneInternalAddresses[i])
            }

        }
    }
}