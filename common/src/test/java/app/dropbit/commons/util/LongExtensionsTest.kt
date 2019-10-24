package app.dropbit.commons.util

import android.os.Build.VERSION_CODES.LOLLIPOP
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDateTime
import java.util.*

@RunWith(AndroidJUnit4::class)
class LongExtensionsTest {

    @Test
    fun to_RFC3339() {
        assertThat(1568740796762.toRFC3339()).isEqualTo("2019-09-17T17:19:56.762Z")
    }

    @Test
    fun to_local_date_time_UTC() {
        val date: LocalDateTime = 1568740796762.toLocalDateTimeUTC()
        assertThat(date.year).isEqualTo(2019)
        assertThat(date.monthValue).isEqualTo(9)
        assertThat(date.dayOfMonth).isEqualTo(17)
        assertThat(date.hour).isEqualTo(17)
        assertThat(date.minute).isEqualTo(19)
        assertThat(date.second).isEqualTo(56)
        assertThat(date.nano).isEqualTo(762000000)
        assertThat(date.toMillis()).isEqualTo(1568740796762)
    }

}