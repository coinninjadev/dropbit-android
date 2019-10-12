package com.coinninja.coinkeeper.cn.account

import app.coinninja.cn.libbitcoin.HDWallet
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.MetaAddress
import com.coinninja.coinkeeper.model.db.Address
import com.coinninja.coinkeeper.model.db.Wallet
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import java.util.*

class AccountManagerTest {

    private fun createManager(): AccountManager = AccountManager(mock(), mock(), mock(), mock())

    @Test
    fun instructs_address_cache_to_build_for_both_internal_and_external_chains() {
        val accountManager = createManager()
        accountManager.cacheAddresses()

        verify(accountManager.addressCache).cacheAddressesFor(HDWallet.EXTERNAL)
        verify(accountManager.addressCache).cacheAddressesFor(HDWallet.INTERNAL)
    }

    @Test
    fun reportLargestChangeIndexConsumed_when_less_than_current() {
        val accountManager = createManager()
        whenever(accountManager.walletHelper.currentInternalIndex).thenReturn(22)
        accountManager.reportLargestChangeIndexConsumed(20)
        verify(accountManager.walletHelper, times(0)).setInternalIndex(any())
    }

    @Test
    fun reportLargestChangeIndexConsumed_when_same_as_current() {
        val accountManager = createManager()
        whenever(accountManager.walletHelper.currentInternalIndex).thenReturn(22)
        accountManager.reportLargestChangeIndexConsumed(22)
        verify(accountManager.walletHelper).setInternalIndex(23)
    }

    @Test
    fun reportLargestChangeIndexConsumed_when_greater_than_current() {
        val accountManager = createManager()
        whenever(accountManager.walletHelper.currentInternalIndex).thenReturn(22)
        accountManager.reportLargestChangeIndexConsumed(23)
        verify(accountManager.walletHelper).setInternalIndex(24)
    }

    @Test
    fun reportLargestReceiveIndexConsumed_when_less_than_current() {
        val accountManager = createManager()
        whenever(accountManager.walletHelper.currentExternalIndex).thenReturn(22)
        accountManager.reportLargestReceiveIndexConsumed(20)
        verify(accountManager.walletHelper, times(0)).setExternalIndex(any())
    }

    @Test
    fun reportLargestReceiveIndexConsumed_when_same_as_current() {
        val accountManager = createManager()
        whenever(accountManager.walletHelper.currentExternalIndex).thenReturn(22)
        accountManager.reportLargestReceiveIndexConsumed(22)
        verify(accountManager.walletHelper).setExternalIndex(23)
    }

    @Test
    fun reportLargestReceiveIndexConsumed_when_greater_than_current() {
        val accountManager = createManager()
        whenever(accountManager.walletHelper.currentExternalIndex).thenReturn(22)
        accountManager.reportLargestReceiveIndexConsumed(23)
        verify(accountManager.walletHelper).setExternalIndex(24)
    }

    @Test
    fun returns_first_receive_address_when_address_cache_empty() {
        val accountManager = createManager()
        val path = DerivationPath(49, 0, 0, 0, 0)
        val address = MetaAddress("--address--", "--pubkey--", path)
        val addresses = ArrayList<Address>()
        val wallet = mock<Wallet>()
        whenever(wallet.purpose).thenReturn(49)
        whenever(wallet.coinType).thenReturn(0)
        whenever(wallet.accountIndex).thenReturn(0)
        whenever(accountManager.addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses)
        whenever(accountManager.walletHelper.primaryWallet).thenReturn(wallet)
        whenever(accountManager.hdWallet.getAddressForPath(path)).thenReturn(address)

        assertThat(accountManager.nextReceiveAddress).isEqualTo(address.address)
    }

    @Test
    fun returns_next_change_address_from_hd_wallet_when_cache_is_empty() {
        val accountManager = createManager()
        val addresses = ArrayList<Address>()
        whenever(accountManager.addressHelper.getUnusedAddressesFor(HDWallet.INTERNAL)).thenReturn(addresses)

        assertThat(accountManager.nextChangeIndex).isEqualTo(0)
    }

    @Test
    fun returns_next_receive_address_from_hd_wallet_when_cache_is_empty() {
        val accountManager = createManager()
        assertThat(accountManager.nextChangeIndex).isEqualTo(0)
    }

