package app.coinninja.cn.persistance.converter

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class DateTimeConverterTest {
    @Test
    fun toTimeOffset() {
        val date = DateTimeConverter.toOffsetDateTime("2019-08-20T18:39:48.851Z")!!
        assertThat(date.time).isEqualTo(1566326388851)
    }

    @Test
    fun fromOffset() {
        val expected = "2019-08-20T18:39:48.851Z"
        val date = DateTimeConverter.toOffsetDateTime(expected)!!
        assertThat(DateTimeConverter.fromOffsetDateTime(date)).isEqualTo(expected)

    }
}