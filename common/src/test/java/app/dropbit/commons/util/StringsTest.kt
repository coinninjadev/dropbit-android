package app.dropbit.commons.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.nio.charset.Charset

class StringsTest {

    @Test
    fun `hex string to byte array`() {
        val helloWorld = "hello world"

        val helloWorldHex = "68656c6c6f20776f726c64"

        assertThat(helloWorldHex.hexToBytes().toString(Charset.defaultCharset())).isEqualTo(helloWorld)
    }
}