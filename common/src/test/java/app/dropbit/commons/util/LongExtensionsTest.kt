package app.dropbit.commons.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LongExtensionsTest {

    @Test
    fun example() {
        assertThat(1568740796762.toRFC3339()).isEqualTo("2019-09-17T17:19:56.762Z")
    }


}