package app.dropbit.commons.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ByteArraysKtTest {
    @Test
    fun `to hex string`() {
        assertThat("Hello World".toByteArray().toHexString()).isEqualTo("48656c6c6f20576f726c64")
    }
}