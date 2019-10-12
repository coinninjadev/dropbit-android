package app.dropbit.commons.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DoublesKtTest {
    @Test
    fun formats() {
        assertThat(200.00.decimalFormatted()).isEqualTo("200")
        assertThat(2000.144.decimalFormatted()).isEqualTo("2,000.14")
        assertThat(2000.145.decimalFormatted()).isEqualTo("2,000.14")
    }

}