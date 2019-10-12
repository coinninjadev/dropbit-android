package com.coinninja.coinkeeper.util.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.BTCCurrency
import com.coinninja.coinkeeper.util.uri.parameter.BitcoinParameter
import com.coinninja.coinkeeper.util.uri.parameter.BitcoinParameter.AMOUNT
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class BitcoinUriTest {

    private fun createBuilder(): BitcoinUri.Builder {
        val bitcoinUtil = mock<BitcoinUtil>()
        whenever(bitcoinUtil.isValidBTCAddress(any())).thenReturn(true)
        return BitcoinUri.Builder(bitcoinUtil)
    }

    // --------------
    // Building
    // --------------

    @Test
    fun builds_from_address__ignores_bad_address() {
        val builder = createBuilder()
        val address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xaa"
        whenever(builder.bitcoinUtil.isValidBTCAddress(address)).thenReturn(false)

        val uri = builder.setAddress(address).build()

        assertThat(uri.toString()).isEqualTo("bitcoin:")
        assertThat(uri.address).isEqualTo("")
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.memo).isEqualTo("")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isFalse()
    }

    @Test
    fun builds_from_address() {
        val builder = createBuilder()
        val address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"

        val uri = builder.setAddress(address).build()

        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.address).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.hasRequiredFee).isFalse()
        assertThat(uri.requiredFee).isEqualTo(0.0)
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    @Test
    fun builds_with_amount_param() {
        val builder = createBuilder()
        val address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"
        val btc = BTCCurrency("1.0")
        val parameters = HashMap<BitcoinParameter, String>()
        parameters[AMOUNT] = btc.toUriFormattedString()

        val uri = builder.setAddress(address).addParameters(parameters).build()

        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=1.00000000")
        assertThat(uri.address).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.satoshiAmount).isEqualTo(100_000_000)
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    @Test
    fun builds_with_amount() {
        val builder = createBuilder()
        val address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"

        val uri = builder.setAddress(address).setAmount(BTCCurrency(150_000_000)).build()

        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=1.50000000")
    }

    @Test
    fun builder_can_remove_amount() {
        val builder = createBuilder()
        val address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"

        val uri = builder.setAddress(address).setAmount(BTCCurrency(150_000_000)).build()
        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=1.50000000")

        assertThat(builder.removeAmount().build().toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
    }

    @Test
    fun builder_can_add_a_memo() {
        val builder = createBuilder()
        val address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"
        val memo = "Hello World"

        val uri = builder.setAddress(address).setAmount(BTCCurrency(150_000_000)).setMemo(memo).build()
        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=1.50000000&memo=Hello%2BWorld")
        assertThat(uri.address).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.satoshiAmount).isEqualTo(150_000_000)
        assertThat(uri.memo).isEqualTo("Hello World")
    }

    @Test
    fun builder_can_add_a_required_fee() {
        val builder = createBuilder()
        val address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"
        val fee: Double = 5.0

        val uri = builder.setAddress(address).setAmount(BTCCurrency(150_000_000)).setFee(fee).build()
        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=1.50000000&required_fee=5.0")
        assertThat(uri.address).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.satoshiAmount).isEqualTo(150_000_000)
        assertThat(uri.hasRequiredFee).isTrue()
        assertThat(uri.requiredFee).isEqualTo(fee)
    }

    @Test
    fun builder_clear() {
        val builder = createBuilder()
        val address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"
        val fee: Double = 5.0

        builder.setAddress(address).setAmount(BTCCurrency(150_000_000)).setFee(fee).setMemo("foo bar")
        assertThat(builder.build().toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=1.50000000&required_fee=5.0&memo=foo%2Bbar")

        assertThat(builder.clear().build().toString()).isEqualTo("bitcoin:")
    }


    // --------------
    // PARSING
    // --------------

    // Text Globs

    @Test
    fun parse_just_a_valid_btc_address_and_no_amount_not_a_uri_test() {
        val builder = createBuilder()
        val sampleURI = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"
        val uri = builder.parse(sampleURI)
        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.address).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.satoshiAmount).isEqualTo(0)
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    @Test
    fun parse_just_a_valid_btc_address_and_no_amount_not_a_uri_test__garbage() {
        val builder = createBuilder()
        val data = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xaa"
        whenever(builder.bitcoinUtil.isValidBTCAddress(data)).thenReturn(false)
        val uri = builder.parse(data)
        assertThat(uri.toString()).isEqualTo("bitcoin:")
        assertThat(uri.address).isEqualTo("")
        assertThat(uri.satoshiAmount).isEqualTo(0)
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isFalse()
    }

    @Test
    fun parse_a_random_string_that_contains_a_valid_btc_uri_test() {
        val builder = createBuilder()
        val sampleURI = "Hello, here is my btc request bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=0.00325. Thanks for sending me the money"
        val uri = builder.parse(sampleURI)
        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=0.00325")
        assertThat(uri.address).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.satoshiAmount).isEqualTo(325_000)
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    @Test
    fun parse_a_random_string_that_contains_a_btc_address_somewhere_in_it_test() {
        val sampleURI = "Whats up my man. Here is my btc address 35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa did you need any more information?"
        val builder = createBuilder()
        val uri = builder.parse(sampleURI)
        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.address).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.satoshiAmount).isEqualTo(0)
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    @Test
    fun parse_a_random_string_that_contains_a_btc_address_somewhere_in_it_test__segwit() {
        val sampleURI = "Whats up my man. Here is my btc address bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu did you need any more information?"
        val builder = createBuilder()
        val uri = builder.parse(sampleURI)
        assertThat(uri.toString()).isEqualTo("bitcoin:bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu")
        assertThat(uri.address).isEqualTo("bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu")
        assertThat(uri.satoshiAmount).isEqualTo(0)
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    fun throw_exception_when_parsing_a_string_with_no_btc_data_test() {
        val message = "Hello, how are you today?"
        val builder = createBuilder()
        val uri = builder.parse(message)
        assertThat(uri.toString()).isEqualTo("bitcoin:")
        assertThat(uri.address).isEqualTo("")
        assertThat(uri.satoshiAmount).isEqualTo(0)
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isFalse()
    }

    @Test
    fun parsing_null_is_empty() {
        val uri = createBuilder().parse(null)
        assertThat(uri.toString()).isEqualTo("bitcoin:")
        assertThat(uri.address).isEqualTo("")
        assertThat(uri.satoshiAmount).isEqualTo(0)
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isFalse()
    }

    @Test
    fun parsing_empty_string_is_empty() {
        val uri = createBuilder().parse(null)
        assertThat(uri.toString()).isEqualTo("bitcoin:")
        assertThat(uri.address).isEqualTo("")
        assertThat(uri.satoshiAmount).isEqualTo(0)
        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isFalse()
    }

    // bitcoin uri

    @Test
    fun parse_a_valid_btc_uri_with_btc_address() {
        val builder = createBuilder()
        val sampleURI = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"

        val uri = builder.parse(sampleURI)

        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.address).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    @Test
    fun parse_a_valid_btc_uri_with_btc_address_and_amount_test() {
        val builder = createBuilder()
        val sampleURI = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=0.00325"

        val uri = builder.parse(sampleURI)

        assertThat(uri.scheme).isEqualTo("bitcoin")
        assertThat(uri.address).isEqualTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")
        assertThat(uri.satoshiAmount).isEqualTo(325_000)
        assertThat(uri.toString()).isEqualTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=0.00325")
        assertThat(uri.isBip70).isFalse()
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    // bip 70

    @Test
    fun parse_a_valid_bip70_with_identifier() {
        val builder = createBuilder()
        val sampleBip70URI = "bitcoin:?r=https://merchant.com/pay.php?h%3D2a8628fc2fbe"
        val uri = builder.parse(sampleBip70URI)
        assertThat(uri.isBip70).isTrue()
        assertThat(uri.merchantUri.toString()).isEqualTo("https://merchant.com/pay.php?h%3D2a8628fc2fbe")
        assertThat(uri.isValidPaymentAddress).isTrue()
    }


    @Test
    fun parse_a_valid_bip70_btc_uri_with_r_parameter() {
        val builder = createBuilder()
        val sampleURI = "bitcoin:?r=https://merchant.com/pay.php?h%3D2a8628fc2fbe"
        val uri = builder.parse(sampleURI)
        assertThat(uri.isBip70).isTrue()
        assertThat(uri.merchantUri.toString()).isEqualTo("https://merchant.com/pay.php?h%3D2a8628fc2fbe")
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    @Test
    fun parse_a_valid_bip70_url_without_bitcoin_scheme() {
        val sampleURI = "https://merchant.com/pay.php?h%3D2a8628fc2fbe"
        val builder = createBuilder()
        val uri = builder.parse(sampleURI)
        assertThat(uri.isBip70).isTrue()
        assertThat(uri.merchantUri.toString()).isEqualTo("https://merchant.com/pay.php?h%3D2a8628fc2fbe")
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    @Test
    fun parse_a_valid_bip70_btc_uri_with_request_parameter() {
        val sampleURI = "bitcoin:?request=https://merchant.com/pay.php?h%3D2a8628fc2fbe"
        val builder = createBuilder()
        val uri = builder.parse(sampleURI)
        assertThat(uri.isBip70).isTrue()
        assertThat(uri.merchantUri.toString()).isEqualTo("https://merchant.com/pay.php?h%3D2a8628fc2fbe")
        assertThat(uri.isValidPaymentAddress).isTrue()
    }

    @Test
    fun matches_bip70_when_merchant_on_query_string() {
        val sampleURI = "bitcoin:mq7se9wy2egettFxPbmn99cK8v5AFq55Lx?amount=0.11&r=https://merchant.com/pay.php?h%3D2a8628fc2fbe"
        val builder = createBuilder()
        val uri = builder.parse(sampleURI)
        assertThat(uri.merchantUri.toString()).isEqualTo("https://merchant.com/pay.php?h%3D2a8628fc2fbe")
        assertThat(uri.isBip70).isTrue()
        assertThat(uri.isValidPaymentAddress).isTrue()
    }
}