    @Test
    fun returns_next_change_address() {
        val accountManager = createManager()
        val address1 = Address()
        address1.address = "---- address 1 ----"
        address1.index = 7
        val address2 = Address()
        address2.address = "---- address 2 ----"
        address2.index = 14
        val addresses = ArrayList<Address>()
        addresses.add(address1)
        addresses.add(address2)

        whenever(accountManager.addressHelper.getUnusedAddressesFor(HDWallet.INTERNAL)).thenReturn(addresses)

        assertThat(accountManager.nextChangeIndex).isEqualTo(7)
    }

    @Test
    fun returns_next_receive_address() {
        val accountManager = createManager()
        val address1 = Address()
        address1.address = "---- address 1 ----"
        address1.index = 5
        val address2 = Address()
        address2.address = "---- address 2 ----"
        address2.index = 14
        val addresses = ArrayList<Address>()
        addresses.add(address1)
        addresses.add(address2)

        whenever(accountManager.addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses)

        assertThat(accountManager.nextReceiveIndex).isEqualTo(5)
    }

    @Test
    fun returns_next_available_receive_address() {
        val accountManager = createManager()
        val address1 = Address()
        address1.address = "---- address 1 ----"
        address1.index = 0
        val address2 = Address()
        address2.address = "---- address 2 ----"
        address2.index = 14
        val addresses = ArrayList<Address>()
        addresses.add(address1)
        addresses.add(address2)

        whenever(accountManager.addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses)
        val nextAddress = "---- address 1 ----"
        val address = accountManager.nextReceiveAddress

        assertThat(address).isEqualTo(nextAddress)
    }

    @Test
    fun returns_array_of_addresses_for_requested_chain_and_block_size() {
        val accountManager = createManager()
        val addressOne = mock<Address>()
        whenever(addressOne.address).thenReturn("-- addr 1 --")
        whenever(addressOne.derivationPath).thenReturn(DerivationPath.from("M/49/0/0/0/0"))
        val addressTwo: Address = mock()
        whenever(addressTwo.address).thenReturn("-- addr 4 --")
        whenever(addressTwo.derivationPath).thenReturn(DerivationPath.from("M/49/0/0/0/4"))
        val addressThree: Address = mock()
        whenever(addressThree.address).thenReturn("-- addr 7 --")
        whenever(addressThree.derivationPath).thenReturn(DerivationPath.from("M/49/0/0/0/7"))
        val addressFour: Address = mock()
        whenever(addressFour.address).thenReturn("-- addr 8 --")
        whenever(addressFour.derivationPath).thenReturn(DerivationPath.from("M/49/0/0/0/8"))

        val addresses = listOf(addressOne, addressTwo, addressThree, addressFour)
        whenever(accountManager.addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses)
        whenever(accountManager.addressCache.getUncompressedPublicKey(any())).thenReturn("")

        val block = accountManager.unusedAddressesToPubKey(HDWallet.EXTERNAL, 3)

        assertThat(block.keys.size).isEqualTo(3)
        assertThat(block["-- addr 1 --"]!!.address).isEqualTo(addressOne.address)
        assertThat(block["-- addr 4 --"]!!.address).isEqualTo(addressTwo.address)
        assertThat(block["-- addr 7 --"]!!.address).isEqualTo(addressThree.address)
    }

    @Test
    fun returns_smaller_array_of_addresses_for_requested_chain_and_block_size_when_unused_limited() {
        val accountManager = createManager()
        val addressOne = mock<Address>()
        whenever(addressOne.address).thenReturn("-- addr 1 --")
        whenever(addressOne.derivationPath).thenReturn(DerivationPath.from("M/49/0/0/0/0"))
        val addressTwo: Address = mock()
        whenever(addressTwo.address).thenReturn("-- addr 4 --")
        whenever(addressTwo.derivationPath).thenReturn(DerivationPath.from("M/49/0/0/0/4"))

        val addresses = listOf(addressOne, addressTwo)
        whenever(accountManager.addressHelper.getUnusedAddressesFor(HDWallet.EXTERNAL)).thenReturn(addresses)
        whenever(accountManager.addressCache.getUncompressedPublicKey(any())).thenReturn("")

        val block = accountManager.unusedAddressesToPubKey(HDWallet.EXTERNAL, 3)

        assertThat(block.keys.size).isEqualTo(2)
        assertThat(block["-- addr 1 --"]!!.address).isEqualTo(addressOne.address)
        assertThat(block["-- addr 4 --"]!!.address).isEqualTo(addressTwo.address)
    }

}