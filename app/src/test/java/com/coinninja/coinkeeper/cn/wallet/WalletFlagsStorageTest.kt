package com.coinninja.coinkeeper.cn.wallet

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class WalletFlagsStorageTest {

    @Test
    fun saves_flags_in_storage() {
        val storage = WalletFlagsStorage(mock())
        val walletFlags = WalletFlags.compose(WalletFlags.purpose49, WalletFlags.v1)

        storage.flags = walletFlags.flag

        verify(storage.preferencesUtils).savePreference(WalletFlagsStorage.key, walletFlags.flag)
    }

    @Test
    fun retrieves_flags_from_storage() {
        val storage = WalletFlagsStorage(mock())
        val walletFlags = WalletFlags.compose(WalletFlags.purpose49, WalletFlags.v1)

        whenever(storage.preferencesUtils.getLong(WalletFlagsStorage.key, 0)).thenReturn(walletFlags.flag)

        assertThat(storage.flags).isEqualTo(1)
    }
}