package com.coinninja.coinkeeper.util.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.enum.AddressType
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitcoinUtilTest {
    companion object {
        private val invalidWords = arrayOf("word1", "word2", "word3", "word4", "word5", "word6",
                "word7", "word8", "word9", "word10", "word11", "word12")
        private val validWords = arrayOf("abandon", "abandon", "abandon", "abandon", "abandon", "abandon",
                "abandon", "abandon", "abandon", "abandon", "abandon", "about")
    }

    private fun createUtil(): BitcoinUtil = BitcoinUtil(ApplicationProvider.getApplicationContext<Context>(), mock())

    // Word List Validation

    @Test
    fun test_valid_bip39_seed_words() {
        assertThat(createUtil().isValidBIP39Words(validWords)).isTrue()
    }

    @Test
    fun test_null_seed_words() {
        assertThat(createUtil().isValidBIP39Words(emptyArray())).isFalse()
    }

    @Test
    fun test_invalid_bip39_seed_words___not_part_of_the_list_of_words() {
        assertThat(createUtil().isValidBIP39Words(invalidWords)).isFalse()
    }

    @Test
    fun test_invalid_bip39_seed_words___missing_words() {
        assertThat(createUtil().isValidBIP39Words(
                arrayOf(validWords[0], validWords[1], validWords[2], validWords[3])
        )).isFalse()
    }

    // Bitcoin Address Validation

    @Test
    fun is_valid_btc_address_check_with_a_valid_address_test_P() {
        val address = "17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem"
        val util = createUtil()
        whenever(util.addressUtil.isBase58(address)).thenReturn(true)
        assertThat(
                util.isValidBTCAddress(address)
        ).isTrue()
    }

    @Test
    fun is_valid_btc_address_check_with_a_valid_address_test() {
        val address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"
        val util = createUtil()
        whenever(util.addressUtil.isBase58(address)).thenReturn(true)
        assertThat(
                util.isValidBTCAddress(address)
        ).isTrue()
    }

    @Test
    fun is_valid_btc_address_check_with_error_NOT_STANDARD_BTC_PATTERN_reason_test() {
        val address = "444445t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"
        val util = createUtil()
        assertThat(util.isValidBTCAddress(address)).isFalse()
    }

    @Test
    fun is_valid_btc_address_check_with_error_IS_A_BC1_reason_test() {
        val address = "bc135t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"
        val util = createUtil()
        whenever(util.addressUtil.isSegwit(address)).thenReturn(true)
        assertThat(
                util.isValidBTCAddress(address)
        ).isTrue()
    }

    @Test
    fun is_valid_btc_address_check_with_error_NOT_BASE58_reason_test() {
        val address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xaaaaa"
        val util = createUtil()
        whenever(util.addressUtil.typeOfPaymentAddress(address)).thenReturn(AddressType.UNKNOWN)
        assertThat(
                util.isValidBTCAddress(address)
        ).isFalse()
    }

    @Test
    fun is_valid_btc_address_check_with_error_empty_reason_test() {
        val util = createUtil()
        whenever(util.addressUtil.typeOfPaymentAddress("")).thenReturn(AddressType.UNKNOWN)
        assertThat(
                util.isValidBTCAddress("")
        ).isFalse()
    }
}