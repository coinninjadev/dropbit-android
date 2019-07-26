package app.dropbit.commons.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RelativeTimeTest {
    @Test
    fun `before time`() {
        val millis = 0L

        assertThat(millis.makeRelativeTime()).isEqualTo("before recorded history")
    }

    @Test
    fun `in the future`() {
        val millis = System.currentTimeMillis() + MINUTE_MILLIS

        assertThat(millis.makeRelativeTime()).isEqualTo("in the future")
    }

    @Test
    fun `2 minutes ago`() {

        val millis = System.currentTimeMillis() - MINUTE_MILLIS * 2

        assertThat(millis.makeRelativeTime()).isEqualTo("2 minutes ago")
    }

    @Test
    fun `1 hour ago`() {

        val millis = System.currentTimeMillis() - HOUR_MILLIS

        assertThat(millis.makeRelativeTime()).isEqualTo("1 hour ago")
    }

    @Test
    fun `2 hours ago`() {

        val millis = System.currentTimeMillis() - HOUR_MILLIS * 2

        assertThat(millis.makeRelativeTime()).isEqualTo("2 hours ago")
    }

    @Test
    fun `yesterday`() {

        val millis = System.currentTimeMillis() - DAY_MILLIS

        assertThat(millis.makeRelativeTime()).isEqualTo("yesterday")
    }

    @Test
    fun `2 days ago`() {

        val millis = System.currentTimeMillis() - DAY_MILLIS * 2

        assertThat(millis.makeRelativeTime()).isEqualTo("2 days ago")
    }
}