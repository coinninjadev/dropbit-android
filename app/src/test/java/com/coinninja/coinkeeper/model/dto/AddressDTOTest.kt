package com.coinninja.coinkeeper.model.dto

import app.coinninja.cn.libbitcoin.model.DerivationPath
import com.coinninja.coinkeeper.model.db.Address
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class AddressDTOTest {

    @Test
    fun test_display_derivation_path() {
        val pubKey = "akjsdha3kjrhaecnb3rkjbef"
        val addr = "-- address --"
        val address: Address = mock()
        whenever(address.derivationPath).thenReturn(DerivationPath(49, 0, 0, 0, 44))
        whenever(address.address).thenReturn(addr)
        val addressDTO = AddressDTO(address, pubKey)
        assertThat(addressDTO.derivationPath).isEqualTo("M/49/0/0/0/44")
        assertThat(addressDTO.address).isEqualTo(addr)
        assertThat(addressDTO.uncompressedPublicKey).isEqualTo(pubKey)
    }
}