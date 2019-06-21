package app.dropbit.commons.util

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ByteArraysKtTest {
    @Test
    fun `to hex string`() {
        assertThat("Hello World".toByteArray().toHexString(), equalTo("48656c6c6f20576f726c64"))
    }
}